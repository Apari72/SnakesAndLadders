package com.omerfaruk.snakesandladders;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class Server {

    private static final int PORT = 12345;
    private static final ExecutorService pool = Executors.newCachedThreadPool();
    private static final Queue<Socket> waitingPlayers = new LinkedList<>();

    public static void main(String[] args) {
        System.out.println("Server started. Waiting for players...");
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New player connected: " + clientSocket);

                synchronized (waitingPlayers) {
                    waitingPlayers.add(clientSocket);
                    if (waitingPlayers.size() >= 2) {
                        Socket player1 = waitingPlayers.poll();
                        Socket player2 = waitingPlayers.poll();
                        GameSession session = new GameSession(player1, player2);
                        pool.execute(session);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

class GameSession implements Runnable {

    private final ClientHandler player1;
    private final ClientHandler player2;

    public GameSession(Socket socket1, Socket socket2) throws IOException {
        player1 = new ClientHandler(socket1, 1);
        player2 = new ClientHandler(socket2, 2);
        player1.setOpponent(player2);
        player2.setOpponent(player1);
    }

    @Override
    public void run() {
        player1.sendMessage("START:1");
        player2.sendMessage("START:2");

        new Thread(player1).start();
        new Thread(player2).start();
    }
}

class ClientHandler implements Runnable {

    private final Socket socket;
    private final int playerId;
    private OutputStream out;
    private Scanner in;
    private ClientHandler opponent;

    public ClientHandler(Socket socket, int playerId) throws IOException {
        this.socket = socket;
        this.playerId = playerId;
        this.out = socket.getOutputStream();
        this.in = new Scanner(socket.getInputStream());
    }

    public void setOpponent(ClientHandler opponent) {
        this.opponent = opponent;
    }

    public void sendMessage(String message) {
        try {
            out.write((message + "\n").getBytes());
            out.flush();
        } catch (IOException e) {
            System.out.println("Error sending message to Player " + playerId);
        }
    }

    @Override
    public void run() {
        try {
            while (in.hasNextLine()) {
                String message = in.nextLine();
                System.out.println("PLAYER" + playerId + ": " + message);
                if (opponent != null) {
                    opponent.sendMessage("PLAYER" + playerId + ":" + message);
                }
            }
        } catch (Exception e) {
            System.out.println("Connection lost with Player " + playerId);
        }
    }
}
