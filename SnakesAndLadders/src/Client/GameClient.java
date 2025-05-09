package Client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class GameClient extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        // Connect to server
        Socket socket = new Socket("127.0.0.1", 12345); // replace with AWS IP for deployment
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

        // Load FXML
        FXMLLoader loader = new FXMLLoader(getClass().getResource("GameView.fxml"));
        Scene scene = new Scene(loader.load());

        // Get controller and set connection
        GameController controller = loader.getController();
        controller.setConnection(socket, in, out);

        // Show window
        stage.setTitle("Snakes and Ladders");
        stage.setScene(scene);
        stage.show();

        // Start a thread to read messages from server
        new Thread(() -> {
            try {
                String line;
                while ((line = in.readLine()) != null) {
                    String finalLine = line;
                    javafx.application.Platform.runLater(() -> controller.handleServerMessage(finalLine));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
