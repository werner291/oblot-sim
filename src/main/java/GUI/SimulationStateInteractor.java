package GUI;

import RobotPaths.RobotPath;
import Schedulers.CalculatedEvent;
import Schedulers.Event;
import Schedulers.EventType;
import Simulator.Robot;
import Simulator.Simulator;
import Simulator.State;
import Util.Vector;
import javafx.beans.binding.DoubleExpression;
import javafx.beans.property.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Facade class for the Simulator that makes interaction with it a bit more high-level.
 *
 * Separates concerns between controlling the view and controlling the simulator interactions,
 * such as interpolation, moving to the next event, etc...
 */
public class SimulationStateInteractor implements GUI.RobotView.RobotManager {

    private final Simulator simulator;

    public SimulationStateInteractor(Simulator simulator) {
        this.simulator = simulator;
    }

    /**
     * Compute the position and state of robots from a timestamp in the past.
     *
     * @param timestamp Timestamp of the simulation to display on the canvas
     */
    void moveRobotsToTimestamp(double timestamp) {
        // Gather the most recent and the next event if available
        CalculatedEvent[] eventsFound = eventBeforeAndAfterTime(timestamp);

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
            if (!fxFXMLController.isDoneSimulating() && fxFXMLController.isScheduleDone() && !fxFXMLController.isPaddedLastEvent()) {
                double startTime = currentEvent.events.get(0).t;
                double endTime = currentEvent.events.get(0).t + 1;
                double maxEndTime = 0;

                List<Event> newlistofEvents = new ArrayList<Event>();

                for (Robot robot : fxFXMLController.getLocalRobots()) {
                    int robotIndexTemp = fxFXMLController.getRobotIndex(robot);
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
                fxFXMLController.getEventList().events.setAll(fxFXMLController.getSimulator().calculatedEvents);
                fxFXMLController.setPaddedLastEvent(true);

                fxFXMLController.getDragBarSimulation().setMax(maxEndTime);
                fxFXMLController.getDragBarSimulation().valueProperty().setValue(maxEndTime);
            }
        }

        // Change robots for the draw function
        for (Event nextRobotEvent : nextEvent.events) {
            int robotIndex = fxFXMLController.getRobotIndex(nextRobotEvent.r);
            Robot robot = fxFXMLController.getLocalRobots()[robotIndex];

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


    /**
     * Gather the previous and next event given a timestamp.
     * @param timestamp timestamp to find the prev and next events for
     * @return a length 2 array containing the previous and next event in order
     */
    private CalculatedEvent[] eventBeforeAndAfterTime(double timestamp) {
        List<CalculatedEvent> calculatedEvents = simulatorProperty.getValue().getCalculatedEvents();
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

    @Override
    public Robot[] getRobots() {
        return simulator.getRobots();
    }

    public void removeRobot(Robot toRemove) {
        Robot[] copy = Arrays.stream(simulator.getRobots()).filter(robot -> robot != toRemove).map(robot -> {
            robot.state = State.SLEEPING;
            return robot;
        })


                new Robot[simulator.getRobots().length - 1];
        int indexInCopy = 0;
        for (Robot localRobot : simulator.getRobots()) {
            if (localRobot != toRemove) {
                copy[indexInCopy] = localRobot;
                indexInCopy++;
            }
        }
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
}