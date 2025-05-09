package Server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class GameServer {
    private static final int PORT = 12345;
    private static ArrayList<ClientHandler> clients = new ArrayList<>();
    private static GameState gameState;

    public static void main(String[] args) {
        System.out.println("Server started. Waiting for players...");

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (clients.size() < 2) {
                Socket socket = serverSocket.accept();
                ClientHandler clientHandler = new ClientHandler(socket, clients.size());
                clients.add(clientHandler);
                clientHandler.start();
                System.out.println("Player " + clients.size() + " connected.");
            }

            // All players connected, start the game
            gameState = new GameState(clients);
            gameState.startGame();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static GameState getGameState() {
        return gameState;
    }
}
