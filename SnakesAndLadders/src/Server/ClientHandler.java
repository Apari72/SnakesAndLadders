package Server;

import java.io.*;
import java.net.Socket;

public class ClientHandler extends Thread {
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private int playerId;

    public ClientHandler(Socket socket, int playerId) {
        this.socket = socket;
        this.playerId = playerId;

        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void run() {
        sendMessage("WELCOME Player " + (playerId + 1));
        while (true) {
            try {
                String input = in.readLine();
                if (input != null) {
                    GameServer.getGameState().handlePlayerInput(playerId, input);
                }
            } catch (IOException e) {
                System.out.println("Player " + (playerId + 1) + " disconnected.");
                break;
            }
        }
    }

    public void sendMessage(String message) {
        out.println(message);
    }
}
