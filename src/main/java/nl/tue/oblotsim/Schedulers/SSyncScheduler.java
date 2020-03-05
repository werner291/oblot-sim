package nl.tue.oblotsim.Schedulers;

import nl.tue.oblotsim.Simulator.Robot;
import nl.tue.oblotsim.Simulator.State;

import java.util.*;

public class SSyncScheduler extends Scheduler {

    double minComputeTime;
    double maxComputeTime;
    double minMoveTime;
    double maxMoveTime;
    double nextEndMoving = 0;

    double timestampLastEvent = 0;

    double lastRequestedEventTime = -1;
    List<Event> lastReturnedEvents = null;

    Random random;

    public SSyncScheduler() {
        this(1, 1, 1, 5);
    }


    /**
     * creates new SyncScheduler
     * @param minComputeTime min compute time
     * @param maxComputeTime max compute time
     * @param minMoveTime min move time
     * @param maxMoveTime max move time
     */
    public SSyncScheduler(double minComputeTime, double maxComputeTime, double minMoveTime, double maxMoveTime) {
        this.minComputeTime = minComputeTime;
        this.maxComputeTime = maxComputeTime;

        this.minMoveTime = minMoveTime;
        this.maxMoveTime = maxMoveTime;


        random = new Random();
    }

    @Override
    public List<Event> getNextEvent(List<Robot> robots, double t) {
        if (lastRequestedEventTime == t) {
            return lastReturnedEvents;
        }
        EventType nextType = null;
        for (Robot robot: robots) {
            if (nextType == null || nextType == EventType.START_COMPUTE) {
                nextType = getEventType(robot);
            }else if (nextType == EventType.START_MOVING && robot.getState() == State.MOVING) {
                throw new IllegalStateException("One robot is moving and one is computing which is not possible in Sync");
            }else if (nextType == EventType.END_MOVING && robot.getState() == State.COMPUTING) {
                throw new IllegalStateException("One robot is moving and one is computing which is not possible in Sync");
            }
        }

        List<Event> events = new ArrayList<>();
        switch (nextType) {
            case START_COMPUTE:
                int NROFRobots = random.nextInt(robots.size()-1) + 1;
                List<Robot> chosenRobots = List.copyOf(robots);

                while (chosenRobots.size() > NROFRobots) {
                    int chosenRobot = random.nextInt(chosenRobots.size());
                    chosenRobots.remove(chosenRobot);
                }

                double computeStart = t + random.nextDouble();
                for (Robot robot: chosenRobots) {
                    events.add(new Event(nextType, computeStart, robot.getId()));
                }
                break;
            case START_MOVING:
                double computeTime = minComputeTime + (maxComputeTime - minMoveTime) * random.nextDouble();

                for (Robot robot: robots) {
                    if (robot.getState() == State.COMPUTING) {
                        events.add(new Event(nextType, t + computeTime, robot.getId()));
                    }
                }
                break;
            case END_MOVING:
                double endTime;
                if (nextEndMoving > t) {
                    endTime = nextEndMoving;
                }else {
                    endTime = t + minMoveTime + (maxMoveTime - minMoveTime) * random.nextDouble();;
                }

                for (Robot robot: robots) {
                    if (robot.getState() == State.MOVING) {
                        events.add(new Event(nextType, endTime, robot.getId()));
                    }
                }
                break;
        }
        lastRequestedEventTime = t;
        lastReturnedEvents = events;
        return events;
    }

    /**
     * Get the eventType that a robot needs.
     * @param robot the robot to check
     * @return the next event for a specific robot
     */
    protected EventType getEventType(Robot robot) {
        switch (robot.getState()) {
            case SLEEPING:
                return EventType.START_COMPUTE;
            case COMPUTING:
                return EventType.START_MOVING;
            case MOVING:
                return EventType.END_MOVING;
            default:
                throw new IllegalStateException("The state of a robot is not properly initialized.");
        }
    }

    public void addEvent(Event e) {
        if (e.getType() == EventType.END_MOVING && e.getT() > nextEndMoving) {
            nextEndMoving = e.getT();
        }
    }
}
