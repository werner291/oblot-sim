package Simulator;

import RobotPaths.LinearPath;
import RobotPaths.RobotPath;
import Schedulers.CalculatedEvent;
import Schedulers.EventType;
import Util.Config;
import Util.Vector;
import Schedulers.Event;
import Schedulers.Scheduler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * The main simulator. Works by extracting an event list of the schedule,
 * and asking the robots accordingly how they want to handle their current states
 * Is also capable of saving the end result.
 */
public class Simulator {
    /**
     * The {@link Config} for this simulator. Can be changed on the fly.
     */
    public Config config;

    /**
     * The robots that will be simulated
     */
    public Robot[] robots;

    /**
     * The list of events that have happened up till the current simulated timestamp.
     * This should be sorted in the order of timestamps of the inner events.
     * The previous events can be extracted from this.
     */
    public List<CalculatedEvent> calculatedEvents = new ArrayList<>();

    /**
     * The timestamp the simulator is currently at.
     */
    private double currentTime = 0;

    /**
     * The {@link Scheduler} that regulates which robots activate when.
     */
    private Scheduler scheduler;

    public Simulator(Config c, Robot[] robots, Scheduler scheduler) {
        this.config = c;
        this.robots = robots;
        this.scheduler = scheduler;

    }

    /**
     * Simulate the robots until timestamp t
     * @param t the timestamp to simulate to
     */
    public void simulateTillTimestamp(double t) {
        // use simulateTillNextEvent until we pass the correct t and interpolate back to the correct t
        List<Event> events = scheduler.getNextEvent(robots, currentTime);
        while (events != null && events.get(0).t < t) {
            //this changes the current time
            simulateEvents(events);
            events = scheduler.getNextEvent(robots, currentTime);
        }
        interpolateRobots(currentTime, t);
        currentTime = t;
    }


    private void simulateEvents(List<Event> nextEvents) {
        if (nextEvents == null) {
            return;
        }

        RobotPath[] robotPaths;

        if (!calculatedEvents.isEmpty()) {
            robotPaths = calculatedEvents.get(calculatedEvents.size()-1).robotPaths;
        } else {
            //set the robotPaths to the current position of the robots
            robotPaths = Arrays.stream(robots).map(robot -> new LinearPath(robot.pos, robot.pos)).toArray(RobotPath[]::new);
        }

        double previousTime = currentTime;
        currentTime = nextEvents.get(0).t;

        interpolateRobots(previousTime, currentTime);

        for (Event e : nextEvents) { // process all events
            switch (e.type) {
                case START_COMPUTE:
                    if (e.r.state != State.SLEEPING) {
                        throw new IllegalStateException("Scheduled event has a wrong type: " + e.r.state + " Should be " + State.SLEEPING);
                    }
                    RobotPath path = e.r.calculate(getSnapshot(e.r));
                    int index = Arrays.asList(robots).indexOf(e.r);
                    robotPaths[index] = path;
                    e.r.state = State.COMPUTING;
                    break;
                case START_MOVING:
                    if (e.r.state != State.COMPUTING) {
                        throw new IllegalStateException("Scheduled event has a wrong type: " + e.r.state + " Should be " + State.COMPUTING);
                    }
                    e.r.state = State.MOVING;
                    path = robotPaths[Arrays.asList(robots).indexOf(e.r)];
                    if (!config.interuptable) {
                        // add next end_move to the scheduler
                        double endTime = path.getEndTime(currentTime, e.r.speed);
                        Event proposed_end_move = new Event(EventType.END_MOVING, endTime, e.r);
                        scheduler.addEvent(proposed_end_move);
                    }
                    break;
                case END_MOVING:
                    if (e.r.state != State.MOVING) {
                        throw new IllegalStateException("Scheduled event has a wrong type: " + e.r.state + " Should be " + State.MOVING);
                    }
                    e.r.state = State.SLEEPING;
                    break;
            }
            e.r.lastStateChange = e.t;
        }
        Vector[] positions = Arrays.stream(robots).map(robot -> robot.pos).toArray(Vector[]::new);
        CalculatedEvent calculatedEvent = new CalculatedEvent(nextEvents, positions, robotPaths);
        calculatedEvents.add(calculatedEvent);
    }


    /**
     * Simulate the robots until the next event that will be requested from the scheduler.
     * It could be the case that there are no events anymore. In this case, the simulator will do nothing,
     * as all information is already known
     */
    public void simulateTillNextEvent() {
        List<Event> nextEvents = scheduler.getNextEvent(robots, currentTime);


        simulateEvents(nextEvents);

    }

    /**
     * moves the robots to the position of the given timestamp.
     * @param startTime The time they started moving (time of the last event)
     * @param interpolateTime The time until they want to move
     */
    public void interpolateRobots(double startTime, double interpolateTime) {
        RobotPath[] robotPaths;
        if (!calculatedEvents.isEmpty()) {
            robotPaths = calculatedEvents.get(calculatedEvents.size()-1).robotPaths;
        } else {
            //set the robotPaths to the current position of the robots
            robotPaths = Arrays.stream(robots).map(robot -> new LinearPath(robot.pos, robot.pos)).toArray(RobotPath[]::new);

        }

        for (int i = 0; i < robots.length; i++) {
            Robot robot = robots[i];
            if (robot.state == State.MOVING) {
                RobotPath robotPath = robotPaths[i];
                double endTime = robotPath.getEndTime(startTime, robot.speed);
                if (endTime < interpolateTime) {
                    robot.pos = robotPath.end;
                } else {
                    robot.pos = robotPath.interpolate(startTime, endTime, interpolateTime);
                }
            }
        }
    }

    /**
     * Get a snapshot of the current view (in global coordinates) for a specific robot, given that it has a visibility and a current position.
     * @param r the robot to get the snapshot for
     * @return a snapshot of the positions of all robots in global space for a specific robot
     */
    public Vector[] getSnapshot(Robot r) {
        Vector[] positions = Arrays.stream(robots).map(robot -> robot.pos).toArray(Vector[]::new);
        // first check the visibility
        if (config.visibility != -1) {
            positions = Arrays.stream(positions).filter(p -> r.pos.dist(p) < config.visibility).toArray(Vector[]::new);
        }
        // then remove duplicates if there is no multiplicity detection
        if (!config.multiplicity) {
            positions = Arrays.stream(positions).distinct().toArray(Vector[]::new);
        }
        return positions;
    }

    /**
     * Returns the robots. If these robots are modified, the simulator is modified as well!
     * @return the internal robots
     */
    public Robot[] getRobots() {
        Robot[] copy = new Robot[this.robots.length];
        for (int i = 0; i < robots.length; i++) {
            copy[i] = robots[i].copy();
        }
        return copy;
    }

    /**
     * Returns the list of calculated events
     * @return the list of calculated events
     */
    public List<CalculatedEvent> getCalculatedEvents() {
        return this.calculatedEvents.stream().map(CalculatedEvent::copyDeep).collect(Collectors.toList());
    }

    /**
     * Set the complete state of the simulator
     * @param robots the robots
     * @param calculatedEvents the calculated events
     * @param time the time
     */
    public void setState(Robot[] robots, List<CalculatedEvent> calculatedEvents, double time) {
        this.robots = robots;
        this.calculatedEvents = calculatedEvents;
        this.currentTime = time;
    }

    /**
     * Set only the robots to a different set of robots, but not the calculatedEvents nor the timestamp
     * @param robots the new robots
     */
    public void setState(Robot[] robots) {
        this.robots = robots;
    }

    /**
     * Sets the scheduler. Take note that this may cause problems with the interruptability of robots.
     * @param s The new scheduler
     */
    public void setScheduler(Scheduler s) {
        this.scheduler = s;
    }
}
