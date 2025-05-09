package Test;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;


public class JavaFXTest extends Application {
    public void start(Stage stage) {
        stage.setScene(new Scene(new Label("JavaFX works!"), 200, 100));
        stage.setTitle("Test");
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

