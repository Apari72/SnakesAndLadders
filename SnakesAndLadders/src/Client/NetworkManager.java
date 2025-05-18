package Client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

public class NetworkManager {
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private MessageListener listener;

    public NetworkManager(Socket socket, BufferedReader in, PrintWriter out) {
        this.socket = socket;
        this.in = in;
        this.out = out;
        startListening(); // Start listening for messages from the server
    }

    public void sendMessage(String msg) {
        out.println(msg);
    }

    public void setMessageListener(MessageListener listener) {
        this.listener = listener;
    }

    private void startListening() {
        Thread thread = new Thread(() -> {
            try {
                String line;
                while ((line = in.readLine()) != null) {
                    if (listener != null) {
                        listener.onMessageReceived(line);
                    }
                }
            } catch (IOException e) {
                System.err.println("Error reading from server: " + e.getMessage());
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    public interface MessageListener {
        void onMessageReceived(String message);
    }

    public void close() {
        try {
            socket.close();
            in.close();
            out.close();
        } catch (IOException e) {
            System.err.println("Error closing connection: " + e.getMessage());
        }
    }
}
