package GUI;

import Simulator.Robot;
import Util.Circle;
import Util.SmallestEnclosingCircle;
import Util.Vector;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.transform.Affine;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class RobotView extends Region {

    // parameters for the canvas
    private double viewX = 0; // bottom left coords
    private double viewY = 0;
    private double oldViewX = 0; // used when dragging
    private double oldViewY = 0;
    private double mouseX = 0; // mouse coords at start of dragging
    private double mouseY = 0;
    private final double LINE_SEP = 1; // the distance between two lines
    private double scale = 40; // the current scale of the coordinate system
    private final double MAX_SCALE = 200; // the maximum scale of the coordinate system
    private final double MIN_SCALE = 10; // the minimum scale of the coordinate system
    public SimpleBooleanProperty drawCoordinateSystems = new SimpleBooleanProperty(false);
    public SimpleBooleanProperty drawSEC = new SimpleBooleanProperty(false);
    public SimpleBooleanProperty drawRadii = new SimpleBooleanProperty(false);
    private Canvas canvas;

    public RobotView() {

        setStyle("-fx-background-color: lightblue;");

        canvas = new Canvas();
        getChildren().add(canvas);


        setOnMouseDragged(this::canvasMouseDragged);
        setOnMousePressed(this::canvasMousePressed);
        setOnScroll(this::canvasScrolled);

        canvas.widthProperty().bind(this.widthProperty());
        canvas.heightProperty().bind(this.heightProperty());

//        onMouseDragged="#canvasMouseDragged" onMousePressed="#canvasMousePressed" onScroll="#canvasScrolled"
    }


    public void setWidth(double w) {
        super.setWidth(w);
    }

    public void setHeight(double h) {
        super.setHeight(h);
    }

    /**
     * Draws a grid in the canvas based on the viewX, viewY and the scale
     */
    public void paintCanvas(Robot[] robots) {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        double portHeight = canvas.getHeight();
        double portWidth = canvas.getWidth();

        // clear the canvas
        gc.setFill(Color.WHITE);
        gc.fillRect(0, 0, portWidth, portHeight);

        // calculate the starting position of the lines
        double lineX;
        if (viewX < 0) {
            lineX = Math.abs(viewX % LINE_SEP) - LINE_SEP;
        } else {
            lineX = 0 - Math.abs(viewX % LINE_SEP);
        }
        double lineY;
        if (viewY < 0) {
            lineY = Math.abs(viewY % LINE_SEP) - LINE_SEP;
        } else {
            lineY = 0 - Math.abs(viewY % LINE_SEP);
        }

        gc.setStroke(Color.BLACK);
        gc.setLineWidth(1);
        // vertical lines
        while (lineX * scale <= portWidth) {
            String label;
            if (viewX + lineX < 0) {
                label = String.valueOf((int) (viewX + lineX - 0.5));
            } else {
                label = String.valueOf((int) (viewX + lineX + 0.5));
            }
            gc.strokeText(label, (int) (lineX * scale) + 4, portHeight - 10);
            gc.strokeLine((int) ((lineX * scale) + 0.5), 0, (int) ((lineX * scale) + 0.5), portHeight);
            lineX += LINE_SEP;
        }

        // horizontal lines (from top to bottom, because 0 is at the top)
        while (lineY * scale <= portHeight) {
            String label;
            if (viewY + lineY < 0) {
                label = String.valueOf((int) (viewY + lineY - 0.5));
            } else {
                label = String.valueOf((int) (viewY + lineY + 0.5));
            }
            gc.strokeText(label, 10, (int) portHeight - (lineY * scale) - 4);
            gc.strokeLine(0, (int) (portHeight - (lineY * scale) + 0.5), portWidth, (int) (portHeight - (lineY * scale) + 0.5));
            lineY += LINE_SEP;
        }

        // draw legend for the coordinate system
        if (drawCoordinateSystems.get()) {
            Vector center = new Vector(50, 100);
            Vector right = new Vector(100, 100);
            Vector up = new Vector(50, 50);
            gc.setLineWidth(2.5);
            gc.setStroke(Color.RED);
            gc.strokeLine(center.x, center.y, up.x, up.y);
            gc.strokeText("y", up.x + 5, up.y);
            gc.setStroke(Color.GREEN);
            gc.strokeLine(center.x, center.y, right.x, right.y);
            gc.strokeText("x", right.x, right.y - 5);
        }

        Affine tOld = gc.getTransform();
        Affine t = new Affine();
        // set the transform to a new transform
        gc.transform(t);
        // transform into the new coordinate system so we can draw on that.
        gc.translate(0, portHeight - 1);
        gc.scale(scale, -scale); // second negative to reflect horizontally and draw from the bottom left
        gc.translate(-viewX, -viewY);

        Circle SEC = null;
        boolean drawSEC = this.drawSEC.get(); // We'd ideally use JavaFX vector graphics, but taking a snapshot like this works too.
        boolean drawRadii = this.drawRadii.get(); // We'd ideally use JavaFX vector graphics, but taking a snapshot like this works too.
        boolean drawCoordinateSystems = this.drawCoordinateSystems.get(); // We'd ideally use JavaFX vector graphics, but taking a snapshot like this works too.

        if (drawSEC || drawRadii) {
            List<Vector> robotPositions = Arrays.stream(robots).map(r -> r.pos).collect(Collectors.toList());
            SEC = SmallestEnclosingCircle.makeCircle(robotPositions);
        }
        if (drawSEC) {
            gc.setLineWidth(0.05);
            gc.setStroke(Color.BLACK);
            gc.strokeOval(SEC.c.x - SEC.r, SEC.c.y - SEC.r, 2 * SEC.r, 2 * SEC.r);
            gc.setFill(Color.BLACK);
            double centerR = 0.05;
            gc.fillOval(SEC.c.x - centerR, SEC.c.y - centerR, 2 * centerR, 2 * centerR);
        }
        if (drawRadii) {
            for (Robot r : robots) {
                if (r.pos.equals(SEC.c)) continue;
                gc.setLineWidth(0.03);
                gc.setStroke(Color.BLACK);
                Vector onCircle = drawSEC ? SEC.getPointOnCircle(r.pos) : r.pos;
                gc.strokeLine(onCircle.x, onCircle.y, SEC.c.x, SEC.c.y);
            }
        }

        // draw on the coordinate system
        for (Robot r : robots) {
            if (drawCoordinateSystems) {
                Vector up = new Vector(0, 1);
                Vector right = new Vector(1, 0);
                Vector upGlobal = r.trans.localToGlobal(up, r.pos);
                Vector rightGlobal = r.trans.localToGlobal(right, r.pos);
                gc.setLineWidth(0.05);
                gc.setStroke(Color.RED);
                gc.strokeLine(r.pos.x, r.pos.y, upGlobal.x, upGlobal.y);
                gc.setStroke(Color.GREEN);
                gc.strokeLine(r.pos.x, r.pos.y, rightGlobal.x, rightGlobal.y);
            }
            gc.setStroke(Color.BLACK);
            gc.setLineWidth(0.05);
            switch (r.state) {
                case SLEEPING:
                    gc.setFill(Color.WHITE);
                    break;
                case MOVING:
                    gc.setFill(Color.GREEN);
                    break;
                case COMPUTING:
                    gc.setFill(Color.RED);
                    break;
            }
            double robotWidth = 0.5;
            gc.fillOval(r.pos.x - robotWidth / 2, r.pos.y - robotWidth / 2, robotWidth, robotWidth);
            gc.strokeOval(r.pos.x - robotWidth / 2, r.pos.y - robotWidth / 2, robotWidth, robotWidth);
        }

        // transform back to the old transform
        gc.setTransform(tOld);
    }

    /**
     * Zoom in on the mouse coordinates
     *
     * @param mouseX the x coord of the mouse
     * @param mouseY the y coord of the mouse
     */
    public void zoomIn(double mouseX, double mouseY) {
        if (scale < MAX_SCALE) { // prevent scrolling further
            double portHeight = canvas.getHeight();

            // calculate canvas coord
            double xCoord = mouseX / scale + viewX;
            double yCoord = ((mouseY - (portHeight - 1)) / -scale) + viewY;

            scale *= 1.5;

            // calculate new canvas coord
            double xCoordNew = mouseX / scale + viewX;
            double yCoordNew = ((mouseY - (portHeight - 1)) / -scale) + viewY;

            // the difference should be added to the bottom left of the view
            viewX += xCoord - xCoordNew;
            viewY += yCoord - yCoordNew;
        }
    }

    /**
     * Zoom out on the mouse coordinates.
     *
     * @param mouseX the x coord of the mouse
     * @param mouseY the y coord of the mouse
     */
    public void zoomOut(double mouseX, double mouseY) {
        if (scale > MIN_SCALE) {
            double portHeight = canvas.getHeight();

            // calculate canvas coord
            double xCoord = mouseX / scale + viewX;
            double yCoord = ((mouseY - (portHeight - 1)) / -scale) + viewY;

            scale /= 1.5;

            // calculate new canvas coord
            double xCoordNew = mouseX / scale + viewX;
            double yCoordNew = ((mouseY - (portHeight - 1)) / -scale) + viewY;

            // the difference should be added to the bottom left of the view
            viewX += xCoord - xCoordNew;
            viewY += yCoord - yCoordNew;
        }
    }

    @FXML
    public void canvasMouseDragged(MouseEvent e) {
        viewX = oldViewX - ((e.getX() - mouseX) * (1 / scale));
        viewY = oldViewY + ((e.getY() - mouseY) * (1 / scale)); // + because y is upside down
    }

    @FXML
    public void canvasMousePressed(MouseEvent e) {
        mouseX = e.getX();
        mouseY = e.getY();
        oldViewX = viewX;
        oldViewY = viewY;
    }

    @FXML
    public void canvasScrolled(ScrollEvent e) {
        // This is the amount of pixels that should be scrolled where it a normal scrollbar.
        // For my mouse, it has a default of 40 per "notch". Therefore the division by 40.
        double deltaY = e.getDeltaY();
        // If we detect scroll, but it was less than 40, set it to 40.
        if (deltaY > 0 && deltaY < 40) {
            deltaY = 40;
        } else if (deltaY < 0 && deltaY > 40) {
            deltaY = -40;
        }
        int notches = ((int) deltaY / 40);
        if (notches > 0) {
            for (int i = 0; i < notches; i++) {
                zoomIn(e.getX(), e.getY());
            }
        } else {
            for (int i = 0; i > notches; i--) {
                zoomOut(e.getX(), e.getY());
            }
        }
    }

}
