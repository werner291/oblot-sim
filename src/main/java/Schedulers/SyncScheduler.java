package Schedulers;

import Simulator.Robot;
import Simulator.State;

import java.util.*;
;

public class SyncScheduler extends Scheduler {

    double minComputeTime;
    double maxComputeTime;
    double minMoveTime;
    double maxMoveTime;
    double nextEndMoving = 0;

    double timestampLastEvent = 0;

    Random random;

    public SyncScheduler () {
        this(1, 1, 1, 5);
    }


    /**
     * creates new SyncScheduler
     * @param minComputeTime min compute time
     * @param maxComputeTime max compute time
     * @param minMoveTime min move time
     * @param maxMoveTime max move time
     */
    public SyncScheduler(double minComputeTime, double maxComputeTime, double minMoveTime, double maxMoveTime) {
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
                switch (robot.state) {
                    case SLEEPING:
                        nextType = EventType.START_COMPUTE;
                        break;
                    case COMPUTING:
                        nextType = EventType.START_MOVING;
                        break;
                    case MOVING:
                        nextType = EventType.END_MOVING;
                        break;
                }
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
                List<Robot> clonedRobots = new ArrayList<>(Arrays.asList(robots));

                List<Robot> chosenRobots = new ArrayList<>();

                for (int i = 0; i < NROFRobots; i++) {
                    int chosenRobot = random.nextInt(clonedRobots.size());
                    chosenRobots.add(clonedRobots.get(chosenRobot));
                    clonedRobots.remove(chosenRobot);
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

    @Override
    public void addEvent(Event e) {
        if (e.type == EventType.END_MOVING && e.t > nextEndMoving) {
            nextEndMoving = e.t;
        }
    }
}
