package Schedulers;

import Simulator.Robot;
import Simulator.State;

import java.util.*;

public class SSyncScheduler extends Scheduler {

    double minComputeTime;
    double maxComputeTime;
    double minMoveTime;
    double maxMoveTime;
    double nextEndMoving = 0;

    double timestampLastEvent = 0;

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
    public List<Event> getNextEvent(Robot[] robots, double t) {
        EventType nextType = null;
        for (Robot robot: robots) {
            if (nextType == null || nextType == EventType.START_COMPUTE) {
                nextType = getEventType(robot);
            }else if (nextType == EventType.START_MOVING && robot.state == State.MOVING) {
                throw new IllegalStateException("One robot is moving and one is computing which is not possible in Sync");
            }else if (nextType == EventType.END_MOVING && robot.state == State.COMPUTING) {
                throw new IllegalStateException("One robot is moving and one is computing which is not possible in Sync");
            }
        }

        List<Event> events = new ArrayList<>();
        switch (nextType) {
            case START_COMPUTE:
                int NROFRobots = random.nextInt(robots.length-1) + 1;
                List<Robot> chosenRobots = new ArrayList<>(Arrays.asList(robots));

                while (chosenRobots.size() > NROFRobots) {
                    int chosenRobot = random.nextInt(chosenRobots.size());
                    chosenRobots.remove(chosenRobot);
                }

                double computeStart = t + random.nextDouble();
                for (Robot robot: chosenRobots) {
                    events.add(new Event(nextType, computeStart, robot));
                }
                break;
            case START_MOVING:
                double computeTime = minComputeTime + (maxComputeTime - minMoveTime) * random.nextDouble();

                for (Robot robot: robots) {
                    if (robot.state == State.COMPUTING) {
                        events.add(new Event(nextType, t + computeTime, robot));
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
                    if (robot.state == State.MOVING) {
                        events.add(new Event(nextType, endTime, robot));
                    }
                }
                break;
        }
        return events;
    }

    /**
     * Get the eventType that a robot needs.
     * @param robot the robot to check
     * @return the next event for a specific robot
     */
    protected EventType getEventType(Robot robot) {
        switch (robot.state) {
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

    @Override
    public void addEvent(Event e) {
        if (e.type == EventType.END_MOVING && e.t > nextEndMoving) {
            nextEndMoving = e.t;
        }
    }
}