package nl.tue.oblotsim.Schedulers;

import nl.tue.oblotsim.Simulator.Robot;
import nl.tue.oblotsim.Simulator.State;

import java.util.*;
import java.util.stream.Collectors;

public class SSyncScheduler extends Scheduler {

    double minComputeTime;
    double maxComputeTime;
    double minMoveTime;
    double maxMoveTime;

    Random random = new Random();

    public SSyncScheduler() {
        this(1, 1, 1, 5);
    }


    /**
     * creates new SyncScheduler
     *
     * @param minComputeTime min compute time
     * @param maxComputeTime max compute time
     * @param minMoveTime    min move time
     * @param maxMoveTime    max move time
     */
    public SSyncScheduler(double minComputeTime, double maxComputeTime, double minMoveTime, double maxMoveTime) {
        this.minComputeTime = minComputeTime;
        this.maxComputeTime = maxComputeTime;

        this.minMoveTime = minMoveTime;
        this.maxMoveTime = maxMoveTime;
    }

    private double makePseudorandomStopTime(Robot r, double minDeltaT, double maxDeltaT) {
        int seed = Integer.hashCode(r.getId()) * 31 + Double.hashCode(r.getInCurrentStateSince());
        return (maxDeltaT - minDeltaT) * new Random(seed).nextDouble() + minDeltaT;
    }

    /**
     * Pick a random non-empty sublist of the provided list of robots.
     *
     * Rng is seeded with the last "in current state since" among the given robots,
     * and should therefore yield the same pic within a round.
     *
     * @param r The list of robots to pick from.
     * @return Non-empty pseudo-random sublist of r.
     */
    protected List<Robot> chooseActiveRobots(List<Robot> r) {

        double seedTime = r.stream().mapToDouble(Robot::getInCurrentStateSince).max().orElseThrow(() -> new IllegalArgumentException("Robot list may not be empty!"));

        Random random = new Random(Double.hashCode(seedTime));

        ArrayList<Robot> l = new ArrayList<>(r);

        Collections.shuffle(l, random);

        return l.subList(0, 1 + random.nextInt(l.size()-1));
    }

    @Override
    public List<Event> getNextEvent(List<Robot> robots, double t, boolean allowEarlyStop) {

        State currentRobotState = robots.get(0).getState();
        double since = robots.get(0).getInCurrentStateSince();

        assert robots.stream().allMatch(robot -> robot.getState() == currentRobotState);
        assert robots.stream().allMatch(robot -> robot.getInCurrentStateSince() == since);

        switch (currentRobotState) {

            case COMPUTING:
                return robots.stream()
                        // Filter, because not all robots are active!
                        .filter(robot -> robot.getState() == State.COMPUTING)
                        .map(robot -> new Event(EventType.START_MOVING, since + 1.0, robot.getId()))
                        .collect(Collectors.toList());

            case MOVING:
                if (allowEarlyStop) {
                    return robots.stream()
                            // Filter, because not all robots are active!
                            .filter(robot -> robot.getState() == State.MOVING)
                            .map(robot -> new Event(EventType.END_MOVING, since + makePseudorandomStopTime(robot, minMoveTime, maxMoveTime), robot.getId())).collect(Collectors.toList());
                } else {
                    double lastStopTime = Math.max(minMoveTime, robots.stream().mapToDouble(robot -> robot.willStopBefore().orElse(since)).max().orElse(since));
                    return robots.stream()
                            // Filter, because not all robots are active!
                            .filter(robot -> robot.getState() == State.MOVING)
                            .map(robot -> new Event(EventType.END_MOVING, lastStopTime, robot.getId()))
                            .collect(Collectors.toList());
                }

            case SLEEPING:
                return chooseActiveRobots(robots).stream()
                        .map(robot -> new Event(EventType.START_COMPUTE, since + 1.0, robot.getId()))
                        .collect(Collectors.toList());
        }

        throw new IllegalStateException("Switch should be exhaustive. How did we get here?");
    }
}
