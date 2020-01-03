package Schedulers;

import Simulator.Robot;
import Simulator.State;

import java.util.ArrayList;
import java.util.List;

public class FSyncScheduler extends SSyncScheduler {

    /**
     * creates new Fully sync scheduler
     * @param minComputeTime min compute time
     * @param maxComputeTime max compute time
     * @param minMoveTime min move time
     * @param maxMoveTime max move time
     */
    public FSyncScheduler(double minComputeTime, double maxComputeTime, double minMoveTime, double maxMoveTime) {
        super(minComputeTime, maxComputeTime, minMoveTime, maxMoveTime);
    }

    public FSyncScheduler() {
        super();
    }

    @Override
    public List<Event> getNextEvent(Robot[] robots, double t) {
        EventType nextType = null;
        State currentState = null;
        for (Robot robot: robots) {
            if (nextType == null) {
                nextType = getEventType(robot);
                currentState = robot.state;
            } else if (currentState != robot.state) {
                throw new IllegalStateException("not all the robots have the same state");
            }
        }

        List<Event> events = new ArrayList<>();
        switch (nextType) {
            case START_COMPUTE:
                double computeStart = t + random.nextDouble();
                for (Robot robot: robots) {
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
}
