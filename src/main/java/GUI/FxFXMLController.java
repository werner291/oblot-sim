package GUI;

import Algorithms.Robot;
import Simulator.Simulator;
import javafx.animation.AnimationTimer;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.transform.Affine;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * The controller behind the {@link GUI}. The functions here define what happens when
 * the GUI is manipulated using button presses, slider changes etc.
 */
public class FxFXMLController
{
    @FXML
    // The reference of inputText will be injected by the FXML loader
    private ProgressBar progressBarSimulation;

    @FXML
    // The reference of inputText will be injected by the FXML loader
    private Button playButton;

    @FXML
    // The reference of inputText will be injected by the FXML loader
    private ScrollPane eventList;

    // The reference of outputText will be injected by the FXML loader
    @FXML
    private Slider dragBarSimulation;

    // location and resources will be automatically injected by the FXML loader
    @FXML
    private URL help;

    @FXML
    private ResourceBundle resources;

    // The canvas on which we can draw the robots and its corresponding graphics object
    @FXML
    private Canvas canvas;

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

    private Simulator simulator; // the simulator that will run the simulation.

    // Add a public no-args constructor
    public FxFXMLController()
    {
    }

    @FXML
    private void initialize()
    {
        // draw the canvas on a timer
        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                paintCanvas();
            }
        };
        timer.start();
    }

    public void setSimulator(Simulator sim) {
        this.simulator = sim;
    }

    @FXML
    private void onSave()
    {
        System.out.println("Save");
    }

    @FXML
    private void onLoad()
    {
        System.out.println("Load");
    }

    @FXML
    private void onClear()
    {
        System.out.println("Clear");
    }

    @FXML
    private void onQuit()
    {
        System.exit(0);
    }

    @FXML
    private void onFsync()
    {
        System.out.println("Fsync");
    }

    @FXML
    private void onSsync()
    {
        System.out.println("Ssync");
    }

    @FXML
    private void onAsync()
    {
        System.out.println("Async");
    }

    @FXML
    private void onFullshared()
    {
        System.out.println("Full Shared");
    }

    @FXML
    private void onOrienShared()
    {
        System.out.println("Orientation Shared");
    }

    @FXML
    private void onNothingShared()
    {
        System.out.println("Nothing Shared");
    }

    @FXML
    private void onGettingStarted()
    {
        System.out.println("Getting Started");
    }

    @FXML
    private void onAbout()
    {
        System.out.println("About");
    }

    @FXML
    private void playSimulation()
    {
        System.out.println("Playing Simulation");
//        progressBarSimulation.setProgress(100);
//
//        eventList.setContent(buildContent());
//        eventList.setPannable(true); // it means that the user should be able to pan the viewport by using the mouse.
//        playButton.setText("ReCompute");
        simulator.simulateTillNextEvent();
    }

    private Node buildContent() {
        VBox events = new VBox();
        events.getChildren().addAll(createEventButton(), createEventButton(), createEventButton(), createEventButton(), createEventButton(), createEventButton(),
                createEventButton(), createEventButton(), createEventButton(), createEventButton(), createEventButton(), createEventButton(), createEventButton(),
                createEventButton(), createEventButton(), createEventButton(), createEventButton(), createEventButton(), createEventButton(), createEventButton(),
                createEventButton(), createEventButton(), createEventButton(), createEventButton(), createEventButton(), createEventButton(), createEventButton(),
                createEventButton(), createEventButton(), createEventButton(), createEventButton(), createEventButton(), createEventButton(), createEventButton(),
                createEventButton(), createEventButton(), createEventButton(), createEventButton(), createEventButton(), createEventButton(), createEventButton());
        events.setSpacing(1);
        events.setPadding(new Insets(1));

        return events;
    }

    static private int eventNr = 0;
    private Button createEventButton() {
        Button eventButton = new Button("EVENT: " + eventNr);
        eventButton.setPrefWidth(150);
        eventButton.setMaxWidth(200);
        eventButton.setMinWidth(25);
        eventNr++;
        return eventButton;
    }

    /**
     * Draws a grid in the canvas based on the viewX, viewY and the scale
     */
    private void paintCanvas() {
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
            String label = "";
            if (viewX + lineX < 0) {
                label = String.valueOf((int)(viewX + lineX - 0.5));
            } else {
                label = String.valueOf((int)(viewX + lineX + 0.5));
            }
            gc.strokeText(label, (int) (lineX * scale) + 4, portHeight - 10);
            gc.strokeLine((int)((lineX * scale) + 0.5), 0, (int)((lineX * scale) + 0.5), portHeight);
            lineX += LINE_SEP;
        }

        // horizontal lines (from top to bottom, because 0 is at the top)
        while (lineY * scale <= portHeight) {
            String label = "";
            if (viewY + lineY < 0) {
                label = String.valueOf((int)(viewY + lineY - 0.5));
            } else {
                label = String.valueOf((int)(viewY + lineY + 0.5));
            }
            gc.strokeText(label, 10,  (int)portHeight - (lineY * scale) - 4);
            gc.strokeLine(0, (int)(portHeight - (lineY * scale) + 0.5), portWidth, (int)(portHeight - (lineY * scale) + 0.5));
            lineY += LINE_SEP;
        }

        Affine tOld = gc.getTransform();
        Affine t = new Affine();
        // set the transform to a new transform
        gc.transform(t);
        // transform into the new coordinate system so we can draw on that.
        gc.translate(0, portHeight - 1);
        gc.scale(scale, -scale); // second negative to reflect horizontally and draw from the bottom left
        gc.translate(-viewX, -viewY);

        // draw on the coordinate system
        Robot[] robots = simulator.getRobots();
        gc.setFill(Color.AQUA);
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(0.05);
        for (Robot r : robots) {
            double robotWidth = 0.5;
            gc.fillOval(r.pos.x - robotWidth/2, r.pos.y - robotWidth/2, robotWidth, robotWidth);
            gc.strokeOval(r.pos.x - robotWidth/2, r.pos.y - robotWidth/2, robotWidth, robotWidth);
        }

        // transform back to the old transform
        gc.setTransform(tOld);
    }

    /**
     * Zoom in on the mouse coordinates
     * @param mouseX the x coord of the mouse
     * @param mouseY the y coord of the mouse
     */
    public void zoomIn(double mouseX, double mouseY) {
        if (scale < MAX_SCALE) { // prevent scrolling further
            double portHeight = canvas.getHeight();
            double portWidth = canvas.getWidth();
            double dividableX = (portWidth / scale) / 3;
            double dividableY = (portHeight / scale) / 3;
            viewX += dividableX * (mouseX / portWidth);
            viewY += dividableY * (mouseY / portHeight);
            scale *= 1.5;
        }
    }

    /**
     * Zoom out on the mouse coordinates.
     * @param mouseX the x coord of the mouse
     * @param mouseY the y coord of the mouse
     */
    public void zoomOut(double mouseX, double mouseY) {
        if (scale > MIN_SCALE) {
            double portHeight = canvas.getHeight();
            double portWidth = canvas.getWidth();
            double dividableX = (portWidth / scale) / 3;
            double dividableY = (portHeight / scale) / 3;
            viewX -= dividableX * (mouseX / portWidth);
            viewY -= dividableY * (mouseY / portHeight);
            scale /= 1.5;
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
        int notches = ((int)deltaY / 40);
        if (notches > 0) {
            for(int i=0; i<notches; i++){
                zoomIn(e.getX(),e.getY());
            }
        } else {
            for(int i=0; i>notches; i--){
                zoomOut(e.getX(),e.getY());
            }
        }
    }
}
