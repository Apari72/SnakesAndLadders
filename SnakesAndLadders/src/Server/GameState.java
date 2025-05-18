package Server;

import java.util.*;

public class GameState {
    private ClientHandler[] players;
    private int[] positions;
    private int currentPlayer;
    private final Map<Integer, Integer> snakesAndLadders = Map.ofEntries(
            Map.entry(3, 22), Map.entry(5, 8), Map.entry(11, 26),
            Map.entry(17, 4), Map.entry(19, 7), Map.entry(20, 29),
            Map.entry(27, 1), Map.entry(21, 9), Map.entry(32, 30),
            Map.entry(34, 12), Map.entry(36, 6), Map.entry(48, 26),
            Map.entry(62, 18), Map.entry(87, 24), Map.entry(93, 73),
            Map.entry(95, 75), Map.entry(98, 79)
    );

    public GameState(List<ClientHandler> clients) {
        this.players = new ClientHandler[] { clients.get(0), clients.get(1) };
        this.positions = new int[] { 0, 0 };
        this.currentPlayer = 0;
    }

    public void startGame() {
        broadcast("Both players connected. Game starts!");
        players[currentPlayer].sendMessage("YOUR_TURN");
    }

    public synchronized void handlePlayerInput(int playerId, String input) {
        if (playerId != currentPlayer) return;

        if ("ROLL".equalsIgnoreCase(input.trim())) {
            int roll = new Random().nextInt(6) + 1;
            positions[playerId] += roll;
            if (positions[playerId] > 100) positions[playerId] = 100;

            // Apply snake or ladder
            positions[playerId] = snakesAndLadders.getOrDefault(
                    positions[playerId], positions[playerId]
            );
            int finalPos = positions[playerId];

            // Structured broadcast for client animation
            broadcast("PLAYER_MOVE:" + (playerId+1) + ":" + roll + ":" + finalPos);

            // Victory check
            if (finalPos == 100) {
                broadcast("Player " + (playerId+1) + " WINS!");
                return;
            }

            // Next player's turn
            currentPlayer = (currentPlayer + 1) % 2;
            players[currentPlayer].sendMessage("YOUR_TURN");
        }
    }

    private void broadcast(String message) {
        for (ClientHandler player : players) {
            player.sendMessage(message);
        }
    }
}