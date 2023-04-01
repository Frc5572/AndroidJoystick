package net.ffst.adbpotato;


import org.msgpack.core.MessageBufferPacker;
import org.msgpack.core.MessagePack;
import org.msgpack.core.MessagePacker;
import org.msgpack.core.MessageUnpacker;
import org.msgpack.core.buffer.MessageBuffer;

import java.io.Closeable;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

public class Bridge extends Thread {

    public static class Pair<A, B> {
        public A first;
        public B second;

        public static <Ax, Bx> Pair<Ax, Bx> create(Ax a, Bx b) {
            Pair<Ax, Bx> pair = new Pair<>();
            pair.first = a;
            pair.second = b;
            return pair;
        }
    }

    public static final int PORT = 1235;

    public enum Side {
        Potato(false), Android(true);

        public final boolean isServer;

        Side(boolean b) {
            isServer = b;
        }
    }

    @FunctionalInterface
    public interface Listener {
        void update(String key, MessageUnpacker value);
    }

    private List<Listener> listeners = new ArrayList<>();

    private volatile boolean running = false;
    private Side side;
    private DataOutputStream output;

    private ArrayBlockingQueue<Pair<String, MessageBuffer>> buffersToPush = new ArrayBlockingQueue<>(20);

    public Bridge(Side side) throws IOException {
        this.side = side;
    }

    @Override
    public synchronized void start() {
        running = true;
        super.start();
    }

    @Override
    public void run() {
        while (running) {
            try {
                Socket socket;
                ServerSocket server = null;
                if (side.isServer) {
                    server = new ServerSocket(PORT);
                    socket = server.accept();
                } else {
                    socket = new Socket("127.0.0.1", PORT);
                }
                output = new DataOutputStream(socket.getOutputStream());
                DataInputStream input = new DataInputStream(socket.getInputStream());
                byte[] bytes = new byte[8192];
                System.out.println("loop");
                while(running) {
                    buffersToPush.forEach((pair) -> {
                        try {
                            System.out.println("Writing " + pair.first);
                            output.writeUTF(pair.first);
                            byte[] buf = pair.second.array();
                            output.writeInt(buf.length);
                            output.write(buf);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    });
                    buffersToPush.clear();
                    if (input.available() == 0) continue;
                    String key = input.readUTF();
                    int len = input.readInt();
                    int num = input.read(bytes, 0, len);
                    if (num < 0) { // Occurs when socket closes
                        break;
                    }
                    MessageUnpacker unpacker = MessagePack.newDefaultUnpacker(ByteBuffer.wrap(bytes, 0, len));
                    for (Listener listener : listeners) {
                        listener.update(key, unpacker);
                    }
                    unpacker.close();
                }
                if (side.isServer) {
                    assert server != null;
                    server.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            output = null;
        }
    }

    public void end() {
        running = false;
    }

    public void addListener(Listener listener) {
        listeners.add(listener);
    }

    public void addListener(String key, Listener listener) {
        addListener((k, v) -> {
            if(k.equals(key)) {
                listener.update(k, v);
            }
        });
    }

    public void publish(String key, MessageBuffer buffer) throws IOException {
        buffersToPush.add(Pair.create(key, buffer));
    }

    public void publish(String key, double value) throws IOException {
        System.out.println("publish " + key);
        MessageBufferPacker packer = MessagePack.newDefaultBufferPacker();
        packer.packDouble(value);
        packer.close();
        publish(key, packer.toMessageBuffer());
    }

    public void publish(String key, int value) throws IOException {
        System.out.println("publish " + key);
        MessageBufferPacker packer = MessagePack.newDefaultBufferPacker();
        packer.packInt(value);
        packer.close();
        publish(key, packer.toMessageBuffer());
    }

    public void publish(String key, boolean value) throws IOException {
        MessageBufferPacker packer = MessagePack.newDefaultBufferPacker();
        packer.packBoolean(value);
        packer.close();
        publish(key, packer.toMessageBuffer());
    }

}
