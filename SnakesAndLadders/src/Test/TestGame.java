package Test;

import Client.GameClient;
import Server.GameServer;

public class TestGame {
    public static void main(String[] args) {
        Server.GameServer s = new GameServer();

        Client.GameClient c1 = new GameClient();
        Client.GameClient c2 = new GameClient();
    }
}
