package Client;

import javafx.animation.SequentialTransition;
import javafx.animation.TranslateTransition;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.text.Text;
import javafx.util.Duration;

import java.util.*;

public class BoardPane extends Pane {
    private static final int SIZE = 10;
    private static final double CELL = 50;
    private final Map<Integer,Circle> tokens = new HashMap<>();
    private final Map<Integer,Integer> positions = new HashMap<>();

    private final Map<Integer,Integer> snakesAndLadders = Map.ofEntries(
            Map.entry(3, 22), Map.entry(5, 8), Map.entry(11, 26),
            Map.entry(17, 4), Map.entry(19, 7), Map.entry(20,29),
            Map.entry(27, 1), Map.entry(21, 9), Map.entry(32,30),
            Map.entry(34,12), Map.entry(36,6), Map.entry(48,26),
            Map.entry(62,18), Map.entry(87,24), Map.entry(93,73),
            Map.entry(95,75), Map.entry(98,79)
    );

    public BoardPane() {
        setPrefSize(SIZE*CELL, SIZE*CELL);
        drawGrid(); drawSnakesAndLadders();
    }

    private void drawGrid() {
        for (int i = 0; i <= SIZE; i++) {
            getChildren().addAll(
                    new Line(i*CELL, 0, i*CELL, SIZE*CELL),
                    new Line(0, i*CELL, SIZE*CELL, i*CELL)
            );
        }
        for (int n = 1; n <= SIZE*SIZE; n++) {
            int row = (n-1)/SIZE;
            boolean l2r = row%2==0;
            int col = l2r ? (n-1)%SIZE : (SIZE-1)-((n-1)%SIZE);
            Text t = new Text(col*CELL+5,
                    (SIZE-1-row)*CELL+15,
                    String.valueOf(n));
            getChildren().add(t);
        }
    }

    private void drawSnakesAndLadders() {
        for (var e : snakesAndLadders.entrySet()) {
            double[] s = coords(e.getKey());
            double[] d = coords(e.getValue());
            Line line = new Line(s[0], s[1], d[0], d[1]);
            line.setStroke(e.getKey()>e.getValue()?Color.RED:Color.GREEN);
            line.setStrokeWidth(4);
            getChildren().add(line);
        }
    }

    private double[] coords(int sq) {
        int row = (sq-1)/SIZE,
                col = ((row%2)==0)
                        ? (sq-1)%SIZE
                        : (SIZE-1)-((sq-1)%SIZE);
        return new double[]{
                col*CELL + CELL/2.0,
                (SIZE-1-row)*CELL + CELL/2.0
        };
    }

    /**
     * @param pid 1-based
     * @param roll number of steps
     * @param finalDest server-computed final square
     */
    public void animatePlayerMove(int pid, int roll, int finalDest) {
        Circle c = tokens.computeIfAbsent(pid, id -> {
            Circle circle = new Circle(10, pid == 1 ? Color.BLUE : Color.ORANGE);
            // initial placement at square 1 if first time
            double[] init = coords(1);
            circle.setCenterX(init[0]);
            circle.setCenterY(init[1]);
            getChildren().add(circle);
            positions.put(pid, 1);
            return circle;
        });

        int start = positions.getOrDefault(pid, 1);
        animateSteps(pid, c, start, roll, finalDest);
    }

    private void animateSteps(int pid, Circle c, int current, int remaining, int finalDest) {
        if (remaining > 0) {
            int next = current + 1;
            double[] dest = coords(next);

            TranslateTransition tt = new TranslateTransition(Duration.millis(200), c);
            tt.setToX(dest[0] - c.getCenterX());
            tt.setToY(dest[1] - c.getCenterY());
            tt.setOnFinished(e -> {
                c.setTranslateX(0);
                c.setTranslateY(0);
                c.setCenterX(dest[0]);
                c.setCenterY(dest[1]);
                positions.put(pid, next);
                animateSteps(pid, c, next, remaining - 1, finalDest);
            });
            tt.play();

        } else if (finalDest != current) {
            // single jump for snake or ladder
            double[] dest = coords(finalDest);
            TranslateTransition tt = new TranslateTransition(Duration.millis(300), c);
            tt.setToX(dest[0] - c.getCenterX());
            tt.setToY(dest[1] - c.getCenterY());
            tt.setOnFinished(e -> {
                c.setTranslateX(0);
                c.setTranslateY(0);
                c.setCenterX(dest[0]);
                c.setCenterY(dest[1]);
                positions.put(pid, finalDest);
            });
            tt.play();
        }
}}