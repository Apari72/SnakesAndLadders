package Client;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

public class GameController {
    @FXML private Label statusLabel;
    @FXML private Button rollButton;
    @FXML private Button restartButton;
    @FXML private BoardPane boardPane;

    private NetworkManager networkManager;

    public GameController() { }

    public void setConnection(NetworkManager networkManager) {
        this.networkManager = networkManager;
    }

    @FXML
    private void initialize() {
        statusLabel.setText("Waiting for players...");
        rollButton.setDisable(true);
        restartButton.setDisable(true);
    }

    @FXML
    private void handleRollDice() {
        if (networkManager != null) {
            networkManager.sendMessage("ROLL");
            statusLabel.setText("Roll sent! Waiting...");
            rollButton.setDisable(true);
        }
    }

    @FXML
    private void handleRestart() {
        if (networkManager != null) {
            networkManager.sendMessage("RESTART_REQUEST");
            restartButton.setDisable(true);
            statusLabel.setText("Restart requested. Waiting for opponent...");
        }
    }

    public void handleServerMessage(String message) {
        Platform.runLater(() -> {
            if ("YOUR_TURN".equals(message)) {
                statusLabel.setText("Your turn! Click Roll Dice.");
                rollButton.setDisable(false);

            } else if (message.startsWith("PLAYER_MOVE:")) {
                String[] parts = message.split(":");
                int pid  = Integer.parseInt(parts[1]);
                int roll = Integer.parseInt(parts[2]);
                int dest = Integer.parseInt(parts[3]);
                statusLabel.setText("Player " + pid + " rolled " + roll + " → " + dest);
                boardPane.animatePlayerMove(pid, roll, dest);

            } else if (message.startsWith("Winner: ")) {
                int winner = Integer.parseInt(message.split(":")[1].trim());

                statusLabel.setText("Player " + winner + " wins! Restart?");
                rollButton.setDisable(true);
                restartButton.setDisable(false);

            } else if (message.startsWith("RESTART_VOTE:")) {
                int voter = Integer.parseInt(message.split(":")[1]);
                statusLabel.setText("Player " + voter + " is ready to restart.");

            } else if ("GAME_RESET".equals(message)) {
                boardPane.resetAllTokens();
                statusLabel.setText("Game reset! Waiting for your turn...");
                rollButton.setDisable(true);
                restartButton.setDisable(true);

            } else {
                // Other broadcasts like welcome messages
                statusLabel.setText(message);
            }
        });
    }
}
