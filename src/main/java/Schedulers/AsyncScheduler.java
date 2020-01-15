package Schedulers;

import Simulator.Robot;
import Simulator.State;

import java.util.*;

public class AsyncScheduler extends Scheduler {

    double minComputeTime;
    double maxComputeTime;
    double minMoveTime;
    double maxMoveTime;
    Map<Robot, Double> endMovingTimes;

    double lastRequestedEventTime = -1;
    List<Event> lastReturnedEvents = null;


    Random random;

    public AsyncScheduler () {
        this(1, 1, 1, 5);
    }


    /**
     * creates new SyncScheduler
     * @param minComputeTime min compute time
     * @param maxComputeTime max compute time
     * @param minMoveTime min move time
     * @param maxMoveTime max move time
     */
    public AsyncScheduler(double minComputeTime, double maxComputeTime, double minMoveTime, double maxMoveTime) {
        this.minComputeTime = minComputeTime;
        this.maxComputeTime = maxComputeTime;

        this.minMoveTime = minMoveTime;
        this.maxMoveTime = maxMoveTime;


        random = new Random();

        endMovingTimes = new HashMap<>();

    }

    private EventType getNextEventType (Robot robot) {
        switch (robot.state) {
            case SLEEPING:
                return EventType.START_COMPUTE;
            case COMPUTING:
                return EventType.START_MOVING;
            case MOVING:
                return EventType.END_MOVING;
            default:
                throw new IllegalStateException("This should not happen");
        }
    }

    @Override
    public List<Event> getNextEvent(Robot[] robots, double t) {
        if (lastRequestedEventTime == t) {
            return lastReturnedEvents;
        }
        double earliestNextEventTime = Double.MAX_VALUE;
        Robot earliestNextEventRobot = null;
        List<Event> events = new ArrayList<>();
        for (Robot robot: robots) {
            if (robot.state == State.COMPUTING) {
                if (robot.lastStateChange + maxComputeTime < earliestNextEventTime) {
                    earliestNextEventTime = robot.lastStateChange + maxComputeTime;
                    earliestNextEventRobot = robot;
                }
            }else if (robot.state == State.MOVING) {
                if (robot.lastStateChange + maxMoveTime < earliestNextEventTime) {
                    earliestNextEventTime = robot.lastStateChange + maxMoveTime;
                    earliestNextEventRobot = robot;
                }
            }
        }

        if (earliestNextEventRobot == null) {
            earliestNextEventTime = t + 0.2*random.nextDouble();
            earliestNextEventRobot = robots[random.nextInt(robots.length)];
        }

        if (earliestNextEventTime - t < 0.2) {
            EventType eventType = getNextEventType(earliestNextEventRobot);
            events.add(new Event(eventType, earliestNextEventTime, earliestNextEventRobot));
            return events;
        }

        boolean forcedEvent = false;
        for(Robot robot: endMovingTimes.keySet()) {
            if (endMovingTimes.get(robot) < earliestNextEventTime) {
                earliestNextEventTime = endMovingTimes.get(robot);
                earliestNextEventRobot = robot;
                forcedEvent = true;
            }
        }

        if (forcedEvent) {
            events.add(new Event(EventType.END_MOVING, earliestNextEventTime, earliestNextEventRobot));
            return events;
        }

        List<Robot> availableRobots = new ArrayList<>();
        for (Robot robot: robots) {
            switch (robot.state) {
                case SLEEPING:
                    availableRobots.add(robot);
                    break;
                case COMPUTING:
                    if (robot.lastStateChange + minComputeTime < t) {
                        availableRobots.add(robot);
                    }
                    break;
                case MOVING:
                    if (robot.lastStateChange + minMoveTime < t && !endMovingTimes.containsKey(robot)) {
                        availableRobots.add(robot);
                    }
                    break;
            }
        }

        Robot chosenRobot = availableRobots.get(random.nextInt(availableRobots.size()));
        EventType eventType = getNextEventType(chosenRobot);
        double eventTime = t + (earliestNextEventTime - t) * random.nextDouble();
        events.add(new Event(eventType, eventTime, chosenRobot));

        lastRequestedEventTime = t;
        lastReturnedEvents = events;
        return events;
    }

    @Override
    public void addEvent(Event e) {
        if (e.type==EventType.END_MOVING) {
            if (!endMovingTimes.containsKey(e.r)) {
                endMovingTimes.put(e.r, e.t);
            }else if (e.t > endMovingTimes.get(e.r)) {
                endMovingTimes.put(e.r, e.t);
            }
        }
    }
}
