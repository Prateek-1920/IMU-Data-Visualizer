import imu_math.IMUConverter;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class IMUparser extends Application {
    private Label imuDataLabel;
    private Button toggleButton;
    private Button recordButton;
    private boolean displayEuler = true;
    private boolean recording = false;
    private BufferedWriter csvWriter;
    private String currentFileName = null;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        imuDataLabel = new Label("IMU Data:");
        toggleButton = new Button("Toggle Display");
        recordButton = new Button("Record Data");
        HBox buttonBox = new HBox(toggleButton, recordButton);

        // Add click event handlers to the buttons
        toggleButton.setOnAction(e -> toggleDisplay());
        recordButton.setOnAction(e -> toggleRecordData());

        VBox root = new VBox(imuDataLabel, buttonBox);
        root.setSpacing(10); // Add some spacing between elements
        root.setPadding(new javafx.geometry.Insets(10)); // Add padding to the container
        Scene scene = new Scene(root, 800, 400);

        // Apply some basic styling to the label and buttons
        imuDataLabel.setStyle("-fx-font-size: 16;");
        toggleButton.setStyle("-fx-font-size: 14;");
        recordButton.setStyle("-fx-font-size: 14;");

        primaryStage.setTitle("IMU Data Viewer");
        primaryStage.setScene(scene);
        primaryStage.show();

        // Start a thread to receive IMU data from the network socket
        new Thread(this::receiveIMUData).start();
    }

    // Toggle between displaying Euler angles and quaternions
    private void toggleDisplay() {
        displayEuler = !displayEuler;
    }

    // Toggle data recording
    private void toggleRecordData() {
        recording = !recording;

        if (recording) {
            try {
                currentFileName = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()) + "_imu_data.csv";
                csvWriter = new BufferedWriter(new FileWriter(currentFileName));
                imuDataLabel.setText("Recording data...");
                recordButton.setText("Stop Recording Data");
            } catch (IOException e) {
                showRecordError();
                recording = false;
            }
        } else {
            try {
                if (csvWriter != null) {
                    csvWriter.close();
                    imuDataLabel.setText("Recording stopped. Data saved to CSV file: " + currentFileName);
                    recordButton.setText("Record Data");
                }
            } catch (IOException e) {
                showRecordError();
            }
        }
    }

    // Receive IMU data from the network socket and update the GUI
    private void receiveIMUData() {
        String HOST = "192.168.199.18";
        int PORT = 12345;

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
                    updateIMUDataLabel("No data received. Exiting.");
                    break;
                }

                String[] values = data.split(",");
                if (values.length == 9) {
                    double linearAccelerationX = Double.parseDouble(values[0]);
                    double linearAccelerationY = Double.parseDouble(values[1]);
                    double linearAccelerationZ = Double.parseDouble(values[2]);

                    // Check if the linear acceleration exceeds 10
                    if (Math.abs(linearAccelerationX) > 10 || Math.abs(linearAccelerationY) > 10
                            || Math.abs(linearAccelerationZ) > 10) {
                        showAccelerationWarning();
                    }

                    double angularAccelerationX = Double.parseDouble(values[3]);
                    double angularAccelerationY = Double.parseDouble(values[4]);
                    double angularAccelerationZ = Double.parseDouble(values[5]);

                    double eulerX = Math.toRadians(Double.parseDouble(values[6]));
                    double eulerY = Math.toRadians(Double.parseDouble(values[7]));
                    double eulerZ = Math.toRadians(Double.parseDouble(values[8]));

                    double quat[] = IMUConverter.eulerToQuaternion(eulerY, eulerX, eulerZ);

                    // Update the GUI with received IMU data
                    String imuData;
                    if (displayEuler) {
                        imuData = "Received IMU data:\n" +
                                "Linear Acceleration X: " + linearAccelerationX + "\n" +
                                "Linear Acceleration Y: " + linearAccelerationY + "\n" +
                                "Linear Acceleration Z: " + linearAccelerationZ + "\n" +
                                "Angular Acceleration X: " + angularAccelerationX + "\n" +
                                "Angular Acceleration Y: " + angularAccelerationY + "\n" +
                                "Angular Acceleration Z: " + angularAccelerationZ + "\n" +
                                "Euler X: " + Math.toDegrees(eulerX) + " degrees\n" +
                                "Euler Y: " + Math.toDegrees(eulerY) + " degrees\n" +
                                "Euler Z: " + Math.toDegrees(eulerZ) + " degrees";
                    } else {
                        imuData = "Received IMU data:\n" +
                                "Linear Acceleration X: " + linearAccelerationX + "\n" +
                                "Linear Acceleration Y: " + linearAccelerationY + "\n" +
                                "Linear Acceleration Z: " + linearAccelerationZ + "\n" +
                                "Angular Acceleration X: " + angularAccelerationX + "\n" +
                                "Angular Acceleration Y: " + angularAccelerationY + "\n" +
                                "Angular Acceleration Z: " + angularAccelerationZ + "\n" +
                                "Quaternion X: " + quat[0] + "\n" +
                                "Quaternion Y: " + quat[1] + "\n" +
                                "Quaternion Z: " + quat[2] + "\n" +
                                "Quaternion W: " + quat[3] + "\n";
                    }
                    updateIMUDataLabel(imuData);

                    if (recording && csvWriter != null) {
                        writeDataToCSV(data);
                    }
                } else {
                    updateIMUDataLabel("Received data does not contain 9 values.");
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

    // Update the IMU data label on the JavaFX UI
    private void updateIMUDataLabel(String data) {
        Platform.runLater(() -> imuDataLabel.setText(data));
    }

    // Show a warning dialog for high acceleration
    private void showAccelerationWarning() {
        Platform.runLater(() -> {
            Alert alert = new Alert(AlertType.WARNING);
            alert.setTitle("Acceleration Warning");
            alert.setHeaderText("High Linear Acceleration Detected");
            alert.setContentText("Linear acceleration exceeds 10. Please be cautious.");
            alert.show();
        });
    }

    // Show an error dialog for recording data
    private void showRecordError() {
        Platform.runLater(() -> {
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Recording Error");
            alert.setHeaderText("Error occurred while recording data.");
            alert.show();
        });
    }

    // Write data to the CSV file
    private void writeDataToCSV(String data) {
        try {
            if (csvWriter != null) {
                csvWriter.write(data);
                csvWriter.newLine();
            }
        } catch (IOException e) {
            showRecordError();
            recording = false;
        }
    }
}
