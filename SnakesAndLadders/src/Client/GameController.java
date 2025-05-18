package Client;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

public class GameController {
    @FXML private Label statusLabel;
    @FXML private Button rollButton;
    @FXML private BoardPane boardPane;

    private NetworkManager networkManager;

    public void setConnection(NetworkManager networkManager) {
        this.networkManager = networkManager;
    }

    @FXML
    private void initialize() {
        statusLabel.setText("Waiting for players...");
        rollButton.setDisable(true);
    }

    @FXML
    private void handleRollDice() {
        if (networkManager != null) {
            networkManager.sendMessage("ROLL");
            statusLabel.setText("Roll sent! Waiting...");
            rollButton.setDisable(true);
        }
    }

    public void handleServerMessage(String message) {
        Platform.runLater(() -> {
            if ("YOUR_TURN".equals(message)) {
                statusLabel.setText("Your turn! Click Roll Dice.");
                rollButton.setDisable(false);

            } else if (message.startsWith("PLAYER_MOVE:")) {
                // Format: PLAYER_MOVE:<pid>:<roll>:<finalDest>
                String[] parts = message.split(":");
                int pid  = Integer.parseInt(parts[1]);
                int roll = Integer.parseInt(parts[2]);
                int dest = Integer.parseInt(parts[3]);
                statusLabel.setText(
                        "Player " + pid + " rolled " + roll + " → " + dest
                );
                boardPane.animatePlayerMove(pid, roll, dest);

            } else if (message.endsWith("WINS!")) {
                statusLabel.setText(message);
                rollButton.setDisable(true);

            } else {
                // Other messages
                statusLabel.setText(message);
            }
        });
    }
}