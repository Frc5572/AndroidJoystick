package java;
import edu.wpi.first.networktables.DoubleSubscriber;
import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.networktables.NetworkTableType;
import edu.wpi.first.networktables.NetworkTableValue;
import edu.wpi.first.networktables.NetworkTablesJNI;
import edu.wpi.first.networktables.NetworkTable.TableEventListener;
import edu.wpi.first.networktables.NetworkTableEvent.Kind;
import edu.wpi.first.util.CombinedRuntimeLoader;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.EnumSet;
import java.util.List;

import org.msgpack.core.MessageBufferPacker;
import org.msgpack.core.MessagePack;
import org.msgpack.core.MessageUnpacker;
import org.msgpack.core.buffer.MessageBuffer;

import edu.wpi.first.math.WPIMathJNI;
import edu.wpi.first.util.WPIUtilJNI;
import net.ffst.adbpotato.Bridge;

public class Program {

    private static final int PORT = 1235;

    public static void main(String[] args) throws IOException {
        String adbPath = "adb";
        if (args.length > 0) {
            adbPath = args[0];
        }
        NetworkTablesJNI.Helper.setExtractOnStaticLoad(false);
        WPIUtilJNI.Helper.setExtractOnStaticLoad(false);
        WPIMathJNI.Helper.setExtractOnStaticLoad(false);

        CombinedRuntimeLoader.loadLibraries(Program.class, "wpiutiljni", "wpimathjni", "ntcorejni",
                "cscorejnicvstatic");
        new Program().run(adbPath);
    }

    private static NetworkTableValue msgPackToNT(MessageUnpacker unpacker) throws IOException {
        int len;
        byte[] bytes;
        NetworkTableValue[] values;
        String[] keys;
        switch(unpacker.getNextFormat()) {
            case UINT8:
            case UINT16:
            case UINT32:
            case UINT64:
            case INT8:
            case INT16:
            case INT32:
            case INT64:
            case POSFIXINT:
            case NEGFIXINT:
                return NetworkTableValue.makeInteger(unpacker.unpackLong());
            case FLOAT32:
                return NetworkTableValue.makeFloat(unpacker.unpackFloat());
            case FLOAT64:
                return NetworkTableValue.makeDouble(unpacker.unpackDouble());
            case BOOLEAN:
                return NetworkTableValue.makeBoolean(unpacker.unpackBoolean());
            case BIN8:
            case BIN16:
            case BIN32:
                len = unpacker.unpackBinaryHeader();
                bytes = new byte[len];
                for(int i = 0; i < len; i++) {
                    bytes[i] = unpacker.unpackByte();
                }
                return NetworkTableValue.makeRaw(bytes);
            case STR8:
            case STR16:
            case STR32:
            case FIXSTR:
                len = unpacker.unpackBinaryHeader();
                bytes = new byte[len];
                for(int i = 0; i < len; i++) {
                    bytes[i] = unpacker.unpackByte();
                }
                return NetworkTableValue.makeRaw(bytes);
            default:
                return null;
        }
    }

    private static MessageBuffer NTToMsgPack(NetworkTableValue value) throws IOException {
        MessageBufferPacker packer = MessagePack.newDefaultBufferPacker();
        int len;
        switch(value.getType()) {
            case kBoolean:
                packer.packBoolean(value.getBoolean());
                break;
            case kBooleanArray:
                len = value.getBooleanArray().length;
                packer.packArrayHeader(len);
                for(int i = 0; i < len; i++) {
                    packer.packBoolean(value.getBooleanArray()[i]);
                }
                break;
            case kDouble:
                packer.packDouble(value.getDouble());
                break;
            case kDoubleArray:
                len = value.getDoubleArray().length;
                packer.packArrayHeader(len);
                for(int i = 0; i < len; i++) {
                    packer.packDouble(value.getDoubleArray()[i]);
                }
                break;
            case kFloat:
                packer.packFloat(value.getFloat());
                break;
            case kFloatArray:
                len = value.getFloatArray().length;
                packer.packArrayHeader(len);
                for(int i = 0; i < len; i++) {
                    packer.packFloat(value.getFloatArray()[i]);
                }
                break;
            case kInteger:
                packer.packLong(value.getInteger());
                break;
            case kIntegerArray:
                len = value.getIntegerArray().length;
                packer.packArrayHeader(len);
                for(int i = 0; i < len; i++) {
                    packer.packLong(value.getIntegerArray()[i]);
                }
                break;
            case kRaw:
                len = value.getRaw().length;
                packer.packBinaryHeader(len);
                for(int i = 0; i < len; i++) {
                    packer.packByte(value.getRaw()[i]);
                }
                break;
            case kString:
                len = value.getString().length();
                packer.packRawStringHeader(len);
                for(int i = 0; i < len; i++) {
                    packer.packByte(value.getString().getBytes()[i]);
                }
                break;
            default:
                break;
        }
        packer.close();
        return packer.toMessageBuffer();
    }

    public void run(String adbPath) throws IOException {
        String tcpString = "tcp:" + PORT;
        Runtime.getRuntime().exec(new String[] { adbPath, "forward", tcpString, tcpString });
        Bridge bridge = new Bridge(Bridge.Side.Potato);
        NetworkTableInstance inst = NetworkTableInstance.getDefault();
        NetworkTable table = inst.getTable("androidjoystick");
        inst.startClient4("example client");
        inst.setServer("10.55.72.2"); // where TEAM=190, 294, etc, or use inst.setServer("hostname") or similar
        // inst.startDSClient(); // recommended if running on DS computer; this gets the
        // robot IP from the DS
        bridge.addListener((k, v) -> {
            System.out.println("update nt from adb for " + k);
            try {
                NetworkTableValue value = msgPackToNT(v);
                if(value != null) {
                    table.putValue(k, value);
                } else {
                    System.err.println("Invalid msgpack!");
                }
            } catch(IOException e) {
                e.printStackTrace();
            }
        });
        table.addListener(EnumSet.of(Kind.kValueRemote), (tab, key, event) -> {
            System.out.println("update adb from nt for " + key);
            try {
                bridge.publish(key, NTToMsgPack(event.valueData.value));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        bridge.start();
        while(true) {}
    }

}