//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package Server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
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
            this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.out = new PrintWriter(socket.getOutputStream(), true);
        } catch (IOException var4) {
            var4.printStackTrace();
        }

    }

    public void run() {
        this.sendMessage("WELCOME Player " + (this.playerId + 1));

        while(true) {
            try {
                String input = this.in.readLine();
                if (input != null) {
                    GameServer.getGameState().handlePlayerInput(this.playerId, input);
                }
            } catch (IOException var2) {
                System.out.println("Player " + (this.playerId + 1) + " disconnected.");
                return;
            }
        }
    }

    public void sendMessage(String message) {
        this.out.println(message);
    }
}
