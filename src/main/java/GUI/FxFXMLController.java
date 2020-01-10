package GUI;

import Algorithms.Algorithm;
import PositionTransformations.RotationTransformation;
import RobotPaths.RobotPath;
import Schedulers.*;
import Util.Circle;
import Util.SmallestEnclosingCircle;
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
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.transform.Affine;
import javafx.stage.FileChooser;
import javafx.stage.Popup;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

/**
 * The controller behind the {@link GUI}. The functions here define what happens when
 * the GUI is manipulated using button presses, slider changes etc.
 */
public class FxFXMLController
{
    boolean isScheduleDone = false;
    boolean isDoneSimulating = false;
    boolean paddedLastEvent = false;
    boolean isPaused = true;
    int playBackSpeed = 1;
    int last_size_calc_events = 0;

    @FXML
    // The reference of inputText will be injected by the FXML loader
    private ProgressBar progressBarSimulation;

    @FXML
    private TextField playBackSpeedLabel;

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
        // draw the canvas on a timer
        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                // Prevents trying to draw the simulator before it's fully initialized. (race condition)
                if (simulator != null) {
                    recomputeRobots(dragBarSimulation.getValue());
                    robotView.paintCanvas(localRobots);

                    if (!isPaused) {
                        playDragBar();
                    }
                }
            }
        };
        timer.start();

        playBackSpeedLabel.setText(""+playBackSpeed);
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

        // set the values for the events scrollpane
        eventsVBox.setSpacing(1);
        eventsVBox.setPadding(new Insets(1));

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

    private void hideWarningPopUp() {
        warningPopup.hide();
    }

    private void playDragBar() {
        // Get list of computed events
        List<CalculatedEvent> calculatedEvents = simulator.getCalculatedEvents();
        if (calculatedEvents.size() == 0) {
            if (!simulateNextEvent()) {
                isPaused = true;
                playButton.setText("Play");
            }
            return;
        }
        List<Event> recentEvents = calculatedEvents.get((calculatedEvents.size()-1)).events;

        // Add recent events to Vbox containing all events
        double recentTimeStamp = 0;
        for (Event calculatedEvent : recentEvents) {
            recentTimeStamp = calculatedEvent.t;
        }

        // Redraw robot positions
        double simulationTime = dragBarSimulation.valueProperty().get();
        if (simulationTime == recentTimeStamp && playBackSpeed <= 0) dragBarSimulation.valueProperty().set(simulationTime + ((double)playBackSpeed/100));
        else if (simulationTime == 0 && playBackSpeed >= 0) dragBarSimulation.valueProperty().set(simulationTime + ((double)playBackSpeed/100));
        else if (simulationTime < recentTimeStamp && simulationTime > 0) dragBarSimulation.valueProperty().set(simulationTime + ((double)playBackSpeed/100));
        else if (simulationTime >= recentTimeStamp && playBackSpeed >= 0)
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
            regenerateEventList(simulator.getCalculatedEvents());
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
            regenerateEventList(simulator.getCalculatedEvents());
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
    }

    /**
     * Called whenever the Next Event button is pressed
     * If a user presses next whilst browsing history, clear the stack of calcevents and reset the sim back to that time
     * So the simulation is done again from the nextevent onwards @TODO Allow user to start simulation from generic timstamp
     */
    @FXML
    private void nextSimulation() {
        if (dragBarSimulation.getValue() < dragBarSimulation.getMax()) {
            double localTimeStamp = dragBarSimulation.getValue();
            CalculatedEvent[] events = gatherRecentEvents(localTimeStamp);
            if (events != null && events[1] != null) {
                recomputeRobots(localTimeStamp);
                CalculatedEvent latestEvent = events[1];
                List<CalculatedEvent> newList = removeInvalidCalcEvents(simulator.calculatedEvents, latestEvent);
                simulator.setState(localRobots, newList, localTimeStamp);
                dragBarSimulation.setMax(localTimeStamp);
            }
            dragBarSimulation.setValue(dragBarSimulation.getMax());
            last_size_calc_events = 0;
            simulateNextEvent();
        } else if (dragBarSimulation.getValue() != dragBarSimulation.getMax()) {
            dragBarSimulation.setValue(dragBarSimulation.getMax());
        } else {
            if (!simulateNextEvent()) {
                isPaused = true;
                playButton.setText("Play");
            }
        }
    }

    /**
     * Returns whether or not every robot has reached their goal
     * @param calculatedEvents list of all events until now, used to check if the last event made the robots reach their goals
     * @return if the robots have all reached their goal then return true
     */
    private boolean checkIsDoneSimulating(List<CalculatedEvent> calculatedEvents) {
        if (calculatedEvents.size() == 0)
            throw new IllegalArgumentException("Next event was requested, however no event was added to the calculatedEvents list");

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

        try {
            simulator.simulateTillNextEvent();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Get list of computed events
        List<CalculatedEvent> calculatedEvents = simulator.getCalculatedEvents();

        // Check if an additional event was added. If not, then don't add anything to the list
        if (calculatedEvents.size() == last_size_calc_events || isScheduleDone) {
            isDoneSimulating = checkIsDoneSimulating(calculatedEvents);
            isScheduleDone = true;
            showWarningPopUp("No new event was generated");
            return false;
        }
        last_size_calc_events = calculatedEvents.size();


        regenerateEventList(calculatedEvents);

        double recentTimeStamp = calculatedEvents.get(calculatedEvents.size()-1).getTimestamp();
        progressBarSimulation.setProgress(75);
        dragBarSimulation.setMax(recentTimeStamp);
        dragBarSimulation.setValue(dragBarSimulation.getMax());
        progressBarSimulation.setProgress(100);

        return true;
    }

    private void regenerateEventList(List<CalculatedEvent> calculatedEvents) {
        List<Event> allEvents = new ArrayList<>();
        calculatedEvents.forEach(e -> allEvents.addAll(e.events));
        progressBarSimulation.setProgress(50);

        // Add recent events to Vbox containing all events
        int eventIndex = 1;
        eventsVBox = new VBox();
        for (Event event : allEvents) {
            eventsVBox.getChildren().add(createEventButton(eventIndex, event.type.toString(), event.t));
            eventIndex++;
        }
        eventList.setContent(eventsVBox);
    }

    private List<CalculatedEvent> removeInvalidCalcEvents(List<CalculatedEvent> calculatedEvents, CalculatedEvent latestEvent) {
        List<CalculatedEvent> tempCalcEvents = new ArrayList<>();

        for (CalculatedEvent calculatedEvent : calculatedEvents) {
            tempCalcEvents.add(calculatedEvent);
            if (calculatedEvent.events.get(0).t == (latestEvent.events.get(0).t)) break;
        }

        return tempCalcEvents;
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
        boolean keepSimulating = true;
        while (keepSimulating) {
            keepSimulating = simulateNextEvent();
        }
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
            double maxEndTime = currentEvent.events.get(0).t + 1;

            int eventIndex = 0;
            for (Event robotEvent : nextEvent.events) {
                int robotIndexTemp = getRobotIndex(robotEvent.r);
                Robot robot = localRobots[robotIndexTemp];

                Event nextRobotEvent = nextEvent.events.get(robotIndexTemp);
                RobotPath nextRobotPath = nextEvent.robotPaths[robotIndexTemp];

                // If no more calculatedevents came up and we haven't finished padding till all robots stop do this
                if (!isDoneSimulating && isScheduleDone && !paddedLastEvent) {

                    // If robots have started moving then finish the movement to their goal and calculate how much time this takes. TODO: Make sure this takes the current scheduler into account
                    if (currentEvent.events.get(robotIndexTemp).type.equals(EventType.START_MOVING)) {
                        nextRobotEvent.type = EventType.END_MOVING;
                        nextEvent.positions[robotIndexTemp] = nextRobotPath.end;
                        double endTime = nextRobotPath.getEndTime(nextRobotEvent.t, robot.speed);
                        if (endTime > maxEndTime) maxEndTime = endTime;
                    }

                    // If robots have started computing, finish the compute cycle and afterwards should start moving to final goal.
                    if (currentEvent.events.get(robotIndexTemp).type.equals(EventType.START_COMPUTE)) {
                        nextRobotEvent.type = EventType.START_MOVING;
                        nextRobotEvent.t = nextRobotEvent.t + 1;
                    }

                    // If robots have started stopped moving, but are not yet at their goal start computing next round.
                    if (currentEvent.events.get(robotIndexTemp).type.equals(EventType.END_MOVING)) {
                        nextRobotEvent.type = EventType.START_COMPUTE;
                        nextRobotEvent.t = nextRobotEvent.t + 1;
                        nextEvent.positions[robotIndexTemp] = nextRobotPath.end;
                    }

                    if (eventIndex == nextEvent.events.size()-1) {
                        dragBarSimulation.setMax(maxEndTime);
                        dragBarSimulation.setValue(maxEndTime);

                        // Set the time of all robots events to the max time till now
                        int temp = 0;
                        for (Event event : nextEvent.events) {
                            event.t = maxEndTime;
                            eventsVBox.getChildren().add(createEventButton(temp + 1, event.type.toString(), maxEndTime));
                            temp++;
                        }
                        simulator.getCalculatedEvents().add(nextEvent);
                        if (nextEvent.events.get(robotIndexTemp).type.equals(EventType.END_MOVING)) {
                            paddedLastEvent = true;
                        }
                    }
                }
                eventIndex++;
            }
        }

        // Change robots for the draw function
        for (Event nextRobotEvent : nextEvent.events) {
            int robotIndex = getRobotIndex(nextRobotEvent.r);
            Robot robot = localRobots[robotIndex];

            double startTime = currentEvent.events.get(0).t;
            Vector endPos = nextEvent.positions[robotIndex];
            double endTime = nextEvent.events.get(0).t;
            RobotPath currentPath = currentEvent.robotPaths[robotIndex];
            // could be that the robot already earlier reached its goal. We want to show this as well in the gui
            double possiblyEarlierEndtime = currentPath.getEndTime(startTime, robot.speed);
            endTime = Math.min(endTime, possiblyEarlierEndtime);

            switch (currentEvent.events.get(robotIndex).type) {
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

            if (startTime == endTime) {
                robot.pos = endPos;
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
            if (i == 0 && calculatedEventsTime > timestamp)
            {
                currentEvent = null;
                nextEvent = calculatedEvents.get(i);
                break;
            }

            // The event with this timestamp happened after the selected timestamp
            if (calculatedEventsTime > timestamp)
            {
                // If there is no previous event simply use the first event, only occurs when the first timestamp an
                // event occurs is selected to be simulated. Otherwise pick the most recent event
                currentEvent = calculatedEvents.get(i-1);
                nextEvent = calculatedEvents.get(i);

                // Stop after finding first candidate
                break;
            }

            // The event with this timestamp happens at the selected timestamp
            if (i == calculatedEvents.size()-1)
            {
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

//    public void showAxisTriggered(ActionEvent actionEvent) {
//        this.drawCoordinateSystems = ((CheckMenuItem)actionEvent.getSource()).isSelected();
//        if (drawCoordinateSystems) {
//            System.out.println("Coordinate systems of the robots will be drawn.");
//        } else {
//            System.out.println("Coordinate systems of the robots will not be drawn.");
//        }
//    }

    public void axisChanged(ActionEvent actionEvent) {
        boolean sameChirality = chiralityAxisButton.isSelected();
        boolean sameUnitLength = unitLengthAxisButton.isSelected();
        boolean sameRotation = rotationAxisButton.isSelected();
        for (Robot r : localRobots) {
            r.trans = new RotationTransformation().randomize(sameChirality, sameUnitLength, sameRotation);
        }
    }

    public void onFSync(ActionEvent actionEvent) {
        String selectedText = ((RadioMenuItem)actionEvent.getSource()).getText();
        if (!selectedText.equals(lastSelectedScheduler)) {
            lastSelectedScheduler = selectedText;
            simulator.setScheduler(new FSyncScheduler());
            System.out.println("The scheduler changed. This may affect still moving robots and they may be interrupted even if the config says they should not be interrupted.");
        }
    }

    public void onSSync(ActionEvent actionEvent) {
        String selectedText = ((RadioMenuItem)actionEvent.getSource()).getText();
        if (!selectedText.equals(lastSelectedScheduler)) {
            lastSelectedScheduler = selectedText;
            simulator.setScheduler(new SSyncScheduler());
            System.out.println("The scheduler changed. This may affect still moving robots and they may be interrupted even if the config says they should not be interrupted.");
        }
    }

    public void onASync(ActionEvent actionEvent) {
        String selectedText = ((RadioMenuItem)actionEvent.getSource()).getText();
        if (!selectedText.equals(lastSelectedScheduler)) {
            lastSelectedScheduler = selectedText;
            simulator.setScheduler(new AsyncScheduler());
            System.out.println("The scheduler changed. This may affect still moving robots and they may be interrupted even if the config says they should not be interrupted.");
        }
    }

//    public void onShowSEC(ActionEvent actionEvent) {
//        this.drawSEC = ((CheckMenuItem)actionEvent.getSource()).isSelected();
//        if (drawSEC) {
//            System.out.println("Smallest enclosing circle will be drawn.");
//        } else {
//            System.out.println("Smallest enclosing circle will not be drawn.");
//        }
//    }
//
//    public void onShowRadii(ActionEvent actionEvent) {
//        this.drawRadii = ((CheckMenuItem)actionEvent.getSource()).isSelected();
//        if (drawRadii) {
//            System.out.println("Radii to center of SEC will be shown");
//        } else {
//            System.out.println("Radii to center of SEC will not be shown");
//            System.out.println("Radii to center of SEC will not be shown");
//        }
//    }
}