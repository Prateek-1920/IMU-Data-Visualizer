import imu_math.IMUConverter;
import java.io.*;
import java.net.*;

public class IMUServer {

    public static void main(String[] args) {
        String HOST = "192.168.199.18"; // Go to wifi settings and see IP of network connected to. In HyperIMU server IP
                                        // address, IP should be the same
        int PORT = 12345; // SAME ON BOTH THE DEVICES

        try {
            InetAddress serverAddress = InetAddress.getByName(HOST);
            ServerSocket serverSocket = new ServerSocket(PORT, 0, serverAddress);

            System.out.println("Server listening on " + HOST + ":" + PORT);

            Socket clientSocket = serverSocket.accept();
            System.out.println("Connected to client: " + clientSocket.getInetAddress());

            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            while (true) {
                String data = in.readLine();
                if (data == null) {
                    System.out.println("No data received. Exiting.");
                    break;
                }

                String[] values = data.split(",");
                if (values.length == 9) {
                    double linearAccelerationX = Double.parseDouble(values[0]);
                    double linearAccelerationY = Double.parseDouble(values[1]);
                    double linearAccelerationZ = Double.parseDouble(values[2]);

                    double angularAccelerationX = Double.parseDouble(values[3]);
                    double angularAccelerationY = Double.parseDouble(values[4]);
                    double angularAccelerationZ = Double.parseDouble(values[5]);

                    double eulerX = Math.toRadians(Double.parseDouble(values[6])); // Convert degrees to radians
                    double eulerY = Math.toRadians(Double.parseDouble(values[7])); // Convert degrees to radians
                    double eulerZ = Math.toRadians(Double.parseDouble(values[8])); // Convert degrees to radians

                    System.out.println("Received IMU data:");
                    System.out.println("Linear Acceleration X: " + linearAccelerationX);
                    System.out.println("Linear Acceleration Y: " + linearAccelerationY);
                    System.out.println("Linear Acceleration Z: " + linearAccelerationZ);

                    System.out.println("Angular Acceleration X: " + angularAccelerationX);
                    System.out.println("Angular Acceleration Y: " + angularAccelerationY);
                    System.out.println("Angular Acceleration Z: " + angularAccelerationZ);

                    double quat[] = IMUConverter.eulerToQuaternion(eulerY, eulerX, eulerZ); // Implementing IMUConverter
                                                                                            // package to convert Euler
                                                                                            // values to Quaternions

                    System.out.println("Quaternion X: " + quat[0]);
                    System.out.println("Quaternion Y: " + quat[1]);
                    System.out.println("Quaternion Z: " + quat[2]);
                    System.out.println("Quaternion W: " + quat[3]);
                } else {
                    System.out.println("Received data does not contain 9 values.");
                }
            }

            // Close the client socket
            clientSocket.close();

            // Close the server socket
            serverSocket.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
