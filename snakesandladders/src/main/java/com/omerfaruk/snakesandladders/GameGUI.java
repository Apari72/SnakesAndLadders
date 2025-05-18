package com.omerfaruk.snakesandladders;

import javafx.application.Application;
import javafx.application.Platform;

import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.IOException;

import java.io.OutputStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GameGUI extends Application {

    private boolean isMyTurn = false;

    private final int tileSize = 50;
    private final int boardSize = 10;

    private String playerName = "Player";
    private Circle player1, player2;
    private int[] pos1 = { 1 };
    private int[] pos2 = { 1 };

    
    private Label statusLabel;
    private Button rollButton;
    private Button resetButton;
    private Text rollResult;

    private javafx.scene.control.TextArea chatArea;
    private javafx.scene.control.TextField chatInput;
    private javafx.scene.control.Button sendButton;

    private Map<Integer, Integer> transitions = new HashMap<>();

    private Circle myPawn;
    private Circle opponentPawn;
    private int[] myPos;
    private int[] opponentPos;
    // SOCKET Connection variables
    private Socket socket;
    private OutputStream out;
    private Scanner in;
    private int myPlayerId;

    private boolean myResetRequest = false;
    private boolean opponentResetRequest = false;
    private boolean gameEnded = false;

    @Override
    public void start(Stage primaryStage) {
        playerName = askPlayerName();

        Pane root = new Pane();

        //Game board picture
        Image board = new Image(getClass().getResource("/images/SnakesAndLaddersBoardPaint.jpg").toExternalForm());
        ImageView boardImage = new ImageView(board);
        boardImage.setFitWidth(tileSize * boardSize);
        boardImage.setFitHeight(tileSize * boardSize);
        root.getChildren().add(boardImage);

        // Players
        player1 = new Circle(10, Color.RED);
        player2 = new Circle(10, Color.BLUE);
        moveToTile(player1, 1);
        moveToTile(player2, 1);
        root.getChildren().addAll(player1, player2);

        // CHAT PART
        // 🔵 Chat Past
        chatArea = new javafx.scene.control.TextArea();
        chatArea.setEditable(false);
        chatArea.setPrefWidth(200);
        chatArea.setPrefHeight(400);
        chatArea.setLayoutX(tileSize * boardSize + 10); 
        chatArea.setLayoutY(10);

        // 🔵 Chat Input (Write message box)
        chatInput = new javafx.scene.control.TextField();
        chatInput.setPromptText("Write message...");
        chatInput.setPrefWidth(140);
        chatInput.setLayoutX(tileSize * boardSize + 10);
        chatInput.setLayoutY(420);

        // 🔵 Send Button
        sendButton = new javafx.scene.control.Button("Send");
        sendButton.setPrefWidth(50);
        sendButton.setLayoutX(tileSize * boardSize + 150);
        sendButton.setLayoutY(420);

        // 🔵 Send button action
        sendButton.setOnAction(_ -> {
            String message = chatInput.getText().trim();
            Platform.runLater(() -> chatArea.appendText(playerName + ": " + message + "\n"));
            sendChatMessage();
        });

        
        root.getChildren().addAll(chatArea, chatInput, sendButton);

        // Buttons and Labels

        rollButton = new Button("🎲 Roll Dice");
        rollButton.setLayoutX(10);
        rollButton.setLayoutY(510);

        resetButton = new Button("🔁 Reset");
        resetButton.setLayoutX(300);
        resetButton.setLayoutY(510);

        rollResult = new Text("Result: ");
        rollResult.setLayoutX(120);
        rollResult.setLayoutY(525);

        statusLabel = new Label("Connecting...");
        statusLabel.setLayoutX(10);
        statusLabel.setLayoutY(550);

        root.getChildren().addAll(rollButton, resetButton, rollResult, statusLabel);

        Scene scene = new Scene(root, tileSize * boardSize + 230, tileSize * boardSize + 80);
        primaryStage.setTitle("Snakes and Ladders Multiplayer (Player: " + playerName + ")");
        primaryStage.setScene(scene);
        primaryStage.show();

        // Snakes and Ladders
        initializeTransitions();

        // Start socket connection
        connectToServer();

        // Roll Dice
        rollButton.setOnAction(_ -> {
            int roll = new Random().nextInt(6) + 1;
            rollResult.setText("You Rolled the Dice: " + roll);
            sendMessage("ROLL:" + roll);
            Platform.runLater(() -> handleRoll(myPlayerId, roll));
            rollButton.setDisable(true);
        });

        // Reset
        resetButton.setOnAction(_ -> {
            if (!gameEnded) {
                rollResult.setText("It cannot be reset before the game is over.");
                return;
            }
            myResetRequest = true;
            sendMessage("RESET_REQUEST");

            if (opponentResetRequest) {
                resetGame();
            } else {
                statusLabel.setText("We are waiting for the opponent for the reset...");
            }
        });
    }

    private void connectToServer() {
        new Thread(() -> {
            try {
                socket = new Socket("13.61.187.150", 12345); // HERE WE WRITE THE AWS IP
                out = socket.getOutputStream();
                in = new Scanner(socket.getInputStream());

                listenToServer();

            } catch (IOException ex) {
                Logger.getLogger(GameGUI.class.getName()).log(Level.SEVERE, null, ex);
                Platform.runLater(() -> statusLabel.setText("Connection Error! " + playerName + " Could not connect."));
            }
        }).start();
    }

    private void listenToServer() {
        try {
            while (in.hasNextLine()) {
                String message = in.nextLine();
                System.out.println("From Server: " + message);

                if (message.startsWith("START:")) {
                    myPlayerId = Integer.parseInt(message.split(":")[1]);

                    Platform.runLater(() -> {
                        statusLabel.setText("You are connected to the game as " + playerName + "! Your player number:" + myPlayerId);
                        isMyTurn = (myPlayerId == 1);
                        updateRollButton();

                        
                        if (myPlayerId == 1) {
                            myPawn = player1;
                            myPos = pos1;
                            opponentPawn = player2;
                            opponentPos = pos2;
                        } else {
                            myPawn = player2;
                            myPos = pos2;
                            opponentPawn = player1;
                            opponentPos = pos1;
                        }
                    });
                } else if (message.startsWith("PLAYER")) {
                    String[] parts = message.split(":");
                    if (parts.length >= 2) {
                        int playerId = Integer.parseInt(parts[0].substring(6));
                        String action = parts[1];

                        if (action.equals("ROLL") && parts.length >= 3) {
                            int rolled = Integer.parseInt(parts[2]);
                            Platform.runLater(() -> handleRoll(playerId, rolled));
                        } else if (action.equals("RESET_REQUEST")) {
                            opponentResetRequest = true;
                            System.out.println("Opponent's reset request has arrived!");
                            if (myResetRequest) {
                                System.out.println("We both asked for a reset, the game is resetting!");
                                Platform.runLater(this::resetGame);
                            } else {
                                Platform.runLater(() -> statusLabel.setText("Opponent invites to play again..."));
                            }
                        } else if (action.equals("CHAT") && parts.length >= 3) {
                            // Incoming chat message
                            String opponentName = parts[2];
                            String chatMessage = parts[3];
                            Platform.runLater(() -> chatArea.appendText(opponentName + ": " + chatMessage + "\n"));
                        }
                    }
                }

            }
        } catch (Exception e) {
            Logger.getLogger(GameGUI.class.getName()).log(Level.SEVERE, null, e);
        }
    }

    private void sendMessage(String message) {
        try {
            out.write((message + "\n").getBytes());
            out.flush();
        } catch (IOException ex) {
            Logger.getLogger(GameGUI.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void handleRoll(int playerId, int rolled) {
        if (playerId == myPlayerId) {
            animateMovement(myPawn, myPos, rolled); // move your own pawn
        } else {
            animateMovement(opponentPawn, opponentPos, rolled); // move your opponent's pawn
        }

        // Turn transition
        if (playerId == myPlayerId) {
            isMyTurn = false; // I was the one who rolled the dice → its the opponent's turn
        } else {
            isMyTurn = true; // The opponent was the one who rolled the dice → its my turn
        }

        updateRollButton();
    }

    private void animateMovement(Circle player, int[] pos, int rolled) {
        new Thread(() -> {
            for (int i = 0; i < rolled; i++) {
                int newPos = pos[0] + 1;
                if (newPos > 100)
                    newPos = 100;

                int finalNewPos = newPos;
                Platform.runLater(() -> moveToTile(player, finalNewPos));
                pos[0] = newPos;

                try {
                    Thread.sleep(200); // wait 200ms between each step
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            // Snake and ladder control
            if (transitions.containsKey(pos[0])) {
                int targetPos = transitions.get(pos[0]);
                Platform.runLater(() -> moveToTile(player, targetPos));
                pos[0] = targetPos;
            }

            // If 100 is reached finish the game
            if (pos[0] == 100) {
                Platform.runLater(() -> {
                    if (player == myPawn) {
                        statusLabel.setText("Congratulations " + playerName + ", You win!");
                    } else {
                        statusLabel.setText("I am sorry " + playerName + ", You lose!");
                    }
                    rollButton.setDisable(true);
                    gameEnded = true;
                });
            }

        }).start();
    }

    private void moveToTile(Circle player, int tileNumber) {
        int row = (tileNumber - 1) / boardSize;
        int col = (tileNumber - 1) % boardSize;

        if (row % 2 == 1) {
            col = boardSize - 1 - col;
        }

        int x = col * tileSize + tileSize / 2;
        int y = (boardSize - 1 - row) * tileSize + tileSize / 2;

        player.setCenterX(x);
        player.setCenterY(y);
    }

    private void initializeTransitions() {
        transitions.put(6, 27);
        transitions.put(11, 34);
        transitions.put(17, 69);
        transitions.put(46, 86);
        transitions.put(60, 85);
        transitions.put(25, 2);
        transitions.put(52, 42);
        transitions.put(70, 55);
        transitions.put(95, 72);
        transitions.put(99, 54);

    }

    private void resetGame() {
        
        pos1[0] = 1;
        pos2[0] = 1;
        moveToTile(player1, pos1[0]);
        moveToTile(player2, pos2[0]);
        myResetRequest = false;
        opponentResetRequest = false;
        gameEnded = false;
        rollResult.setText("Result: ");
        statusLabel.setText("Game reset! Player 1 starts.");
        isMyTurn = (myPlayerId == 1); //Player 1 starts
        updateRollButton();
    }

    private void updateRollButton() {
        rollButton.setDisable(!isMyTurn);
    }

    private String askPlayerName() {
        javafx.scene.control.TextInputDialog dialog = new javafx.scene.control.TextInputDialog();
        dialog.setTitle("Welcome - Player Name Screen");
        dialog.setHeaderText("Enter Your Name");
        dialog.setContentText("Name:");

        return dialog.showAndWait().orElse("Player");
    }

    private void sendChatMessage() {
        String message = chatInput.getText().trim();
        if (!message.isEmpty()) {
            sendMessage("CHAT:" + playerName + ": " + message);
            chatInput.clear();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }

}
