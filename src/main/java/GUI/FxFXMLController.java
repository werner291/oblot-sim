package GUI;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import Algorithms.Robot;
import Algorithms.State;
import Schedulers.CalculatedEvent;
import Schedulers.Event;
import Simulator.Simulator;
import Util.Interpolate;
import Util.Vector;
import javafx.animation.AnimationTimer;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.transform.Affine;
import javafx.stage.Popup;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

/**
 * The controller behind the {@link GUI}. The functions here define what happens when
 * the GUI is manipulated using button presses, slider changes etc.
 */
public class FxFXMLController
{
    boolean isPaused = true;
    double playBackSpeed = 0.01;
    int last_size_calc_events = 0;

    @FXML
    // The reference of inputText will be injected by the FXML loader
    private ProgressBar progressBarSimulation;

    @FXML
    // The reference of inputText will be injected by the FXML loader
    private Button playButton;
    @FXML
    private Button nextButton;
    @FXML
    private Button endButton;

    @FXML
    // The reference of inputText will be injected by the FXML loader
    private ScrollPane eventList;
    private VBox eventsVBox = new VBox();

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

    @FXML
    private AnchorPane canvasBackground;

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
                recomputeRobots(dragBarSimulation.getValue());
                paintCanvas();

                if (!isPaused) {
                    playDragBar();
                }
            }
        };
        timer.start();

        // set the values for the events scrollpane
        eventsVBox.setSpacing(1);
        eventsVBox.setPadding(new Insets(1));

        // Setup dragSimulationBar
        dragBarSimulation.valueProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
//                paintCanvas(recomputeRobots(newValue.doubleValue()));
            }
        });
    }

    private Popup warningPopup = new Popup();
    private void showWarningPopUp(String warning) {
        Label label = new Label(warning);
        Button button = new Button("OK");
        warningPopup.getContent().add(label);
        warningPopup.getContent().add(button);
        label.setMinWidth(80);
        label.setMinHeight(50);
        label.setStyle(" -fx-background-color: white;");
        button.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                hideWarningPopUp();
            }
        });

        warningPopup.show(GUI.stage);

    }

    private void hideWarningPopUp() {
        warningPopup.hide();
    }

    private void playDragBar() {
        // Get list of computed events
        List<CalculatedEvent> calculatedEvents = simulator.getCalculatedEvents();
        if (calculatedEvents.size() == 0) {
            showWarningPopUp("Nothing has been simulated yet");
            isPaused = true;
            playButton.setText("Play");
            return;
        }
        List<Event> recentEvents = calculatedEvents.get((simulator.getCalculatedEvents().size()-1)).events;

        // Add recent events to Vbox containing all events
        double recentTimeStamp = 0;
        for (Event calculatedEvent : recentEvents) {
            recentTimeStamp = calculatedEvent.t;
        }

        // Redraw robot positions
        double simulationTime = dragBarSimulation.valueProperty().get();
        if (simulationTime < recentTimeStamp) {
            dragBarSimulation.valueProperty().set(simulationTime + playBackSpeed);
        } else {
            isPaused = true;
            playButton.setText("Play");
        }
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
    private void onDragDetected() {
        isPaused = true;
        playButton.setText("Play");
    }

    /**
     * Called whenever the Next Event button is pressed
     */
    @FXML
    private void nextSimulation() {
//        System.out.println("Next event");
        hideWarningPopUp();

        // Compute next events
        progressBarSimulation.setProgress(0);
        simulator.simulateTillNextEvent();

        // Get list of computed events
        List<CalculatedEvent> calculatedEvents = simulator.getCalculatedEvents();

        // Check if an additional event was added. If not, then don't add anything to the list
        if (calculatedEvents.size() == last_size_calc_events) {
            showWarningPopUp("No new events have been found by the simulator");
            return;
        }
        last_size_calc_events = calculatedEvents.size();

        List<Event> recentEvents = calculatedEvents.get((simulator.getCalculatedEvents().size()-1)).events;
        progressBarSimulation.setProgress(50);

        // Add recent events to Vbox containing all events
        double recentTimeStamp = 0;
        for (Event calculatedEvent : recentEvents) {
            eventsVBox.getChildren().add(createEventButton(calculatedEvent.type.toString(), calculatedEvent.t));
            recentTimeStamp = calculatedEvent.t;
        }
        progressBarSimulation.setProgress(75);
        eventList.setContent(eventsVBox);
        dragBarSimulation.setMax(recentTimeStamp);
        dragBarSimulation.setValue(recentTimeStamp);

        // Redraw robot positions
        progressBarSimulation.setProgress(100);
    }

    /**
     * Starts and pauses automatic playback, stops when it reaches the end of what has currently been computed
     */
    @FXML
    private void playSimulation() {
        isPaused = !isPaused;
        if (isPaused) playButton.setText("Play");
        else playButton.setText("Pause");
    }

    /**
     * TODO: Computes until all the robots have stopped moving and are sleeping, might never finish
     */
    @FXML
    private void endSimulation() {
    }

    /**
     * Create a button for in the event scrollpane
     * @param eventName Name of the event, probably the type of event
     * @param timeStamp Timestamp of when the event took place
     * @return The object that can be clicked by the user to return to a certain timestamp/event
     */
    private Button createEventButton(String eventName, double timeStamp) {
        EventButton eventButton = new EventButton(eventName + " | @: " + timeStamp, timeStamp);
        eventButton.prefWidthProperty().bind(eventList.widthProperty());
        eventButton.setOnAction(eventButtonHandler);
        return eventButton;
    }

    /**
     * Recompute robot location at certain timestamps. Never modifies data given by the simulator. Uses its local robots
     * object. Computes where they would have been using the computedevents list stored by the simulator.
     * TODO: Depends on the simulator's stored robots object to figure out how many robots there are. So don't remove robots
     * from the list! Could perhaps be done more robustly.
     * @param timestamp Timestamp of the simulation to display on the canvas
     */

    private void recomputeRobots(double timestamp) {
        List<CalculatedEvent> calculatedEvents = simulator.getCalculatedEvents();
        if (calculatedEvents.size() == 0) {
            return;
        }

        // Select the Last event at the given timestep in the simulation
        short indexOfCalcEvents = 0;
        CalculatedEvent prevEvent = null;
        CalculatedEvent nextEvent = null;
        for (int i = 0; i <= calculatedEvents.size(); i++) {
            // If the next event is not yet known don't assign any next event
            if (i == calculatedEvents.size()) {
                prevEvent = calculatedEvents.get(i-1);
                nextEvent = prevEvent;
                break;
            }

            double calculatedEventsTime = calculatedEvents.get(i).events.get(0).t;
            // The event with this timestamp happened after the selected timestamp
            if (calculatedEventsTime > timestamp)
            {
                // If there is no previous event simply use the first event, only occurs when the first timestamp an
                // event occurs is selected to be simulated. Otherwise pick the most recent event
                prevEvent = calculatedEvents.get(Math.max(0, indexOfCalcEvents-1));
                nextEvent = calculatedEvents.get(indexOfCalcEvents);

                // Stop after finding first candidate
                break;
            }

            indexOfCalcEvents++;
        }

        Robot[] robots = simulator.getRobots();

        short robotIndex = 0;
        for (Robot robot : robots) {
            Vector startPos = prevEvent.positions[robotIndex];
            double startTime = prevEvent.events.get(0).t;
            Vector endPos = nextEvent.positions[robotIndex];
            double endTime = nextEvent.events.get(0).t;

            switch (prevEvent.events.get(robotIndex).type) {
                case END_MOVING:
                    robot.state = State.SLEEPING;
                    break;
                case START_COMPUTE:
                    robot.state = State.COMPUTING;
                    break;
                case START_MOVING:
                    robot.state = State.MOVING;
                    break;
            }

            if (startTime == endTime) robot.pos = endPos;
            else if (endTime < timestamp) { robot.pos = endPos; }
            else { robot.pos = Interpolate.linearInterpolate(startPos, startTime, endPos, endTime, timestamp); }

            robotIndex++;
        }


//        short robotIndex = 0;
//        for (Robot robot : robots) {
//            robot = robotsSim[robotIndex].copy();
//
//            Vector startPos = prevEvent.positions[robotIndex];
//            double startTime = prevEvent.events.get(0).t;
//            Vector endPos = nextEvent.positions[robotIndex];
//            double endTime = nextEvent.events.get(0).t;
//            switch (prevEvent.events.get(robotIndex).type){
//                case END_MOVING:
//                    robot.state = State.SLEEPING;
//                    break;
//                case START_COMPUTE:
//                    robot.state = State.COMPUTING;
//                    break;
//                case START_MOVING:
//                    robot.state = State.MOVING;
//                    break;
//            }
//
//            // In case the robot didn't move
//            if (startTime == endTime) robot.pos = endPos;
//            // Else interpolate
//            else {
//                robot.pos = Interpolate.linearInterpolate(startPos, startTime, endPos, endTime, timestamp);
//            }
//
//            robots[robotIndex] = robot;
//            robotIndex++;
//        }
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
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(0.05);
        Robot[] robots = simulator.getRobots();
        for (Robot r : robots) {
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
            gc.fillOval(r.pos.x - robotWidth/2, r.pos.y - robotWidth/2, robotWidth, robotWidth);
            gc.strokeOval(r.pos.x - robotWidth/2, r.pos.y - robotWidth/2, robotWidth, robotWidth);
        }

        // transform back to the old transform
        gc.setTransform(tOld);

        canvasBackground.widthProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                canvas.widthProperty().setValue(newValue);
//                System.out.println("Width: " + newValue.doubleValue());
            }
        });

        canvasBackground.heightProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                canvas.heightProperty().set(newValue.doubleValue() - dragBarSimulation.getHeight());
//                System.out.println("Height: " + newValue.doubleValue());
            }
        });
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

    private EventHandler<ActionEvent> eventButtonHandler = event -> {
        if (!event.getSource().getClass().equals(EventButton.class)) {
            throw new IllegalArgumentException("Event button trigger by: " + event.getSource().getClass() +
                    " was not an event button, please only use this eventHandler for EventButtons");
        }
        EventButton eventButton = (EventButton) event.getSource();
        dragBarSimulation.setValue(eventButton.getTimeStamp());
        event.consume();
    };
}
