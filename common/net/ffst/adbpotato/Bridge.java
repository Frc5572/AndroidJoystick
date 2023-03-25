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
import java.util.List;

public class Bridge extends Thread {

    private static final int PORT = 1234;

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

    private List<Listener> listeners;

    private volatile boolean running = false;
    private Side side;
    private DataOutputStream output;

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
                synchronized (output) {
                    output = new DataOutputStream(socket.getOutputStream());
                }
                DataInputStream input = new DataInputStream(socket.getInputStream());
                byte[] bytes = new byte[2048];
                while(running) {
                    String key = input.readUTF();
                    int len = input.readInt();
                    int num = input.read(bytes, 0, len);
                    if(num < 0) { // Occurs when socket closes
                        break;
                    }
                    MessageUnpacker unpacker = MessagePack.newDefaultUnpacker(ByteBuffer.wrap(bytes, 0, len));
                    for(Listener listener : listeners) {
                        listener.update(key, unpacker);
                    }
                    unpacker.close();
                }
                synchronized (output) {
                    output = null;
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
        synchronized (output) {
            output.writeUTF(key);
            byte[] buf = buffer.array();
            output.writeInt(buf.length);
            output.write(buf);
        }
    }

    public void publish(String key, double value) throws IOException {
        MessageBufferPacker packer = MessagePack.newDefaultBufferPacker();
        packer.packDouble(value);
        packer.close();
        publish(key, packer.toMessageBuffer());
    }

    public void publish(String key, int value) throws IOException {
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
