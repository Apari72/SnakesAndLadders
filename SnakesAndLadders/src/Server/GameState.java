package Server;

import java.util.List;
import java.util.Map;
import java.util.Random;

public class GameState {
    private ClientHandler[] players;
    private int[] positions;
    private int currentPlayer;
    private final Map<Integer, Integer> snakesAndLadders = Map.ofEntries(
            Map.entry(3, 22), Map.entry(5, 8), Map.entry(11, 26), Map.entry(17, 4),
            Map.entry(19, 7), Map.entry(20, 29), Map.entry(38, 44), Map.entry(64, 46),
            Map.entry(32, 30), Map.entry(34, 12), Map.entry(36, 6), Map.entry(48, 26),
            Map.entry(62, 57), Map.entry(47, 65), Map.entry(73, 91), Map.entry(95, 75), Map.entry(98, 81),Map.entry(70, 54),Map.entry(77, 82));

    // ✅ Track restart votes
    private boolean[] restartVotes = new boolean[]{false, false};

    public GameState(List<ClientHandler> clients) {
        this.players = new ClientHandler[]{clients.get(0), clients.get(1)};
        this.positions = new int[]{1, 1};  // For testing; set to {1, 1} for normal gameplay
        this.currentPlayer = 0;
    }

    public void startGame() {
        broadcast("Both players connected. Game starts!");
        players[currentPlayer].sendMessage("YOUR_TURN");
    }

    public synchronized void handlePlayerInput(int playerId, String input) {
        input = input.trim();

        if ("ROLL".equalsIgnoreCase(input) && playerId == currentPlayer) {
            int roll = new Random().nextInt(6) + 1;
            positions[playerId] += roll;
            if (positions[playerId] > 100) {
                positions[playerId] = 100;
            }

            positions[playerId] = snakesAndLadders.getOrDefault(positions[playerId], positions[playerId]);
            int finalPos = positions[playerId];

            broadcast("PLAYER_MOVE:" + (playerId + 1) + ":" + roll + ":" + finalPos);

            if (finalPos == 100) {
                broadcast("Winner: " + (playerId + 1));
                return;
            }

            currentPlayer = (currentPlayer + 1) % 2;
            players[currentPlayer].sendMessage("YOUR_TURN");

        } else if ("RESTART_REQUEST".equalsIgnoreCase(input)) {
            // ✅ Handle restart vote
            restartVotes[playerId] = true;
            broadcast("RESTART_VOTE:" + (playerId + 1));

            if (restartVotes[0] && restartVotes[1]) {
                resetGame();
            }
        }
    }

    private void broadcast(String message) {
        for (ClientHandler player : players) {
            player.sendMessage(message);
        }
    }

    // ✅ Reset game state
    private void resetGame() {
        positions = new int[]{1, 1};      // Reset positions
        currentPlayer = 0;               // Player 1 starts
        restartVotes[0] = false;
        restartVotes[1] = false;

        broadcast("GAME_RESET");
        players[currentPlayer].sendMessage("YOUR_TURN");
    }
}
