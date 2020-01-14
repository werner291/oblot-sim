package GUI;

import Algorithms.Algorithm;
import PositionTransformations.RotationTransformation;
import RobotPaths.RobotPath;
import Schedulers.*;
import Util.Circle;
import Util.SmallestEnclosingCircle;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import Simulator.Simulator;
import Simulator.Robot;
import Simulator.State;
import Util.Vector;
import javafx.animation.AnimationTimer;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.Popup;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URL;
import java.util.*;
import java.util.function.Supplier;

/**
 * The controller behind the {@link GUI}. The functions here define what happens when
 * the GUI is manipulated using button presses, slider changes etc.
 */
public class FxFXMLController implements RobotView.RobotManager
{
    public CheckBox interruptableToggle;
    public TextField visibilityTextBox;
    public CheckBox multiplicityToggle;
    public CheckBox infiniteVisibilityToggle;
    boolean isScheduleDone = false;
    boolean isDoneSimulating = false;

    // Global variable that's set to true once the GUI has padded the last moving robot with a path.
    boolean paddedLastEvent = false;

    // Global variable that is set to false for automatic playing of the drag bar in the bottom.
    boolean isPaused = true;

    // Global variable that is set to true if the GUI should keep simulating,
    // is automatically set to false once endtime has been reached as chosen in the GUI
    boolean simulatingTillEnd = false;

    // Global variable that should be set to true if the eventlist should be
    // rebuild as a result of changing a past event
    boolean resetEvents = true;

    double lastframeTime = 0;
    double frameRate = 60;
    double frameTimeMillis = 1000/frameRate;

    double lastsimTime = 0;
    double simRate = 10;
    double simTimeMillis = 1000/simRate;

    int playBackSpeed = 2;
    int timeToEndSimulation = 100;
    int last_size_calc_events = 0;

    @FXML
    // The reference of inputText will be injected by the FXML loader
    private ProgressBar progressBarSimulation;

    @FXML
    private TextField playBackSpeedLabel;

    @FXML
    private Label statusLabel;

    @FXML
    private TextField timeToEndSimulationTextField;

    @FXML
    public MenuItem frameRateMenuItem;
    @FXML
    public Slider frameRateMenuSlider;

    @FXML
    // The reference of inputText will be injected by the FXML loader
    private Button playButton;
    @FXML
    private Button nextButton;
    @FXML
    private Button endButton;

    @FXML
    private ScrollPane eventList;
    // The reference of inputText will be injected by the FXML loader
    private VBox eventsVBox;

    @FXML
    private ScrollPane algorithmsList;
    private VBox algorithmsVBox = new VBox();

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
    private RobotView robotView;

    @FXML
    private AnchorPane canvasBackground;

    @FXML
    private CheckMenuItem chiralityAxisButton;
    @FXML
    private CheckMenuItem unitLengthAxisButton;
    @FXML
    private CheckMenuItem rotationAxisButton;

    @FXML
    private CheckMenuItem drawCoordinateSystemsButton;
    @FXML
    private CheckMenuItem drawSECButton;
    @FXML
    private CheckMenuItem drawRadiiButton;


    private String lastSelectedScheduler;

    private boolean drawCoordinateSystems = false;
    private boolean drawSEC = false;
    private boolean drawRobotLabel = true;
    private boolean drawRadii = false;

    private Simulator simulator; // the simulator that will run the simulation.
    private Class[] algorithms; // the list of possible algorithms

    private Robot[] localRobots;

    // Add a public no-args constructor
    public FxFXMLController()
    {
    }

    @FXML
    private void initialize()
    {
        robotView.setRobotManager(this);

        // draw the canvas on a timer
        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                // Prevents trying to draw the simulator before it's fully initialized. (race condition)
                if (simulator != null && System.currentTimeMillis() > lastframeTime + frameTimeMillis) {
                    lastframeTime = System.currentTimeMillis();
                    recomputeRobots(dragBarSimulation.getValue());
                    robotView.paintCanvas();
                }

                if (System.currentTimeMillis() > lastsimTime + simTimeMillis) {
                    lastsimTime = System.currentTimeMillis();
                    // If the bar is playing increment it
                    if (!isPaused) {
                        recomputeRobots(dragBarSimulation.getValue());
                        playDragBar();
                    }
                }

                // If the simulation is simulating till a certain timestamp then follow that
                if (simulatingTillEnd) {
                    List<CalculatedEvent> calculatedEvents = simulator.getCalculatedEvents();

                    // If no events exist yet atleast simulate one event
                    if (calculatedEvents.size() == 0) {
                        simulateNextEvent();
                        calculatedEvents = simulator.getCalculatedEvents();
                    }

                    // Most recent Event
                    Event recentEvent = calculatedEvents.get(calculatedEvents.size()-1).events.get(0);

                    // Stop early at he max specified time given in the GUI
                    if (recentEvent.t < timeToEndSimulation) {
                        simulateNextEvent();
                    } else {
                        // Else stop simulating, goal has been reached
                        endButton.setText("End:");
                        playButton.setDisable(false);
                        nextButton.setDisable(false);
                        simulatingTillEnd = false;
                    }

                    // Update progressbar
                    progressBarSimulation.setProgress((recentEvent.t / timeToEndSimulation));
                    int timeToDisplay = (int)(recentEvent.t*100);
                    float timeToDisplayFloat = (float)(timeToDisplay)/100;
                    statusLabel.setText("Computing: " + timeToDisplayFloat + "/" + timeToEndSimulation);
                }
            }
        };
        timer.start();

        playBackSpeedLabel.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                try {
                    playBackSpeed = Integer.parseInt(playBackSpeedLabel.getText());
                } catch (NumberFormatException e) {
                    playBackSpeedLabel.setText(""+playBackSpeed);
                }
            }
        });

        // Listen to the user picking times on the event list sidebar.
//        eventList.timePickedCB.setValue(dragBarSimulation::setValue);
        timeToEndSimulationTextField.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                try {
                    timeToEndSimulation = Integer.parseInt(timeToEndSimulationTextField.getText());
                } catch (NumberFormatException e) {
                    timeToEndSimulationTextField.setText(""+playBackSpeed);
                }
            }
        });

        frameRateMenuItem.setText("FrameRate: "+frameRate);
        frameRateMenuSlider.valueProperty().setValue(frameRate);
        frameRateMenuSlider.valueProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number number, Number t1) {
                frameRate = t1.floatValue();
                frameTimeMillis = 1000/frameRate;
                frameRateMenuItem.setText("FrameRate: " + t1.intValue());
            }
        });

        // set the values for the events scrollpane
        algorithmsVBox.setSpacing(1);
        algorithmsVBox.setPadding(new Insets(1));

        // set the canvas to listen to the size of its parent
        canvasBackground.widthProperty().addListener((ov, oldValue, newValue) -> {
            robotView.setWidth(newValue.doubleValue() - 30);
        });
        canvasBackground.heightProperty().addListener((ov, oldValue, newValue) -> {
            robotView.setHeight(newValue.doubleValue() - dragBarSimulation.getHeight());
        });

        robotView.drawCoordinateSystems.bind(drawCoordinateSystemsButton.selectedProperty());
        robotView.drawSEC.bind(drawSECButton.selectedProperty());
        robotView.drawRadii.bind(drawRadiiButton.selectedProperty());
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

    public boolean canEditRobots() {
        return isPaused;
    }

    @Override
    public Robot[] getRobots() {
        return localRobots;
    }

    public double getTime() {
        return dragBarSimulation.getValue();
    }

    public Scheduler getScheduler() {
        return simulator.scheduler;
    }

    public void removeRobot(Robot toRemove) {
        Robot[] copy = new Robot[localRobots.length - 1];
        int indexInCopy = 0;
        for (Robot localRobot : localRobots) {
            if (localRobot != toRemove) {
                copy[indexInCopy] = localRobot;
                indexInCopy++;
            }
        }
        localRobots = copy;
        Arrays.stream(localRobots).forEach(r -> r.state = State.SLEEPING);
        dragBarSimulation.setValue(0);
        simulator.setState(localRobots, new ArrayList<>(), 0);
    }

    public void addRobot(Robot newRobot) {
        Robot[] copy = new Robot[localRobots.length + 1];
        System.arraycopy(localRobots, 0, copy, 0, localRobots.length);
        copy[copy.length - 1] = newRobot;
        localRobots = copy;
        Arrays.stream(localRobots).forEach(r -> r.state = State.SLEEPING);
        dragBarSimulation.setValue(0);
        simulator.setState(localRobots, new ArrayList<>(), 0);
    }

    private void hideWarningPopUp() {
        warningPopup.hide();
    }

    private void playDragBar() {
        // Get list of computed events
        List<CalculatedEvent> calculatedEvents = simulator.getCalculatedEvents();
        if (calculatedEvents.size() == 0) {
            simulateNextEvent();
            return;
        }
        List<Event> recentEvents = calculatedEvents.get((calculatedEvents.size()-1)).events;

        double recentTimeStamp = recentEvents.get(0).t;

        // Redraw robot positions
        double simulationTime = dragBarSimulation.valueProperty().get();
        if (simulationTime == recentTimeStamp && playBackSpeed < 0) dragBarSimulation.valueProperty().set(simulationTime + ((double)playBackSpeed/100));
        else if (simulationTime == 0 && playBackSpeed > 0) dragBarSimulation.valueProperty().set(simulationTime + ((double)playBackSpeed/100));
        else if (simulationTime < recentTimeStamp && simulationTime > 0) dragBarSimulation.valueProperty().set(simulationTime + ((double)playBackSpeed/100));
        else if (simulationTime >= recentTimeStamp && playBackSpeed > 0)
        {
            if (!simulateNextEvent()) {
                isPaused = true;
                playButton.setText("Play");
            }
            dragBarSimulation.valueProperty().set(simulationTime + ((double)playBackSpeed/100));
        }
        else {
            isPaused = true;
            playButton.setText("Play");
        }
    }

    public void setSimulator(Simulator sim) {
        this.simulator = sim;
        localRobots = simulator.getRobots();

        if (sim.config.visibility == -1.0) {
            infiniteVisibilityToggle.setSelected(true);
            visibilityTextBox.setDisable(true);
        }else {
            visibilityTextBox.setText(Double.toString(sim.config.visibility));
        }
        multiplicityToggle.setSelected(sim.config.multiplicity);
        interruptableToggle.setSelected(sim.config.interuptable);

    }

    public void setAlgorithms(Class[] algorithms) {
        // set all algorithms
        this.algorithms = algorithms;
        for (Class a : algorithms) {
            if (!Algorithm.class.isAssignableFrom(a)) { // try to cast algorithm to his subclass
                System.err.println("Class " + a + " is not a subclass of Algorithm.");
                continue;
            }
            Button algorithmButton = new Button(a.getSimpleName());
            algorithmButton.prefWidthProperty().bind(algorithmsList.widthProperty());
            algorithmButton.setOnAction(algorithmButtonHandler);
            algorithmsVBox.getChildren().add(algorithmButton);
        }
        algorithmsList.setContent(algorithmsVBox);
    }

    @FXML
    private void onSaveRun()
    {
        final FileChooser fc = new FileChooser();

        File file = fc.showSaveDialog(null);
        if (file != null) {
            CalculatedEvent.toFile(file, simulator.getCalculatedEvents(), localRobots);
        }
    }

    @FXML
    private void onSaveRobots()
    {
        final FileChooser fc = new FileChooser();

        File file = fc.showSaveDialog(null);
        if (file != null) {
            try {
                Robot.toFile(file, localRobots);
            } catch (FileNotFoundException e) {
                new Alert(Alert.AlertType.ERROR, e.getMessage()).show();
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void onLoadRun()
    {
        final FileChooser fc = new FileChooser();
        File file = fc.showOpenDialog(null);
        if (file != null) {
            List<CalculatedEvent> events = CalculatedEvent.fromFile(file);
            simulator.setState(simulator.robots, events, 0.0);
            dragBarSimulation.setMax(events.get(events.size()-1).getTimestamp());
            dragBarSimulation.setValue(events.get(events.size()-1).getTimestamp());
            regenerateEventList(simulator.getCalculatedEvents(), true);
        }
        System.out.println("Load");
    }

    @FXML
    private void onLoadRobots()
    {

        // Only prompt if there is anything to be saved/discarded.
        if (! simulator.calculatedEvents.isEmpty()) {
            // Prompt the user to optionally save the current run.
            // Button to save the run.
            ButtonType saveRunButtonType = new ButtonType("Save run", ButtonBar.ButtonData.YES);
            // Button to discard the run.
            ButtonType discardButtonType = new ButtonType("Discard run", ButtonBar.ButtonData.NO);
            // Button to stop loading robots.
            ButtonType cancelButtonType = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);

            Alert alert = new Alert(Alert.AlertType.NONE,
                    "Loading a new set of robots will clear the simulation state. Do you wish to save the current run?",
                    saveRunButtonType, discardButtonType, cancelButtonType);

            // Hack to avoid a bug where the dialog has almost 0 size.
            alert.setResizable(true);
            alert.getDialogPane().setPrefSize(480, 320);

            Optional<ButtonType> result = alert.showAndWait();

            if (result.isPresent() && result.get() == saveRunButtonType) {
                // If the user used the "Save Run" button, trigger the onSaveRun event handler as if the user clicked that menu option.
                // That is what they would have done anyway if they'd canceled the robot load action to save their current run.
                onSaveRun();
            } else if (result.isEmpty() || result.get() == cancelButtonType) {
                return;
            }
        }

        final FileChooser fileChooser = new FileChooser();

        File robotsFile = fileChooser.showOpenDialog(null);

        if (robotsFile != null) {
            Robot[] robots = Robot.fromFile(simulator.getRobots()[0].getAlgorithm(), RotationTransformation.IDENTITY, robotsFile);

            /*empty*/
            simulator.setState(robots, List.of(/*empty*/), 0.0);
            this.localRobots = simulator.getRobots();
            regenerateEventList(simulator.getCalculatedEvents(), true);
        }
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
        endButton.setDisable(false);
        nextButton.setDisable(false);
    }

    /**
     * Called whenever the Next Event button is pressed
     * If a user presses next whilst browsing history, clear the stack of calcevents and reset the sim back to that time
     * So the simulation is done again from the nextevent onwards
     */
    @FXML
    private void nextSimulation() {
        // If called whilst browsing history, reset the future
        if (dragBarSimulation.getValue() < dragBarSimulation.getMax()) {
            resetSimulation();
            simulateNextEvent();
        }
        // Should not be possible, added just in case
        else if (dragBarSimulation.getValue() > dragBarSimulation.getMax()) {
            dragBarSimulation.setValue(dragBarSimulation.getMax());
            System.err.println(dragBarSimulation.getValue() + " surpassed max: " + dragBarSimulation.getMax());
        }
        // Else if we're at the max already just simulate without resetting anything
        else {
            simulateNextEvent();
        }
    }

    /**
     * Starts and pauses automatic playback, stops when it reaches the end of what has currently been computed
     */
    @FXML
    private void playSimulation() {
        // If called whilst browsing history, reset the future, only when starting to play, not pausing
        if (dragBarSimulation.getValue() < dragBarSimulation.getMax() && isPaused) {
            resetSimulation();
        }

        // toggle the isPaused global variable also set the buttontext and disable the end/nextbuttons
        isPaused = !isPaused;
        if (isPaused) {
            playButton.setText("Play");
            endButton.setDisable(false);
            nextButton.setDisable(false);
        }
        else {
            playButton.setText("Pause");
            endButton.setDisable(true);
            nextButton.setDisable(true);
        }
    }

    /**
     * Helper function that resets the simulation to the current dragbar timestamp or the given timestamp if it is given
     */
    private void resetSimulation(double localTimeStamp) {

        // get most recent event for given timestamp
        CalculatedEvent[] events = gatherRecentEvents(localTimeStamp);
        List<CalculatedEvent> newList;
        if (events == null || events[0] == null) {
            System.err.println("Could not find most recent event");
            newList = new ArrayList<>();
        } else {
            // Reset Calculated Events to given timestamp
            CalculatedEvent latestEvent = events[0];
            newList = removeInvalidCalcEvents(simulator.calculatedEvents, latestEvent);
        }

        // Reset robots to given timestamp
        recomputeRobots(localTimeStamp);

        // Set the reset robots and calculatedevents to the state of the sim
        simulator.setState(localRobots, newList, localTimeStamp);
        regenerateEventList(simulator.getCalculatedEvents(), true);

        // Reset the simulation drag bar as well
        dragBarSimulation.setMax(localTimeStamp);
        dragBarSimulation.setValue(dragBarSimulation.getMax());

        // Reset global variable used to check if a simulateNext() call actually adds a new event
        last_size_calc_events = 0;

        // Set global variable which will enforce the list of events on the left to be recomputed from scratch
        resetEvents = true;
        isDoneSimulating = false;
        isScheduleDone = false;
        paddedLastEvent = false;

        isPaused = true;
        playButton.setText("Play");
        nextButton.setDisable(false);
        endButton.setDisable(false);
    }

    /**
     * Helper function that resets the simulation to the current dragbar timestamp or the given timestamp if it is given
     */
    private void resetSimulation() {
        // Get current selected timestamp
        resetSimulation(dragBarSimulation.getValue());
    }

    /**
     * Returns whether or not every robot has reached their goal
     * @param calculatedEvents list of all events until now, used to check if the last event made the robots reach their goals
     * @return true if the robots have all reached their goal, false if one or more have not
     */
    private boolean checkIsDoneSimulating(List<CalculatedEvent> calculatedEvents) {
        if (calculatedEvents.size() == 0) {
            System.err.println("No events have occurred yet");
            return false;
        }
        CalculatedEvent lastEvent = calculatedEvents.get(calculatedEvents.size()-1);

        for (int i = 0; i < lastEvent.events.size(); i++) {
            if (lastEvent.positions[i] != lastEvent.robotPaths[i].end) return false;
        }

        return true;
    }

    /**
     * Called to gather the new events and add them to the eventlist, set the new timestep
     * @return true if the next event exists, false if it does not
     */
    private boolean simulateNextEvent() {
        // Start simulating the next event
        hideWarningPopUp();

        // Compute next events
        progressBarSimulation.setProgress(0);
        statusLabel.setText("Computing Next Event");

        try {
            simulator.simulateTillNextEvent();
        } catch (Exception e) {
            e.printStackTrace();
        }
        progressBarSimulation.setProgress(0.25);

        // Get list of computed events
        List<CalculatedEvent> calculatedEvents = simulator.getCalculatedEvents();

        progressBarSimulation.setProgress(0.5);
        // Check if an additional event was added. If not, then don't add anything to the list
        if (calculatedEvents.size() == last_size_calc_events || isScheduleDone) {
            isDoneSimulating = checkIsDoneSimulating(calculatedEvents);
            isScheduleDone = true;
            showWarningPopUp("No new event was generated");
            return false;
        }
        last_size_calc_events = calculatedEvents.size();
        progressBarSimulation.setProgress(0.75);

        // Rebuild the Vbox with all events
        regenerateEventList(calculatedEvents, resetEvents);

        double recentTimeStamp = calculatedEvents.get(calculatedEvents.size()-1).getTimestamp();
        dragBarSimulation.setMax(recentTimeStamp);
        dragBarSimulation.setValue(dragBarSimulation.getMax());
        progressBarSimulation.setProgress(1);
        statusLabel.setText("Idle");

        return true;
    }

    /**
     * Build EventList on the left
     * @param calculatedEvents Events to draw onto the eventlist, sometimes only takes the most recent event and adds it
     * @param rebuild If true, rebuild the whole eventlist from scratch, necessary after changing anything whilst browsing history
     *                if false just add the most recent event to the vbox, far more performant.
     */
    private void regenerateEventList(List<CalculatedEvent> calculatedEvents, boolean rebuild) {
        // Should completely rebuild or just add recent?
        if (rebuild) {
            List<Event> allEvents = new ArrayList<>();
            calculatedEvents.forEach(e -> allEvents.addAll(e.events));
            eventsVBox = new VBox();
            for (Event event : allEvents) {
                eventsVBox.getChildren().add(createEventButton(event.r.id + 1, event.type.toString(), event.t));
            }

            // Reset from scratch now, just add onto it from now on, until it needs to be reset again
            resetEvents = false;
        } else {
            CalculatedEvent recentEvent = calculatedEvents.get(calculatedEvents.size()-1);
            for (Event event : recentEvent.events) {
                eventsVBox.getChildren().add(createEventButton(event.r.id + 1, event.type.toString(), event.t));
            }
        }

        eventList.setContent(eventsVBox);
    }

    /**
     * Create a button for in the event scrollpane
     * @param eventName Name of the event, probably the type of event
     * @param timeStamp Timestamp of when the event took place
     * @return The object that can be clicked by the user to return to a certain timestamp/event
     */
    private Button createEventButton(int robotnr, String eventName, double timeStamp) {
        EventButton eventButton = new EventButton( "Robot: " + robotnr + " | " + eventName + " | @: " + timeStamp, timeStamp);
        eventButton.prefWidthProperty().bind(eventList.widthProperty());
        eventButton.setOnAction(eventButtonHandler);
        return eventButton;
    }

    private List<CalculatedEvent> removeInvalidCalcEvents(List<CalculatedEvent> calculatedEvents, CalculatedEvent latestEvent) {
        List<CalculatedEvent> tempCalcEvents = new ArrayList<>();

        for (CalculatedEvent calculatedEvent : calculatedEvents) {
            tempCalcEvents.add(calculatedEvent);
            if (calculatedEvent.events.get(0).t == (latestEvent.events.get(0).t)) break;
        }

        return tempCalcEvents;
    }

    @FXML
    private void forwardAction() {
        playBackSpeed += 1;
        playBackSpeedLabel.setText(""+playBackSpeed);
    }

    @FXML
    private void backwardsAction() {
        playBackSpeed -= 1;
        playBackSpeedLabel.setText(""+playBackSpeed);
    }

    /**
     * TODO: Computes until all the robots have stopped moving and are sleeping, might never finish
     */
    @FXML
    private void endSimulation() {
        // Whilst the robots have not reached their path
        simulatingTillEnd = !simulatingTillEnd;

        if (simulatingTillEnd) {
            endButton.setText("Stop");
            playButton.setDisable(true);
            nextButton.setDisable(true);
        }
        else {
            endButton.setText("End:");
            playButton.setDisable(false);
            nextButton.setDisable(false);
        }
    }

    /**
     * Return the index number of a robot robotToMatch
     * @param robotToMatch, robot to find the correct index number for to match CalculatedEvent.positions/goals with
     * @return index used to retrieve the position and goals of a robot with
     */
    private int getRobotIndex(Robot robotToMatch){
        int robotIndex = 0;

        for (Robot robot : localRobots) {
            if (robotToMatch.equals(robot)) return robotIndex;
            robotIndex++;
        }

        return robotIndex;
    }

    /**
     * Compute the position and state of robots from a timestamp in the past
     * @param timestamp Timestamp of the simulation to display on the canvas
     */

    private void recomputeRobots(double timestamp) {
        // Gather the most recent and the next event if available
        CalculatedEvent[] eventsFound = gatherRecentEvents(timestamp);
        // If neither can be found nothing has been simulated yet don't recompute their positions
        if (eventsFound == null) return;

        // Unpack the previous and next events from the helper function
        CalculatedEvent currentEvent = eventsFound[0];
        CalculatedEvent nextEvent = eventsFound[1];

        if (currentEvent == null) { // If the next event is the first event, make up the prev event as sleeping until the first event.
            currentEvent = nextEvent.copyDeep();
            for (Event event : currentEvent.events) {
                event.t = 0;
                event.type = EventType.END_MOVING;
            }
        }
        if (nextEvent == null) { // If the last event is the prev event, make up the next event until sleep.
            nextEvent = currentEvent.copyDeep();

            // If no more calculatedevents came up and we haven't finished padding till all robots stop do this
            if (!isDoneSimulating && isScheduleDone && !paddedLastEvent) {
                double startTime = currentEvent.events.get(0).t;
                double endTime = currentEvent.events.get(0).t + 1;
                double maxEndTime = 0;

                List<Event> newlistofEvents = new ArrayList<>();

                for (Robot robot : localRobots) {
                    int robotIndexTemp = getRobotIndex(robot);
                    RobotPath nextRobotPath = nextEvent.robotPaths[robotIndexTemp];

//                    if (simulator.scheduler.getClass() == AsyncScheduler.class) {
                    endTime = nextRobotPath.getEndTime(startTime, robot.speed);
                    if (endTime > maxEndTime) maxEndTime = endTime;
//                    }

                    // If robots have started stopped moving, but are not yet at their goal start computing next round.
                    if (robot.state.equals(State.MOVING)) {
                        Event finalRobotEvent = new Event(EventType.END_MOVING, endTime, robot);
                        newlistofEvents.add(finalRobotEvent);
                        nextEvent.positions[robotIndexTemp] = nextRobotPath.end;
                    }

                }

                nextEvent.events = newlistofEvents;
                simulator.calculatedEvents.add(nextEvent);
                regenerateEventList(simulator.calculatedEvents, false);
                paddedLastEvent = true;

                dragBarSimulation.setMax(maxEndTime);
                dragBarSimulation.valueProperty().setValue(maxEndTime);
            }
        }

        // Change robots for the draw function
        for (Event nextRobotEvent : nextEvent.events) {
            int robotIndex = getRobotIndex(nextRobotEvent.r);
            Robot robot = localRobots[robotIndex];

            double startTime = currentEvent.events.get(0).t;
            Vector endPos = nextEvent.positions[robotIndex];
            double endTime = nextRobotEvent.t;
            RobotPath currentPath = currentEvent.robotPaths[robotIndex];
            // could be that the robot already earlier reached its goal. We want to show this as well in the gui
            double possiblyEarlierEndtime = currentPath.getEndTime(startTime, robot.speed);
            endTime = Math.min(endTime, possiblyEarlierEndtime);

            robot.state = State.resultingFromEventType(nextRobotEvent.type);

            if (startTime == endTime) {
                robot.pos = endPos;
                switch (nextRobotEvent.type) {
                    case START_COMPUTE:
                        robot.state = State.COMPUTING;
                        break;
                    case START_MOVING:
                        robot.state = State.MOVING;
                        break;
                    case END_MOVING:
                        robot.state = State.SLEEPING;
                        break;
                }
            } else if (endTime < timestamp) {
                robot.pos = endPos;
            } else {
                switch (robot.state) {
                    case MOVING:
                        robot.pos = currentPath.interpolate(startTime, possiblyEarlierEndtime, timestamp);
                        break;
                    case COMPUTING:
                        robot.pos = currentPath.start;
                        break;
                    case SLEEPING:
                        robot.pos = currentPath.end;
                        break;
                }
                if (robot.state == State.MOVING) {
                    robot.pos = currentPath.interpolate(startTime, possiblyEarlierEndtime, timestamp);
                } else {
                    robot.pos = nextEvent.positions[robotIndex];
                }
            }
        }
    }
//
//    /**
//     * Creates a new {@link CalculatedEvent} at the end of the current timeline that is a sufficiently
//     * sensible continuation of the current timeline. This is mostly done in response to the user choosing
//     * to view a time beyond the end of the current timeline.
//     *
//     * Mutates the current list of calculated events! Note that the simulator will destroy this event
//     * if it is set back before it and started.
//     *
//     * @return The event that was created. This is now also the last event in the timeline.
//     */
//    private CalculatedEvent makeSyntheticNextEvent() {
//        System.out.println("Synthetic.");
//        CalculatedEvent currentEvent = simulator.calculatedEvents.get(simulator.calculatedEvents.size()-1);
//        CalculatedEvent nextEvent = currentEvent.copyDeep();
//
//        Vector[] positions = new Vector[localRobots.length];
//
//        double maxEndTime = currentEvent.getTimestamp() + 1;
//
//        int eventIndex = 0;
//        for (Event robotEvent : currentEvent.events) {
//            int robotIndexTemp = getRobotIndex(robotEvent.r);
//            Robot robot = localRobots[robotIndexTemp];
//
//            Event nextRobotEvent = nextEvent.events.get(robotIndexTemp);
//            RobotPath nextRobotPath = nextEvent.robotPaths[robotIndexTemp];
//
//            // If no more calculated events came up and we haven't finished padding till all robots stop do this
//            if (!isDoneSimulating && isScheduleDone && !paddedLastEvent) {
//
//                Event robotCurrentEvent = currentEvent.events.get(robotIndexTemp);
//                switch (robotCurrentEvent.type) {
//                    // If robots have started moving then finish the movement to their goal and calculate how much time this takes. TODO: Make sure this takes the current scheduler into account
//                    case START_MOVING:
//                        nextEvent.positions[robotIndexTemp] = nextRobotPath.end;
//                        double endTime = nextRobotPath.getEndTime(nextRobotEvent.t, robot.speed);
//                        if (endTime > maxEndTime) maxEndTime = endTime;
//                        break;
//
//                        // If robots have started computing, finish the compute cycle and afterwards should start moving to final goal.
//                    case START_COMPUTE:
//                        nextRobotEvent.t = nextRobotEvent.t + 1;
//                        break;
//
//                    case END_MOVING:
//                        // If robots have started stopped moving, but are not yet at their goal start computing next round.
//                        nextRobotEvent.t = nextRobotEvent.t + 1;
//                        nextEvent.positions[robotIndexTemp] = nextRobotPath.end;
//                        break;
//                }
//
//                nextRobotEvent.type = EventType.next(robotCurrentEvent.type);
//
//                System.out.println(eventIndex + " ? " + (nextEvent.events.size()-1));
//                if (eventIndex == nextEvent.events.size()-1) {
//                    dragBarSimulation.setMax(maxEndTime);
//                    dragBarSimulation.setValue(maxEndTime);
//
//                    // Set the time of all robots events to the max time till now
//                    for (Event event : nextEvent.events) {
//                        event.t = maxEndTime;
//                    }
//                    simulator.getCalculatedEvents().add(nextEvent);
//                    regenerateEventList(simulator.getCalculatedEvents(), false);
//                    if (nextEvent.events.get(robotIndexTemp).type.equals(EventType.END_MOVING)) {
//                        paddedLastEvent = true;
//                    }
//                }
//            }
//            eventIndex++;
//        }
//        return nextEvent;
//    }

    /**
     * Gather the previous and next event given a timestamp.
     * @param timestamp timestamp to find the prev and next events for
     * @return a length 2 array containing the previous and next event in order
     */
    private CalculatedEvent[] gatherRecentEvents(double timestamp) {
        List<CalculatedEvent> calculatedEvents = simulator.getCalculatedEvents();
        if (calculatedEvents.size() == 0) {
            // Only occurs if nothing has been simulated yet
            return null;
        }

        CalculatedEvent currentEvent = null;
        CalculatedEvent nextEvent = null;

        for (int i = 0; i < calculatedEvents.size(); i++) {
            double calculatedEventsTime = calculatedEvents.get(i).events.get(0).t;

            // If the previous event is non-existant due to the first event being the next event
            if (i == 0 && calculatedEventsTime > timestamp) {
                currentEvent = null;
                nextEvent = calculatedEvents.get(i);
                break;
            }

            // The event with this timestamp happened after the selected timestamp
            if (calculatedEventsTime > timestamp) {
                // If there is no previous event simply use the first event, only occurs when the first timestamp an
                // event occurs is selected to be simulated. Otherwise pick the most recent event
                currentEvent = calculatedEvents.get(i - 1);
                nextEvent = calculatedEvents.get(i);

                // Stop after finding first candidate
                break;
            }

            // The event with this timestamp happens at the selected timestamp
            if (i == calculatedEvents.size() - 1) {
                currentEvent = calculatedEvents.get(i);
                nextEvent = null;
                break;
            }
        }
        return new CalculatedEvent[]{currentEvent, nextEvent};
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

    private EventHandler<ActionEvent> algorithmButtonHandler = event -> {
        String simpleName = ((Button)event.getSource()).getText();
        Class algorithmClass = null;
        for (Class c : algorithms) {
            if (c.getSimpleName().equals(simpleName)) {
                algorithmClass = c;
            }
        }
        // cannot give NullPointer if the algorithms array is not changed in between
        System.out.println("Algorithm will be set to: " + algorithmClass.getName());
        Robot[] robots = localRobots;
        for (Robot r : robots) {
            try {
                Algorithm algorithm = (Algorithm)algorithmClass.newInstance();
                r.setAlgorithm(algorithm);
            } catch (InstantiationException | IllegalAccessException e) {
                System.err.println("Algorithm " + algorithmClass + " cannot be instantiated.");
                e.printStackTrace();
            }
        }
        simulator.setState(robots);
    };

    public void axisChanged(ActionEvent actionEvent) {
        boolean sameChirality = chiralityAxisButton.isSelected();
        boolean sameUnitLength = unitLengthAxisButton.isSelected();
        boolean sameRotation = rotationAxisButton.isSelected();
        for (Robot r : localRobots) {
            r.trans = new RotationTransformation().randomize(sameChirality, sameUnitLength, sameRotation);
        }

    }

    public void onFSync(ActionEvent actionEvent) {
        resetSimulation(0d);
        onSelectScheduler(actionEvent, FSyncScheduler::new, false);
    }

    public void onSSync(ActionEvent actionEvent) {
        resetSimulation(0d);
        onSelectScheduler(actionEvent, SSyncScheduler::new, false);
    }

    public void onASync(ActionEvent actionEvent) {
        resetSimulation(0d);
        onSelectScheduler(actionEvent, AsyncScheduler::new, false);
    }

    public void onFileScheduler(ActionEvent actionEvent) {
        final FileChooser fc = new FileChooser();
        File file = fc.showOpenDialog(null);
        onSelectScheduler(actionEvent, () -> {
            try {
                return new FileScheduler(file, localRobots);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            return null;
        }, true);
    }

    public void onManualScheduler(ActionEvent actionEvent) {
        onSelectScheduler(actionEvent, ManualScheduler::new, false);
    }

    public void onSelectScheduler(ActionEvent actionEvent, Supplier<Scheduler> schedulerSupplier, boolean force) {
        String selectedText = ((RadioMenuItem)actionEvent.getSource()).getText();
        if (!selectedText.equals(lastSelectedScheduler) || force) {
            lastSelectedScheduler = selectedText;
            simulator.setScheduler(schedulerSupplier.get());
            System.out.println("The scheduler changed. This may affect still moving robots and they may be interrupted even if the config says they should not be interrupted.");
        }
    }

    public void onMultiplicity(ActionEvent actionEvent) {
        simulator.config.multiplicity = multiplicityToggle.isSelected();
    }

    public void onVisibility(ActionEvent actionEvent) {
        simulator.config.visibility =  Double.valueOf(visibilityTextBox.getText());
    }

    public void onInterruptable(ActionEvent actionEvent) {
        simulator.config.interuptable = interruptableToggle.isSelected();
    }

    public void onInfiniteVisibility(ActionEvent actionEvent) {
        if (infiniteVisibilityToggle.isSelected()) {
            simulator.config.visibility = -1;
        }else {
            if (!visibilityTextBox.getText().equals("")) {
                simulator.config.visibility = Double.valueOf(visibilityTextBox.getText());
            }
        }
        visibilityTextBox.setDisable(infiniteVisibilityToggle.isSelected());
    }

}