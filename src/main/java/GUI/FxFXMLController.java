package GUI;

import Algorithms.Algorithm;
import PositionTransformations.RotationTransformation;
import RobotPaths.RobotPath;
import Schedulers.*;
import javafx.animation.Interpolator;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import Simulator.Simulator;
import Simulator.Robot;
import Simulator.State;
import javafx.animation.AnimationTimer;
import javafx.beans.binding.*;
import javafx.beans.property.*;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URL;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.IntStream;

/**
 * The controller behind the {@link GUI}. The functions here define what happens when
 * the GUI is manipulated using button presses, slider changes etc.
 */
public class FxFXMLController implements RobotView.RobotManager
{
    //region Config option checkboxes.
    /**
     * Checkbox whether the robot movements are interruptible.
     */
    public CheckBox interruptableToggle;

    /**
     * Input field that keeps track of the visibility radius. (Disabled if infinite.)
     */
    public TextField visibilityTextBox;

    /**
     * Checkbox that controls whether visibility is infinitee.
     */
    public CheckBox infiniteVisibilityToggle;

    /**
     * Checkbox that controls multiplicity detection.
     */
    public CheckBox multiplicityToggle;
    //endregion

    boolean paddedLastEvent = false;

    /**
     * Property that, if true, means that the GUI is currently running non-stop until the end of the simulation.
     */
    boolean resetEvents = true;

    BooleanProperty isPaused = new SimpleBooleanProperty(true);
    BooleanProperty simulatingTillEnd = new SimpleBooleanProperty(false);

    int last_size_calc_events = -1;

    /**
     * The slider at the bottom of the screen that tracks the currently-displayed simulation timestamp.
     */
    @FXML
    private ProgressBar progressBarSimulation;

    //region Playback speed.
    /**
     * TextField containing the speed at which to run the simulation w.r.t actual time elapsed.
     */
    @FXML
    private TextField playBackSpeedLabel;

    /**
     * Playback speed of the simulation w.r.t real time. This is different than the frameRate.
     */
    SimpleIntegerProperty playBackSpeed = new SimpleIntegerProperty(1);
    //endregion

    /**
     * Label showing the current state of the simulation. (Idle, computing, etc...)
     */
    @FXML
    private Label statusLabel;

    //region End time stuff
    /**
     * Text field showing timestamp at which to stop simulation when running freely.
     */
    @FXML
    private TextField timeToEndSimulationTextField;

    /**
     * Timestamp until which to run the simulation if the system is set to run until then.
     */
    SimpleIntegerProperty timeToEndSimulation = new SimpleIntegerProperty(100);
    //endregion

    //region RobotView repaint rate stuff.
    @FXML
    public MenuItem frameRateMenuItem;
    @FXML
    public Slider frameRateMenuSlider;

    /**
     * Controls the target refresh rate of the RobotView.
     */
    SimpleDoubleProperty frameRate = new SimpleDoubleProperty(60.0);

    /**
     * Timestamp in milliseconds when the last frame was drawn.
     */
    private double lastFrametime = 0;
    //endregion

    @FXML
    // The reference of inputText will be injected by the FXML loader
    private Button playButton;
    @FXML
    private Button nextButton;
    @FXML
    private Button endButton;

    @FXML
    // The reference of inputText will be injected by the FXML loader
    private EventsView eventList;

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


    //region Buttons / checkboxes for PositionTransformations
    @FXML
    private CheckMenuItem chiralityAxisButton;
    @FXML
    private CheckMenuItem unitLengthAxisButton;
    @FXML
    private CheckMenuItem rotationAxisButton;
    //endregion

    //region Draw / don't draw visual aids.
    @FXML
    private CheckMenuItem drawCoordinateSystemsButton;
    @FXML
    private CheckMenuItem drawSECButton;
    @FXML
    private CheckMenuItem drawRadiiButton;
    //endregion

    // Keep track of this to stop us from replacing an already-selected schedule.
    private String lastSelectedScheduler;

    /**
     * The Simulator currently computing the robot movements.
     */
    private Simulator simulator;

    /**
     * The list of possible algorithms
     */
    private Class[] algorithms;

    // Reference to the array of robots used in the simulator.
    // Note that this is the same array object by reference!
    private Robot[] localRobots;

    //region Binding references to prevent the GC from destroying them.
    @SuppressWarnings("FieldCanBeLocal")
    // NO! IT CANNOT BE LOCAL! JAVAFX IS STUPID AND WILL GARBAGE-COLLECT THE INTERMEDIATE PROPERTY!
    // See https://tomasmikula.github.io/blog/2015/02/10/the-trouble-with-weak-listeners.html
    private Object playbackspeedAsObject;
    @SuppressWarnings("FieldCanBeLocal")
    private Object timetoEndSimulationAsObject;
    @SuppressWarnings("FieldCanBeLocal")
    private DoubleBinding robotViewWidth;
    private StringBinding playButtonText;
    private StringBinding endButtonText;
    //endregion

    // Add a public no-args constructor
    public FxFXMLController()
    {
    }

    @FXML
    private void initialize()
    {
        robotView.setRobotManager(this);
        eventList.timePickedCB = dragBarSimulation.valueProperty()::set;

        // draw the canvas on a timer
        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long now) {

                // Prevents trying to draw the simulator before it's fully initialized. (race condition)
                if (simulator != null && System.currentTimeMillis() > lastFrametime + 1000.0f / frameRate.get()) {
                    // If the bar is playing increment it
                    if (!isPaused.get()) {
                        playDragBar();
                        recomputeRobots(dragBarSimulation.getValue());
                    }
                    robotView.paintCanvas();
                }

                // If the simulation is simulating till a certain timestamp then follow that
                if (simulatingTillEnd.getValue()) {
                    List<CalculatedEvent> calculatedEvents = simulator.getCalculatedEvents();

                    // If no events exist yet atleast simulate one event
                    if (calculatedEvents.size() == 0) {
                        simulateNextEvent();
                        calculatedEvents = simulator.getCalculatedEvents();
                        if (calculatedEvents.size() == 0) {
                            isPaused.setValue(true);
                            simulatingTillEnd.setValue(false);
                        }
                    }

                    // Most recent Event
                    Event recentEvent = calculatedEvents.get(calculatedEvents.size()-1).events.get(0);

                    // Stop early at he max specified time given in the GUI
                    if (recentEvent.t < timeToEndSimulation.get()) {
                        simulateNextEvent();
                    }

                    // Update progressbar
                    progressBarSimulation.setProgress((recentEvent.t / (double) timeToEndSimulation.get()));
                    int timeToDisplay = (int)(recentEvent.t*100);
                    float timeToDisplayFloat = (float)(timeToDisplay)/100;
                    statusLabel.setText("Computing: " + timeToDisplayFloat + "/" + timeToEndSimulation.getValue());
                }
            }
        };
        timer.start();

        ControllerUtil.bindTextboxToNumber(playBackSpeed, retain -> this.playbackspeedAsObject = retain, playBackSpeedLabel);
        ControllerUtil.bindTextboxToNumber(timeToEndSimulation, retain -> this.timetoEndSimulationAsObject = retain, timeToEndSimulationTextField);

        frameRateMenuItem.setText("FrameRate: "+frameRate);
        frameRateMenuSlider.valueProperty().bindBidirectional(frameRate);
        frameRate.addListener((observableValue, number, t1) -> frameRateMenuItem.setText("FrameRate: " + number));

        // set the values for the events scrollpane
        algorithmsVBox.setSpacing(1);
        algorithmsVBox.setPadding(new Insets(1));

        // set the canvas to listen to the size of its parent
        robotViewWidth = canvasBackground.widthProperty().subtract(30);
        robotView.prefWidthProperty().bind(robotViewWidth);

        // Fill remaining height.
        canvasBackground.heightProperty().addListener((ov, oldValue, newValue) -> {
            robotView.setHeight(newValue.doubleValue() - dragBarSimulation.getHeight());
        });
        dragBarSimulation.valueProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                recomputeRobots(newValue.doubleValue());
            }
        });

        robotView.drawCoordinateSystems.bind(drawCoordinateSystemsButton.selectedProperty());
        robotView.drawSEC.bind(drawSECButton.selectedProperty());
        robotView.drawRadii.bind(drawRadiiButton.selectedProperty());

        playButtonText = Bindings.createStringBinding(() -> isPaused.get() ? "Play" : "Pause", isPaused);
        endButtonText = Bindings.createStringBinding(() -> simulatingTillEnd.get() ? "Stop" : "End:", simulatingTillEnd);
        playButton.textProperty().bind(playButtonText);
        endButton.textProperty().bind(endButtonText);
        playButton.disableProperty().bind(simulatingTillEnd);
        nextButton.disableProperty().bind(simulatingTillEnd.or(isPaused.not()));
        endButton.disableProperty().bind(isPaused.not());

        eventList.vvalueProperty().bind(eventList.list.heightProperty());

        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            new Alert(Alert.AlertType.ERROR, throwable.getMessage()).show();
            throwable.printStackTrace();
        });
    }

    public boolean canEditRobots() {
        return isPaused.get();
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

    /**
     * Remove the given robot from the set of robots.
     *
     * @param toRemove The robot to remove. Must be in this.localRobots.
     */
    public void removeRobot(Robot toRemove) {
        assert Arrays.stream(this.localRobots).anyMatch(robot -> robot == toRemove);

        //noinspection SuspiciousToArrayCall This works, Intellij just complains about it for some reason.
        Robot[] newRobots = Arrays.stream(localRobots).filter(robot -> robot != toRemove).map(robot -> {
            robot.state = State.SLEEPING;
            return robot;
        }).toArray(Robot[]::new);
        simulator.setState(newRobots, new ArrayList<>(), 0);
        localRobots = newRobots;
        dragBarSimulation.setValue(0);
        resetSimulation();
    }

    public void addRobot(Robot newRobot) {
        Robot[] copy = new Robot[localRobots.length + 1];
        System.arraycopy(localRobots, 0, copy, 0, localRobots.length);
        copy[copy.length - 1] = newRobot;
        localRobots = copy;
        Arrays.stream(localRobots).forEach(r -> r.state = State.SLEEPING);
        dragBarSimulation.setValue(0);
        simulator.setState(localRobots);
        resetSimulation();
    }

    /**
     * When not paused, called once every frame to update the state of the bar at the bottom of the screen showing
     * which time point in the simulation is visible.
     */
    private void playDragBar() {

        // Get list of computed events
        List<CalculatedEvent> calculatedEvents = simulator.getCalculatedEvents();
        if (calculatedEvents.size() == 0) {
            if (!simulateNextEvent()) isPaused.setValue(true);
            calculatedEvents = simulator.getCalculatedEvents();
            if (calculatedEvents.size() == 0) {
                return;
            }
         }

        List<Event> recentEvents = calculatedEvents.get((calculatedEvents.size()-1)).events;

        double recentTimeStamp = recentEvents.get(0).t;



        // Redraw robot positions
        double simulationTime = dragBarSimulation.valueProperty().get();
        if (simulationTime == recentTimeStamp && playBackSpeed.get() < 0) dragBarSimulation.valueProperty().set(simulationTime + ((double)playBackSpeed.get()/100));
        else if (simulationTime == 0 && playBackSpeed.get() > 0) dragBarSimulation.valueProperty().set(simulationTime + ((double)playBackSpeed.get()/100));
        else if (simulationTime < recentTimeStamp && simulationTime > 0) dragBarSimulation.valueProperty().set(simulationTime + ((double)playBackSpeed.get()/100));
        else if (simulationTime >= recentTimeStamp && playBackSpeed.get() > 0)
        {
            if (!simulateNextEvent()) {
                isPaused.set(true);
            }
            dragBarSimulation.valueProperty().set(simulationTime + ((double)playBackSpeed.get()/100));
        }
        else {
            isPaused.set(true);
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
                new Alert(Alert.AlertType.ERROR, "Class " + a + " is not a subclass of Algorithm.").show();
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
            eventList.events.get().setAll(simulator.getCalculatedEvents());
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
            eventList.events.get().setAll(simulator.getCalculatedEvents());
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
        isPaused.set(true);
    }

    /**
     * Called whenever the Next Event button is pressed
     * If a user presses next whilst browsing history, clear the stack of calcevents and reset the sim back to that time
     * So the simulation is done again from the nextevent onwards
     */
    @FXML
    public void nextSimulation() {
        // If called whilst browsing history, reset the future
        if (dragBarSimulation.getValue() < dragBarSimulation.getMax()) {
            setSimulation(dragBarSimulation.getValue());
            simulateNextEvent();
        }
        // Should not be possible, added just in case
        else if (dragBarSimulation.getValue() > dragBarSimulation.getMax()) {
            System.err.println(dragBarSimulation.getValue() + " surpassed max: " + dragBarSimulation.getMax());
            dragBarSimulation.setValue(dragBarSimulation.getMax());
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
        if (dragBarSimulation.getValue() < dragBarSimulation.getMax() && isPaused.get()) {
            setSimulation(dragBarSimulation.getValue());
        }

        // toggle the isPaused global variable also set the buttontext and disable the end/nextbuttons
        isPaused.set(!isPaused.get());
    }

    /**
     * Helper function that resets the simulation to the current dragbar timestamp or the given timestamp if it is given
     */
    private void setSimulation(double localTimeStamp) {
        // get most recent event for given timestamp
        CalculatedEvent[] events = gatherRecentEvents(localTimeStamp);
        List<CalculatedEvent> newList;

        if (events == null || events[0] == null) {
            newList = new ArrayList<>();
        } else {
            // Reset Calculated Events to given timestamp
            CalculatedEvent latestEvent = events[0];
            // Get the list of events upto the most recent event directly precending or at this event.
            newList = removeInvalidCalcEvents(simulator.calculatedEvents, latestEvent);
        }

        // Set the reset robots and calculatedevents to the state of the sim
        Robot[] copy = new Robot[this.localRobots.length];
        for (int i = 0; i < localRobots.length; i++) {
            copy[i] = localRobots[i].copy();
        }
        simulator.setState(copy, newList, localTimeStamp);
        eventList.events.setAll(simulator.getCalculatedEvents());

        // Reset the simulation drag bar as well
        dragBarSimulation.setMax(localTimeStamp);
        dragBarSimulation.setValue(dragBarSimulation.getMax());
        recomputeRobots(dragBarSimulation.getValue());

        // Reset robots to given timestamp
        recomputeRobots(localTimeStamp);

        // Reset global variable used to check if a simulateNext() call actually adds a new event
        last_size_calc_events = -1;

        // Set global variable which will enforce the list of events on the left to be recomputed from scratch
        resetEvents = true;
        isPaused.setValue(true);
    }

    /**
     * Helper function that resets the simulation to the current dragbar timestamp or the given timestamp if it is given
     */
    private void resetSimulation() {
        // Get current selected timestamp
        setSimulation(0d);
    }

    /**
     * Returns whether or not every robot has reached their goal.
     *
     * @param calculatedEvents last event
     * @return true if the robots have all reached their goal, false if one or more have not
     */
    private boolean checkIsDoneSimulating(List<CalculatedEvent> calculatedEvents, double localTimeStamp) {
        for (Robot robot : localRobots) {
            int robotIndex = getRobotIndex(robot);
            CalculatedEvent calculatedEvent = getLatestRobotEvent(robot, calculatedEvents, localTimeStamp);
            if (calculatedEvent == null) continue;
            if (!calculatedEvent.positions[robotIndex].equals(calculatedEvent.robotPaths[robotIndex].end)) return false;
        }

        return localTimeStamp > 0;
    }

    /**
     * Called to gather the new events and add them to the eventlist, set the new timestep
     * @return true if the next event exists, false if it does not
     */
    private boolean simulateNextEvent() {
        // Start simulating the next event

        // Compute next events
        progressBarSimulation.setProgress(0);
        String statusString = simulator.scheduler instanceof ManualScheduler ? "Waiting for next event" : "Computing next event";
        statusLabel.setText(statusString);

        try {
            simulator.simulateTillNextEvent();
        } catch (Exception e) {
            isPaused.setValue(true);
            new Alert(Alert.AlertType.ERROR, e.getMessage()).show();
            e.printStackTrace();
        }
        progressBarSimulation.setProgress(0.25);

        // Get list of computed events
        List<CalculatedEvent> calculatedEvents = simulator.getCalculatedEvents();

        progressBarSimulation.setProgress(0.5);
        // Check if an additional event was added. If not, then don't add anything to the list
        if (calculatedEvents.size() == last_size_calc_events) {
            List<CalculatedEvent> possibleSynthEvent = makeSyntheticEvent(calculatedEvents);
            if (possibleSynthEvent.size() > 0) {
                simulator.calculatedEvents.addAll(possibleSynthEvent);
                calculatedEvents = simulator.getCalculatedEvents();
            }
            else {
                if (!(simulator.scheduler instanceof ListScheduler)) {
                    new Alert(Alert.AlertType.ERROR, "No new event was generated").show();
                }
                return false;
            }
        }
        if (calculatedEvents.size() == 0) {
            // No events have been generated yet
            return false;
        }

        // Check if robots have reached their goals
        CalculatedEvent latestCalculatedEvent = calculatedEvents.get(calculatedEvents.size()-1);

        last_size_calc_events = calculatedEvents.size();
        progressBarSimulation.setProgress(0.75);

        // Add the new calculatedEvent
        eventList.events.add(latestCalculatedEvent);

        double recentTimeStamp = calculatedEvents.get(calculatedEvents.size()-1).getTimestamp();
        dragBarSimulation.setMax(recentTimeStamp);
        dragBarSimulation.setValue(dragBarSimulation.getMax());
        recomputeRobots(dragBarSimulation.getValue());

        progressBarSimulation.setProgress(1);
        statusLabel.setText("Idle");

        return true;
    }

    private List<CalculatedEvent> makeSyntheticEvent(List<CalculatedEvent> calculatedEvents) {
        List<CalculatedEvent> cendMoveEvent = new ArrayList<>();
        for (Robot robot : localRobots) {
            if (robot.state == State.MOVING) {
                int robotIndex = getRobotIndex(robot);
                CalculatedEvent latestCalculatedRobotEvent = getLatestRobotEvent(robot, calculatedEvents, dragBarSimulation.getMax());
                if (latestCalculatedRobotEvent == null) continue;
                Event latestRobotEvent = getRobotEvent(robot, latestCalculatedRobotEvent.events);
                if (latestRobotEvent == null) continue;

                CalculatedEvent latestCalcEvent = calculatedEvents.get(calculatedEvents.size() - 1);

                RobotPath currentPath = latestCalculatedRobotEvent.robotPaths[robotIndex];
                double startMovingTime = latestCalcEvent.getTimestamp();
                double endMovingTime = currentPath.getEndTime(startMovingTime, robot.speed);
                Event endMoveEvent = new Event(EventType.END_MOVING, endMovingTime, robot);
                List<Event> listEndMoveEvent = new ArrayList<>();
                listEndMoveEvent.add(endMoveEvent);

                cendMoveEvent.add(new CalculatedEvent(listEndMoveEvent, latestCalcEvent.positions, latestCalcEvent.robotPaths));
            }
        }

        return cendMoveEvent;
    }

    /**
     * Cut off the given list of {@link CalculatedEvent} at the timestamp of the one provided, including the one with that timestamp.
     *
     * @param calculatedEvents  List of events to filter.
     * @param latestEvent       Event whose timestamp to use as a cutoff point.
     * @return                  Entries of calculatedEvents upto and including the one with latestEvent's timestamp.
     */
    private List<CalculatedEvent> removeInvalidCalcEvents(List<CalculatedEvent> calculatedEvents,
                                                          CalculatedEvent latestEvent) {

        List<CalculatedEvent> tempCalcEvents = new ArrayList<>();

        for (CalculatedEvent calculatedEvent : calculatedEvents) {
            tempCalcEvents.add(calculatedEvent);
            if (calculatedEvent.events.get(0).t == (latestEvent.events.get(0).t)) break;
        }

        return tempCalcEvents;
    }

    /**
     * Increase the playback speed by 1.
     */
    @FXML
    private void forwardAction() {
        playBackSpeed.set(playBackSpeed.get() + 1);
    }

    /**
     * Decrease the playback speed by 1.
     */
    @FXML
    private void backwardsAction() {
        playBackSpeed.set(playBackSpeed.get() - 1);
    }

    /**
     *
     * Start/stop running the simulation at maximum speed until we either run out of events,
     * or we hit the maximum running time specified by this.timeToEndSimulation
     *
     * TODO: Computes until all the robots have stopped moving and are sleeping, might never finish
     */
    @FXML
    private void endSimulation() {
        // Switch on/off.
        simulatingTillEnd.setValue(!simulatingTillEnd.getValue());
    }

    /**
     * Return the index number of a robot in the localRobots.
     *
     * @param robotToMatch, robot to find the correct index number for to match CalculatedEvent.positions/goals with
     * @return index used to retrieve the position and goals of a robot with.
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
     * Compute the position and state of robots from a timestamp in the past, and set the localRobots to this state.
     *
     * Note: this is different from Simulator.interpolateRobots since
     * this event works with the historical list of events!
     *
     * @param timestamp Timestamp of the simulation to display on the canvas.
     */
    private void recomputeRobots(double timestamp) {
       List<CalculatedEvent> calcEvents = simulator.getCalculatedEvents();
        // Change robots for the draw function
        for (Robot robot : localRobots) {
            int robotIndex = getRobotIndex(robot);

            CalculatedEvent currentRobotCevent = getLatestRobotEvent(robot, calcEvents, timestamp);

            Event currentRobotEvent = null;
            if (currentRobotCevent != null) currentRobotEvent = getRobotEvent(robot, currentRobotCevent.events);

            if (currentRobotEvent == null) { // If the current event is the first event, make up the prev event as sleeping until the first event.
                //currentRobotEvent = new Event(EventType.END_MOVING, 0, robot);
                robot.setToStart();
                robot.state = State.SLEEPING;

                // Don't do anything else
                continue;
            }

            robot.state = State.resultingFromEventType(currentRobotEvent.type);

            if (currentRobotEvent.type != EventType.START_MOVING) continue;

            double startTime = currentRobotEvent.t;
            RobotPath currentPath = currentRobotCevent.robotPaths[robotIndex];

            double endTimePath = currentPath.getEndTime(startTime, robot.speed);
            // could be that the robot already earlier reached its goal. We want to show this as well in the gui

            if (timestamp >= endTimePath) {
                robot.pos = currentPath.end;
            } else {
                if (robot.state == State.MOVING) {
                    robot.pos = currentPath.interpolate(startTime, endTimePath, timestamp);
                } else if (robot.state == State.SLEEPING) {
                    robot.pos = currentRobotCevent.positions[robotIndex];
                } else {
                    robot.pos = currentPath.start;
                }
            }
        }
    }

    private Event getRobotEvent(Robot robot, List<Event> eventList) {
        if (eventList == null) return null;

        for (Event event : eventList) {
            if (event.r.equals(robot)) {
                return event;
            }
        }
        return null;
    }

    /**
     * Return the Current calcevent for the given robot
     * @param robot robot to find the current and next event for
     * @param calcEvents the calculated events to search in
     * @param timestamp timestamp interested in
     * @return two events, the current one and the next one containing the robot r
     * if null, no such event exists
     */
    private CalculatedEvent getLatestRobotEvent(Robot robot, List<CalculatedEvent> calcEvents, double timestamp) {
        for (int i = calcEvents.size() - 1; i >= 0; i--) {
            CalculatedEvent e = calcEvents.get(i);
            if (e.events.get(0).t > timestamp) continue;
            if (e.containsRobot(robot)) return e;
        }
        return null;
    }

    /**
     * Gather the events directly preceding/succeeding a timestamp.
     *
     * The preceding (first in array) event may have the timestamp given as query parameter.
     * Note that either event can be null if we are before the start or after the end of the events list.
     *
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

        // Linear search through calculatedEvents
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

    /**
     * Handle the user clicking one of the algorithm selection buttons.
     */
    private EventHandler<ActionEvent> algorithmButtonHandler = event -> {

        // Look up the selected algorithm by name.
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
                new Alert(Alert.AlertType.ERROR, "Algorithm " + algorithmClass + " cannot be instantiated.").show();
                e.printStackTrace();
            }
        }
        simulator.setState(robots);
    };

    /**
     * Update {@link PositionTransformations.PositionTransformation} options.
     */
    public void axisChanged(ActionEvent actionEvent) {
        boolean sameChirality = chiralityAxisButton.isSelected();
        boolean sameUnitLength = unitLengthAxisButton.isSelected();
        boolean sameRotation = rotationAxisButton.isSelected();
        for (Robot r : localRobots) {
            r.trans = new RotationTransformation().randomize(sameChirality, sameUnitLength, sameRotation);
        }

    }

    //region Select/update scheduler.
    public void onFSync(ActionEvent actionEvent) {
        resetSimulation();
        onSelectScheduler(actionEvent, FSyncScheduler::new, false);
    }

    public void onSSync(ActionEvent actionEvent) {
        resetSimulation();
        onSelectScheduler(actionEvent, SSyncScheduler::new, false);
    }

    public void onASync(ActionEvent actionEvent) {
        resetSimulation();
        onSelectScheduler(actionEvent, AsyncScheduler::new, false);
    }

    public void onFileScheduler(ActionEvent actionEvent) {
        resetSimulation();
        final FileChooser fc = new FileChooser();
        File file = fc.showOpenDialog(null);
        onSelectScheduler(actionEvent, () -> {
            try {
                return new FileScheduler(file, localRobots);
            } catch (FileNotFoundException e) {
                new Alert(Alert.AlertType.ERROR, e.getMessage()).show();
                e.printStackTrace();
            }
            return null;
        }, true);
    }

    public void onManualScheduler(ActionEvent actionEvent) {
        onSelectScheduler(actionEvent, () -> new ManualScheduler(simulator), false);
    }

    public void onSelectScheduler(ActionEvent actionEvent, Supplier<Scheduler> schedulerSupplier, boolean force) {
        String selectedText = ((RadioMenuItem)actionEvent.getSource()).getText();
        if (!selectedText.equals(lastSelectedScheduler) || force) {
            lastSelectedScheduler = selectedText;
            simulator.setScheduler(schedulerSupplier.get());
            System.out.println("The scheduler changed. This may affect still moving robots and they may be interrupted even if the config says they should not be interrupted.");
        }
    }
    //endregion

    //region Simulator config options setting/updating.
    public void onMultiplicity(ActionEvent actionEvent) {
        simulator.config.multiplicity = multiplicityToggle.isSelected();
    }

    public void onVisibility(ActionEvent actionEvent) {
        if (Double.parseDouble(visibilityTextBox.getText()) < 0) {
            simulator.config.visibility = 0;
            visibilityTextBox.setText("0");
        }else {
            simulator.config.visibility =  Double.parseDouble(visibilityTextBox.getText());
        }
    }

    public void onInterruptable(ActionEvent actionEvent) {
        simulator.config.interuptable = interruptableToggle.isSelected();
    }


    public void onInfiniteVisibility(ActionEvent actionEvent) {
        if (infiniteVisibilityToggle.isSelected()) {
            simulator.config.visibility = -1;
        }else {
            if (!visibilityTextBox.getText().equals("")) {
                simulator.config.visibility = Double.parseDouble(visibilityTextBox.getText());
            }
        }
        visibilityTextBox.setDisable(infiniteVisibilityToggle.isSelected());
    }
    //endregion
}