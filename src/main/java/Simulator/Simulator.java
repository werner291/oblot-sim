package Simulator;

import Algorithms.Robot;
import Algorithms.State;
import Schedulers.CalculatedEvent;
import Util.Config;
import Util.Interpolate;
import Util.Vector;
import Schedulers.Event;
import Schedulers.Scheduler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
        while (scheduler.getNextEvent(robots, currentTime) != null && scheduler.getNextEvent(robots, currentTime).get(0).t < t) {
            //this changes the current time
            simulateTillNextEvent();
        }
        interpolateRobots(currentTime, t);
        currentTime = t;
    }



    /**
     * Simulate the robots until the next event that will be requested from the scheduler.
     * It could be the case that there are no events anymore. In this case, the simulator will do nothing,
     * as all information is already known
     */
    public void simulateTillNextEvent() {
        List<Event> nextEvents = scheduler.getNextEvent(robots, currentTime);

        if (nextEvents == null) {
            return;
        }

        Vector[] goals;

        if (!calculatedEvents.isEmpty()) {
            goals = calculatedEvents.get(calculatedEvents.size()-1).goals;
        } else {
            //set the goals to the current position of the robots
            goals = Arrays.stream(robots).map(robot -> robot.pos).toArray(Vector[]::new);
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
                    Vector goal = e.r.calculate(getSnapshot(e.r));
                    int index = Arrays.asList(robots).indexOf(e.r);
                    goals[index] = goal;
                    e.r.state = State.COMPUTING;
                    break;
                case START_MOVING:
                    if (e.r.state != State.COMPUTING) {
                        throw new IllegalStateException("Scheduled event has a wrong type: " + e.r.state + " Should be " + State.COMPUTING);
                    }
                    e.r.state = State.MOVING;
                    break;
                case END_MOVING:
                    if (e.r.state != State.MOVING) {
                        throw new IllegalStateException("Scheduled event has a wrong type: " + e.r.state + " Should be " + State.MOVING);
                    }
                    e.r.state = State.SLEEPING;
                    break;
            }
        }
        Vector[] positions = Arrays.stream(robots).map(robot -> robot.pos).toArray(Vector[]::new);
        CalculatedEvent calculatedEvent = new CalculatedEvent(nextEvents, positions, goals);
        calculatedEvents.add(calculatedEvent);
    }

    /**
     * moves the robots to the position of the given timestamp.
     * @param startTime The time they started moving (time of the last event)
     * @param interpolateTime The time until they want to move
     */
    private void interpolateRobots(double startTime, double interpolateTime) {
        Vector[] goals;
        if (!calculatedEvents.isEmpty()) {
            goals = calculatedEvents.get(calculatedEvents.size()-1).goals;
        } else {
            //set the goals to the current position of the robots
            goals = Arrays.stream(robots).map(robot -> robot.pos).toArray(Vector[]::new);

        }

        for (int i = 0; i < robots.length; i++) {
            Robot robot = robots[i];
            if (robot.state == State.MOVING) {
                Vector position = robot.pos;
                Vector goal = goals[i];
                double endTime = Interpolate.getEndTime(position, startTime, goal, robot.speed);
                if (endTime < interpolateTime) {
                    robot.pos = goal;
                } else {
                    robot.pos = Interpolate.linearInterpolate(position, startTime, goal, endTime, interpolateTime);
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

    public Robot[] getRobots() {
        return this.robots;
    }

    public List<CalculatedEvent> getCalculatedEvents() {
        return this.calculatedEvents;
    }
}
