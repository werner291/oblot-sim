package GUI;

import PositionTransformations.PositionTransformation;
import Simulator.Robot;
import Util.Circle;
import Util.SmallestEnclosingCircle;
import Util.Vector;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.transform.Affine;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * JavaFX view component that contains a canvas that draws the different robots.
 *
 * Robots are given in through the paintCanvas, which makes this component
 * effectively stateless w.r.t the robots.
 */
public class RobotView extends Region {

    private ContextMenu cM = null;
    // parameters for the canvas
    private double viewX = 0; // bottom left coords
    private double viewY = 0;
    private double oldViewX = 0; // used when dragging
    private double oldViewY = 0;
    private double mouseX = 0; // mouse coords at start of dragging
    private double mouseY = 0;
    private double scale = 40; // the current scale of the coordinate system
    private final double MAX_SCALE = 200; // the maximum scale of the coordinate system
    private final double MIN_SCALE = 10; // the minimum scale of the coordinate system


    /**
     * Determines whether the robots' local coordinate systems are visualized,
     * indicating how their view of the world around them is transformed.
     */
    public SimpleBooleanProperty drawCoordinateSystems = new SimpleBooleanProperty(false);

    /**
     * Determine whether the smallest enclosing circle should be drawn of all the robots.
     */
    public SimpleBooleanProperty drawSEC = new SimpleBooleanProperty(false);

    /**
     * Determine whether the radii of the robots on the SEC is to be drawn.
     */
    public SimpleBooleanProperty drawRadii = new SimpleBooleanProperty(false);

    /**
     * Determine whether the radii of the robots on the SEC is to be drawn.
     */
    public SimpleBooleanProperty drawRobotLabel = new SimpleBooleanProperty(true);

    private Canvas canvas;

    /**
     * Interface for any object that manages an editable set of robots.
     */
    public interface RobotManager {

        /**
         * Add a robot to the simulation.
         *
         * canEditRobots will be checked beforehand whether this method can safely be called.
         */
        void addRobot(Robot toAdd);

        /**
         * Remove a robot to the simulation.
         *
         * canEditRobots will be checked beforehand whether this method can safely be called.
         */
        void removeRobot(Robot toRemove);

        /**
         * @return Wether it is currently safe to call addRobot/removeRobot.
         */
        boolean canEditRobots();

        /**
         * Return a reference to the current array of robots.
         *
         * The RobotView will not keep a reference to this or modify it.
         */
        Robot[] getRobots();
    }

    public void setRobotManager(RobotManager robotManager) {
        this.robotManager = robotManager;
    }

    private RobotManager robotManager;

    public RobotView() {

        canvas = new Canvas();
        getChildren().add(canvas);

        // Set up dragging and clicking event handlers.
        setOnMouseDragged(this::canvasMouseDragged);
        setOnMousePressed(this::canvasMousePressed);
        setOnScroll(this::canvasScrolled);

        // Make sure the canvas stays at the same size as its' container.
        canvas.widthProperty().bind(this.widthProperty());
        canvas.heightProperty().bind(this.heightProperty());

        setUpContextMenu();
    }

    public void setWidth(double w) {
        super.setWidth(w);
    }

    public void setHeight(double h) {
        super.setHeight(h);
    }

    /**
     * Draws a grid in the canvas based on the viewX, viewY and the scale, using a given set of robots.
     */
    public void paintCanvas() {
        Robot[] robots = robotManager.getRobots();
        GraphicsContext gc = canvas.getGraphicsContext2D();
        double portHeight = canvas.getHeight();
        double portWidth = canvas.getWidth();

        // clear the canvas
        gc.setFill(Color.WHITE);
        gc.fillRect(0, 0, portWidth, portHeight);

        // Draw the background grid.
        drawGrid(gc, portHeight, portWidth);

        // Draw the robots and any associated graphics.
        drawRobots(robots, gc, portHeight);

        if (drawRobotLabel.get()) {
            gc.setLineWidth(1);
            for (Robot r : robots) {
                double offset = 2.5;
                double xCoord = (r.pos.x - viewX) * scale;
                double yCoord = (r.pos.y - viewY) * -scale + (portHeight - 1);
                gc.strokeText((r.id + 1) + "", xCoord - offset, yCoord + offset);
            }
        }

        // Draw the axes legend.
        drawPTVisualizationLegend(gc);

    }

    public void drawRobots(Robot[] robots, GraphicsContext gc, double portHeight) {
        Affine tOld = gc.getTransform();
        Affine t = new Affine();
        // set the transform to a new transform
        gc.transform(t);
        // transform into the new coordinate system so we can draw on that.
        gc.translate(0, portHeight - 1);
        gc.scale(scale, -scale); // second negative to reflect horizontally and draw from the bottom left
        gc.translate(-viewX, -viewY);

        boolean drawSEC = this.drawSEC.get(); // We'd ideally use JavaFX vector graphics, but taking a snapshot like this works too.
        boolean drawRadii = this.drawRadii.get(); // We'd ideally use JavaFX vector graphics, but taking a snapshot like this works too.
        boolean drawCoordinateSystems = this.drawCoordinateSystems.get(); // We'd ideally use JavaFX vector graphics, but taking a snapshot like this works too.

        // Pre-compute the smallest enclosing circle.
        Circle SEC = null;
        if (drawSEC || drawRadii) {
            List<Vector> robotPositions = Arrays.stream(robots).map(r -> r.pos).collect(Collectors.toList());
            SEC = SmallestEnclosingCircle.makeCircle(robotPositions);
        }

        if (drawSEC) {
            drawSEC(gc, SEC);
        }

        if (drawRadii) {
            for (Robot r : robots) {
                drawRobotToSECRadius(gc, drawSEC, SEC, r);
            }
        }

        // draw on the coordinate system
        for (Robot r : robots) {
            if (drawCoordinateSystems) {
                visualizeTransform(gc, r.pos, r.trans);
            }
            drawRobot(gc, r);
        }

        // transform back to the old transform
        gc.setTransform(tOld);
    }

    /**
     * Draw a legend in the top left corner of the robot view.
     */
    private void drawPTVisualizationLegend(GraphicsContext gc) {
        // draw legend for the coordinate system
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

    public void drawRobotToSECRadius(GraphicsContext gc, boolean drawSEC, Circle SEC, Robot r) {
        if (r.pos.equals(SEC.c)) return;
        gc.setLineWidth(0.03);
        gc.setStroke(Color.BLACK);
        Vector onCircle = drawSEC ? SEC.getPointOnCircle(r.pos) : r.pos;
        gc.strokeLine(onCircle.x, onCircle.y, SEC.c.x, SEC.c.y);
    }

    /**
     * Draw a black circle, in the style used to draw the SEC.
     */
    public void drawSEC(GraphicsContext gc, Circle SEC) {
        gc.setLineWidth(0.05);
        gc.setStroke(Color.BLACK);
        gc.strokeOval(SEC.c.x - SEC.r, SEC.c.y - SEC.r, 2 * SEC.r, 2 * SEC.r);
        gc.setFill(Color.BLACK);
        double centerR = 0.05;
        gc.fillOval(SEC.c.x - centerR, SEC.c.y - centerR, 2 * centerR, 2 * centerR);
    }

    /**
     * Draw a {@link Robot}, color-coding it based on current state.
     */
    private void drawRobot(GraphicsContext gc, Robot r) {
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(0.05);
        switch (r.state) { // Color-coded robot based on current state.
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

    /**
     * Draw a small coordinate system that visualizes the PositionTransformation centered on a given position.
     *
     * @param gc        The graphicscontext used to draw the axes.
     *
     * @param originAt  Where to center the origin of the axes.
     *
     * @param trans     The transformation to visualize.
     */
    private void visualizeTransform(GraphicsContext gc, Vector originAt, PositionTransformation trans) {
        Vector up = new Vector(0, 1);
        Vector right = new Vector(1, 0);
        Vector upGlobal = trans.localToGlobal(up, originAt);
        Vector rightGlobal = trans.localToGlobal(right, originAt);
        gc.setLineWidth(0.05);
        gc.setStroke(Color.RED);
        gc.strokeLine(originAt.x, originAt.y, upGlobal.x, upGlobal.y);
        gc.setStroke(Color.GREEN);
        gc.strokeLine(originAt.x, originAt.y, rightGlobal.x, rightGlobal.y);
    }

    /**
     * Draw the coordinate grid, note that it slides along with the dragging/scrolling.
     */
    private void drawGrid(GraphicsContext gc, double portHeight, double portWidth) {
        // calculate the starting position of the lines
        double LINE_SEP = 1;
        double lineX = LINE_SEP * Math.floor(viewX / LINE_SEP) - viewX;
        double lineY = LINE_SEP * Math.floor(viewY / LINE_SEP) - viewY;

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


    }

    /**
     * Zoom in on the mouse coordinates
     * @param mouseX the x coord of the mouse
     * @param mouseY the y coord of the mouse
     */
    public void zoomIn(double mouseX, double mouseY) {
        if (scale < MAX_SCALE) { // prevent scrolling further
            // calculate old coord
            Vector oldCoord = canvasToRobotCoords(new Vector(mouseX, mouseY));

            scale *= 1.5;

            // calculate new canvas coord
            Vector newCoord = canvasToRobotCoords(new Vector(mouseX, mouseY));

            // the difference should be added to the bottom left of the view
            viewX += oldCoord.x - newCoord.x;
            viewY += oldCoord.y - newCoord.y;
        }
    }

    /**
     * Zoom out on the mouse coordinates.
     * @param mouseX the x coord of the mouse
     * @param mouseY the y coord of the mouse
     */
    public void zoomOut(double mouseX, double mouseY) {
        if (scale > MIN_SCALE) {
            // calculate old coord
            Vector oldCoord = canvasToRobotCoords(new Vector(mouseX, mouseY));

            scale /= 1.5;

            // calculate new canvas coord
            Vector newCoord = canvasToRobotCoords(new Vector(mouseX, mouseY));

            // the difference should be added to the bottom left of the view
            viewX += oldCoord.x - newCoord.x;
            viewY += oldCoord.y - newCoord.y;
        }
    }

    /**
     * Convert a coordinate on the canvas to a coordinate in the coordinate system of the robots that is drawn
     * @param p the point to convert
     * @return the converted point
     */
    private Vector canvasToRobotCoords(Vector p) {
        double portHeight = canvas.getHeight();
        double xCoord = p.x / scale + viewX;
        double yCoord = ((p.y - (portHeight - 1)) / -scale) + viewY;
        return new Vector(xCoord, yCoord);
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

        if (cM != null) {
            cM.hide();
        }
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

    /**
     * Sets up the context menu for the canvas.
     */
    private void setUpContextMenu() {
        // setup the contextmenu
        setOnContextMenuRequested(e -> {
            // Only show if we can currently edit the robots.
            if (!robotManager.canEditRobots()) {
                System.err.println("Cannot currently edit the robots while the simulation is running.");
                return;
            }

            // check if we clicked on a robot
            Vector mouseClick = new Vector(e.getX(), e.getY());
            Robot picked = pickRobot(mouseClick);

            cM = new ContextMenu();
            cM.setAutoHide(true);

            // When clicked, add a new robot in the place that the context menu was opened at.
            MenuItem addRobotMenuItem = new MenuItem("Add robot");
            addRobotMenuItem.setOnAction(actionEvent -> {
                Robot[] localRobots = robotManager.getRobots();
                int maxID = Arrays.stream(localRobots).max((a, b) -> Integer.compare(a.id, b.id)).get().id;
                Robot newRobot = new Robot(maxID + 1, localRobots[0].algo, canvasToRobotCoords(mouseClick), localRobots[0].trans);
                if (!robotManager.canEditRobots()) {
                    System.err.println("Simulation was started before robot could be added.");
                    return;
                }
                robotManager.addRobot(newRobot);
            });

            // When clicked, remove a robot that is close to the clicked position.
            MenuItem removeRobotItem = new MenuItem("Remove robot");
            removeRobotItem.setOnAction(actionEvent -> {
                if (picked != null) { // there is a robot that we should remove
                    if (!robotManager.canEditRobots()) {
                        System.err.println("Simulation was started before robot could be removed.");
                        return;
                    }
                    robotManager.removeRobot(picked);
                }
            });

            cM.getItems().add(addRobotMenuItem);

            if (picked != null) {
                cM.getItems().add(removeRobotItem);
            }

            cM.show(this, e.getScreenX(), e.getScreenY());
        });
    }

    /**
     * Find the robot associated with the position clicked in mouse coordinates.
     *
     * @param mouseClick Where the user has clicked.
     * @return The {@link Robot} that was found, or null if none found.
     */
    Robot pickRobot(Vector mouseClick) {
        Vector clickedPosition = canvasToRobotCoords(mouseClick);
        return Arrays.stream(robotManager.getRobots())
                .filter(r -> Math.abs(r.pos.x - clickedPosition.x) < 0.5 && Math.abs(r.pos.y - clickedPosition.y) < 0.5)
                .findFirst()
                .orElse(null);
    }
}
