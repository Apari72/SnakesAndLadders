//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package Client;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javafx.animation.TranslateTransition;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.text.Text;
import javafx.util.Duration;

public class BoardPane extends Pane {
    private static final int SIZE = 10;
    private static final double CELL = 50.0;
    private final Map<Integer, Circle> tokens = new HashMap();
    private final Map<Integer, Integer> positions = new HashMap();
    private final Map<Integer, Integer> snakesAndLadders = Map.ofEntries( Map.entry(3, 22), Map.entry(5, 8), Map.entry(11, 26), Map.entry(17, 4),
            Map.entry(19, 7), Map.entry(20, 29), Map.entry(38, 44), Map.entry(64, 46),
            Map.entry(32, 30), Map.entry(34, 12), Map.entry(36, 6), Map.entry(48, 26),
            Map.entry(62, 57), Map.entry(47, 65), Map.entry(73, 91), Map.entry(95, 75), Map.entry(98, 81),Map.entry(70, 54),Map.entry(77, 82));

    public BoardPane() {
        this.setPrefSize(500.0, 500.0);
        this.drawGrid();
        this.drawSnakesAndLadders();
    }

    private void drawGrid() {
        int n;
        for(n = 0; n <= 10; ++n) {
            this.getChildren().addAll(new Node[]{new Line((double)n * 50.0, 0.0, (double)n * 50.0, 500.0), new Line(0.0, (double)n * 50.0, 500.0, (double)n * 50.0)});
        }

        for(n = 1; n <= 100; ++n) {
            int row = (n - 1) / 10;
            boolean l2r = row % 2 == 0;
            int col = l2r ? (n - 1) % 10 : 9 - (n - 1) % 10;
            Text t = new Text((double)col * 50.0 + 5.0, (double)(9 - row) * 50.0 + 15.0, String.valueOf(n));
            this.getChildren().add(t);
        }

    }

    private void drawSnakesAndLadders() {
        Iterator var1 = this.snakesAndLadders.entrySet().iterator();

        while(var1.hasNext()) {
            Map.Entry<Integer, Integer> e = (Map.Entry)var1.next();
            double[] s = this.coords((Integer)e.getKey());
            double[] d = this.coords((Integer)e.getValue());
            Line line = new Line(s[0], s[1], d[0], d[1]);
            line.setStroke((Integer)e.getKey() > (Integer)e.getValue() ? Color.RED : Color.GREEN);
            line.setStrokeWidth(4.0);
            this.getChildren().add(line);
        }

    }

    private double[] coords(int sq) {
        int row = (sq - 1) / 10;
        int col = row % 2 == 0 ? (sq - 1) % 10 : 9 - (sq - 1) % 10;
        return new double[]{(double)col * 50.0 + 25.0, (double)(9 - row) * 50.0 + 25.0};
    }

    public void animatePlayerMove(int pid, int roll, int finalDest) {
        Circle c = (Circle)this.tokens.computeIfAbsent(pid, (id) -> {
            Circle circle = new Circle(10.0, pid == 1 ? Color.BLUE : Color.ORANGE);
            double[] init = this.coords(1);
            circle.setCenterX(init[0]);
            circle.setCenterY(init[1]);
            this.getChildren().add(circle);
            this.positions.put(pid, 1);
            return circle;
        });
        int start = (Integer)this.positions.getOrDefault(pid, 1);
        this.animateSteps(pid, c, start, roll, finalDest);
    }

    private void animateSteps(int pid, Circle c, int current, int remaining, int finalDest) {
        if (remaining > 0) {
            int next = current + 1;
            double[] dest = this.coords(next);
            TranslateTransition tt = new TranslateTransition(Duration.millis(200.0), c);
            tt.setToX(dest[0] - c.getCenterX());
            tt.setToY(dest[1] - c.getCenterY());
            tt.setOnFinished((e) -> {
                c.setTranslateX(0.0);
                c.setTranslateY(0.0);
                c.setCenterX(dest[0]);
                c.setCenterY(dest[1]);
                this.positions.put(pid, next);
                this.animateSteps(pid, c, next, remaining - 1, finalDest);
            });
            tt.play();
        } else if (finalDest != current) {
            double[] dest = this.coords(finalDest);
            TranslateTransition tt = new TranslateTransition(Duration.millis(300.0), c);
            tt.setToX(dest[0] - c.getCenterX());
            tt.setToY(dest[1] - c.getCenterY());
            tt.setOnFinished((e) -> {
                c.setTranslateX(0.0);
                c.setTranslateY(0.0);
                c.setCenterX(dest[0]);
                c.setCenterY(dest[1]);
                this.positions.put(pid, finalDest);
            });
            tt.play();
        }

    }
    public void resetAllTokens() {
        // Clear all stored positions
        positions.clear();

        // Move each token back to the starting square (1)
        double[] startCoords = coords(1);
        for (Circle token : tokens.values()) {
            token.setCenterX(startCoords[0]);
            token.setCenterY(startCoords[1]);
        }
    }

}
