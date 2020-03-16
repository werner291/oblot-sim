package nl.tue.oblotsim.Schedulers;

import nl.tue.oblotsim.Simulator.Robot;
import nl.tue.oblotsim.Simulator.State;

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
        switch (robot.getState()) {
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
    public List<Event> getNextEvent(List<Robot> robots, double t, boolean allowEarlyStop) {
        if (lastRequestedEventTime == t) {
            return lastReturnedEvents;
        }
        double earliestNextEventTime = Double.MAX_VALUE;
        Robot earliestNextEventRobot = null;
        List<Event> events = new ArrayList<>();
        for (Robot robot: robots) {
            if (robot.getState() == State.COMPUTING) {
                if (robot.getInCurrentStateSince() + maxComputeTime < earliestNextEventTime) {
                    earliestNextEventTime = robot.getInCurrentStateSince() + maxComputeTime;
                    earliestNextEventRobot = robot;
                }
            }else if (robot.getState() == State.MOVING) {
                if (robot.getInCurrentStateSince() + maxMoveTime < earliestNextEventTime) {
                    earliestNextEventTime = robot.getInCurrentStateSince() + maxMoveTime;
                    earliestNextEventRobot = robot;
                }
            }
        }

        if (earliestNextEventRobot == null) {
            earliestNextEventTime = t + 0.2*random.nextDouble();
            earliestNextEventRobot = robots.get(random.nextInt(robots.size()));
        }

        if (earliestNextEventTime - t < 0.2) {
            EventType eventType = getNextEventType(earliestNextEventRobot);
            events.add(new Event(eventType, earliestNextEventTime, earliestNextEventRobot.getId()));
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
            events.add(new Event(EventType.END_MOVING, earliestNextEventTime, earliestNextEventRobot.getId()));
            return events;
        }

        List<Robot> availableRobots = new ArrayList<>();
        for (Robot robot: robots) {
            switch (robot.getState()) {
                case SLEEPING:
                    availableRobots.add(robot);
                    break;
                case COMPUTING:
                    if (robot.getInCurrentStateSince() + minComputeTime < t) {
                        availableRobots.add(robot);
                    }
                    break;
                case MOVING:
                    if (robot.getInCurrentStateSince() + minMoveTime < t && !endMovingTimes.containsKey(robot)) {
                        availableRobots.add(robot);
                    }
                    break;
            }
        }

        if (availableRobots.size()==0) {
            double earliestMinNextEventTime = Double.MAX_VALUE;
            Robot earliestMinNextEventRobot = null;
            for (Robot robot: robots) {
                switch (robot.getState()) {
                    case COMPUTING:
                        if (robot.getInCurrentStateSince() + minComputeTime < earliestMinNextEventTime) {
                            earliestMinNextEventTime = robot.getInCurrentStateSince() + minComputeTime;
                            earliestMinNextEventRobot = robot;
                        }
                    case MOVING:
                        if (robot.getInCurrentStateSince() + minMoveTime < earliestMinNextEventTime) {
                            earliestMinNextEventTime = robot.getInCurrentStateSince() + minMoveTime;
                            earliestMinNextEventRobot = robot;
                        }

                }
            }
            EventType eventType = getNextEventType(earliestMinNextEventRobot);
            events.add(new Event(eventType, earliestMinNextEventTime, earliestMinNextEventRobot.getId()));
            return events;
        }

        Robot chosenRobot = availableRobots.get(random.nextInt(availableRobots.size()));
        EventType eventType = getNextEventType(chosenRobot);
        double eventTime = t + (earliestNextEventTime - t) * random.nextDouble();
        events.add(new Event(eventType, eventTime, chosenRobot.getId()));

        lastRequestedEventTime = t;
        lastReturnedEvents = events;
        return events;
    }

//    public void addEvent(Event e) {
//        if (e.getType() ==EventType.END_MOVING) {
//            if (!endMovingTimes.containsKey(e.r)) {
//                endMovingTimes.put(e.r, e.getT());
//            }else if (e.getT() > endMovingTimes.get(e.r)) {
//                endMovingTimes.put(e.r, e.getT());
//            }
//        }
//    }
}
