package java;
import edu.wpi.first.networktables.DoubleSubscriber;
import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.networktables.NetworkTablesJNI;
import edu.wpi.first.util.CombinedRuntimeLoader;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import edu.wpi.first.cscore.CameraServerJNI;
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
        CameraServerJNI.Helper.setExtractOnStaticLoad(false);

        CombinedRuntimeLoader.loadLibraries(Program.class, "wpiutiljni", "wpimathjni", "ntcorejni",
                "cscorejnicvstatic");
        new Program().run(adbPath);
    }

    private static class SocketThread extends Thread {

        Bridge bridge;

        @Override
        public void run() {
            while (true) {
                try {
                    Socket socket = new Socket("127.0.0.1", PORT);
                    DataInputStream input = new DataInputStream(socket.getInputStream());
                    DataOutputStream output = new DataOutputStream(socket.getOutputStream());
    
                    while (true) {
    
                    }
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    public void run(String adbPath) throws IOException {
        NetworkTableInstance inst = NetworkTableInstance.getDefault();
        NetworkTable table = inst.getTable("datatable");
        DoubleSubscriber xSub = table.getDoubleTopic("x").subscribe(0.0);
        DoubleSubscriber ySub = table.getDoubleTopic("y").subscribe(0.0);
        inst.startClient4("example client");
        inst.setServer("localhost"); // where TEAM=190, 294, etc, or use inst.setServer("hostname") or similar
        // inst.startDSClient(); // recommended if running on DS computer; this gets the
        // robot IP from the DS
        String tcpString = "tcp:" + PORT;
        Runtime.getRuntime().exec(new String[] { adbPath, "forward", tcpString, tcpString });
        SocketThread adbConnection = new SocketThread();
        while(true) {
            
        }
    }

}