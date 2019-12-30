package Schedulers;

import Simulator.Robot;

import java.util.ArrayList;
import java.util.List;

public class FSyncScheduler extends SyncScheduler {

    public FSyncScheduler(double minComputeTime, double maxComputeTime, double minMoveTime, double maxMoveTime, Robot[] robots) {
        super(minComputeTime, maxComputeTime, minMoveTime, maxMoveTime, robots);
    }

    public FSyncScheduler(Robot[] robots) {
        super(robots);
    }

    @Override
    void computeNextEvent(Robot[] robots, double t) {
        double computeTime = minComputeTime + (maxComputeTime - minMoveTime) * random.nextDouble();
        double moveTime = minMoveTime + (maxMoveTime - minMoveTime) * random.nextDouble();

        List<Event> startComputeEvents = new ArrayList<>();
        List<Event> startMoveEvents = new ArrayList<>();
        List<Event> endMoveEvents = new ArrayList<>();

        for (Robot robot: robots) {
            startComputeEvents.add(new Event(EventType.START_COMPUTE, t, robot));
            startMoveEvents.add(new Event(EventType.START_MOVING, t + computeTime, robot));
            endMoveEvents.add(new Event(EventType.END_MOVING, t + computeTime + moveTime, robot));
        }
        events.add(startComputeEvents);
        events.add(startMoveEvents);
        events.add(endMoveEvents);
        timestampLastEvent = t + computeTime + moveTime;
    }
}
