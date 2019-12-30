package Schedulers;

import Simulator.Robot;

import java.util.*;
;

public class SyncScheduler extends Scheduler {

    double minComputeTime;
    double maxComputeTime;
    double minMoveTime;
    double maxMoveTime;

    List<List<Event>> events;
    double timestampLastEvent = 0;

    Random random;

    public SyncScheduler (Robot[] robots) {
        random = new Random();
        this.minComputeTime = 1;
        this.maxComputeTime = 1;

        this.minMoveTime = 1;
        this.maxMoveTime = 1 + random.nextInt(5);

        events = new ArrayList<>();
        computeNextEvent(robots, 0);
    }


    public SyncScheduler(double minComputeTime, double maxComputeTime, double minMoveTime, double maxMoveTime, Robot[] robots) {
        this.minComputeTime = minComputeTime;
        this.maxComputeTime = maxComputeTime;

        this.minMoveTime = minMoveTime;
        this.maxMoveTime = maxMoveTime;

        events = new ArrayList<>();
        random = new Random();
        computeNextEvent(robots, 0);
    }

    void computeNextEvent(Robot[] robots, double t) {
        int NROFRobots = random.nextInt(robots.length-1) + 1;
        List<Robot> clonedRobots = new ArrayList<>(Arrays.asList(robots));

        List<Robot> chosenRobots = new ArrayList<>();

        for (int i = 0; i < NROFRobots; i++) {
            int chosenRobot = random.nextInt(clonedRobots.size());
            chosenRobots.add(clonedRobots.get(chosenRobot));
            clonedRobots.remove(chosenRobot);
        }

        double computeTime = minComputeTime + (maxComputeTime - minMoveTime) * random.nextDouble();
        double moveTime = minMoveTime + (maxMoveTime - minMoveTime) * random.nextDouble();

        List<Event> startComputeEvents = new ArrayList<>();
        List<Event> startMoveEvents = new ArrayList<>();
        List<Event> endMoveEvents = new ArrayList<>();

        for (Robot robot: chosenRobots) {
            startComputeEvents.add(new Event(EventType.START_COMPUTE, t, robot));
            startMoveEvents.add(new Event(EventType.START_MOVING, t + computeTime, robot));
            endMoveEvents.add(new Event(EventType.END_MOVING, t + computeTime + moveTime, robot));
        }
        events.add(startComputeEvents);
        events.add(startMoveEvents);
        events.add(endMoveEvents);
        timestampLastEvent = t + computeTime + moveTime;

    }

    /**
     * Finds the index i in the event array such that event[i].timestamp > t && event[i-1].timestamp <= t
     * It returns events.length if event[events.length-1].timestamp <= t
     * @param t the timestamp to look for
     * @return the index of the correct event
     */
    private int binarySearch(double t) {
        return binarySearch(0, events.size() - 1, t);
    }

    /**
     * Recursive implementation of binary search
     * @param t the timestamp to look for
     * @return the index of the correct event
     */
    private int binarySearch(int l, int r, double t)
    {
        if (r >= l) {
            int mid = l + (r - l) / 2;

            // check the special case at the beginning
            if (mid == 0 && events.get(0).get(0).t > t) {
                return 0;
            }

            // check the special cases at the end
            if (mid == events.size() - 2 && events.get(mid+1).get(0).t <= t) { // -2 because mid calculation will always be rounded down
                return events.size();
            }
            if (events.get(mid).get(0).t <= t && events.get(mid+1).get(0).t > t) {
                return mid+1;
            }

            // recurse on left or right part of the array
            if (events.get(mid).get(0).t > t) {
                return binarySearch(l, mid, t);
            } else {
                return binarySearch(mid, r, t);
            }
        }
        // Whenever this happens, there is a bug in the binary search.
        // It should find the answer in all cases before r < l
        throw new IllegalStateException("r < l, this should never happen.");
    }

    @Override
    public List<Event> getNextEvent(Robot[] robots, double t) {
        while (t >= timestampLastEvent) {
            computeNextEvent(robots, timestampLastEvent );
        }
        return events.get(binarySearch(t));
    }

    @Override
    public void addEvent(Event e) {
        int index = binarySearch(e.t);
        int indexSameRobotAfter = -1;
        for (int i = index; i < events.size(); i++) {
            for (Event event: events.get(i)) {
                if (event.r == e.r){
                    indexSameRobotAfter = i;
                    break;
                }
            }
        }
        int indexSameRobotBefore = -1; // or with the same timestamp
        for (int i = index - 1; i >= 0; i--) {
            for (Event event: events.get(i)) {
                if (event.r == e.r) {
                    indexSameRobotBefore = i;
                    break;
                }
            }
        }


        // check if the event falls strictly in between
        if (indexSameRobotBefore != -1 && events.get(indexSameRobotBefore).get(0).t < e.t) {
            // if it does not break the chain of events (START_COMPUTE, START_MOVE, END_MOVE), insert it
            // it should be the eventType after the previous for the same robot, but it should be the same type as the one
            // it will be switched for.
            if (indexSameRobotAfter != -1 && EventType.next(events.get(indexSameRobotBefore).get(0).type) == e.type && e.type == events.get(indexSameRobotAfter).get(0).type) { // it is a correct insertion
                // insert e and remove the event at indexSameRobotAfter. Shift everything in between
                if (indexSameRobotAfter - index >= 0) {
                    System.arraycopy(events, index, events, index + 1, indexSameRobotAfter - index);
                }
                ArrayList<Event> newEvent = new ArrayList<>();
                newEvent.add(e);
                events.add(index, newEvent);
            }
        }
        // in case the event is not strictly in between the other events
        // for the same robot, but instead it has the same timestamp as an already defined event in the list,
        // this will always break the natural chain of events
    }
}
