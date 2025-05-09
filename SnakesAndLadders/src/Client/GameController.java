package Client;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Random;

public class GameController {

    // --- FXML-injected UI elements ---
    @FXML private Label statusLabel;
    @FXML private Pane boardPane;
    @FXML private Button rollButton;

    // --- Networking fields ---
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;

    // --- Game logic ---
    private final Random random = new Random();

    /**
     * This method is called by GameClient right after loading the FXML.
     * It hands over the socket and I/O streams so we can talk to the server.
     */
    public void setConnection(Socket socket, BufferedReader in, PrintWriter out) {
        this.socket = socket;
        this.in     = in;
        this.out    = out;

        // Start a thread to listen for server messages
        new Thread(() -> {
            try {
                String line;
                while ((line = in.readLine()) != null) {
                    String msg = line;
                    Platform.runLater(() -> handleServerMessage(msg));
                }
            } catch (IOException e) {
                Platform.runLater(() -> statusLabel.setText("Connection lost."));
            }
        }).start();
    }

    /**
     * Called by the Roll Dice button.
     * Rolls locally for UI feedback, then sends the roll command to the server.
     */
    @FXML
    private void handleRollDice() {
        if (out == null) {
            statusLabel.setText("Not connected to server.");
            return;
        }

        int diceRoll = random.nextInt(6) + 1;          // 1–6
        statusLabel.setText("You rolled a " + diceRoll);

        // Send a command to the server; server logic should interpret "ROLL" by itself,
        // or you could send the explicit number: out.println("ROLL:" + diceRoll);
        out.println("ROLL");

        // Disable the button until the server tells us it's our turn again
        rollButton.setDisable(true);
    }

    /**
     * Handles any message coming from the server.
     * Update statusLabel and re-enable the roll button if it's your turn.
     */
    public void handleServerMessage(String message) {
        // Example protocol:
        // "WELCOME Player 1", "YOUR_TURN", "Player 2 rolled a 4 ...", "Player 1 WINS!"
        statusLabel.setText(message);

        if (message.equals("YOUR_TURN")) {
            rollButton.setDisable(false);
        }
    }
}
