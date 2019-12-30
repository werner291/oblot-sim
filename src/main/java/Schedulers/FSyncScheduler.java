package Schedulers;

import Simulator.Robot;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class FSyncScheduler extends Scheduler {

    private double nextStopTime = 0; // in case the scheduler cannot interrupt the robots, we have to honor the next stop time
    static Random random = new Random();

    private double min;
    private double max;

    /**
     * Creates a new FSync Scheduler.
     * @param min the minimum time between events
     * @param max the maximum time between events
     */
    public FSyncScheduler(double min, double max) {
        this.min = min;
        this.max = max;
    }

    public FSyncScheduler() {
        this(0.5, 1.5);
    }

    @Override
    public List<Event> getNextEvent(Robot[] robots, double t) {
        for (Robot r : robots) {
            if (r.state != robots[0].state) {
                throw new IllegalStateException("FSyncScheduler cannot operate on robots that do not have the same state.");
            }
        }
        EventType nextType = null;
        switch (robots[0].state) {
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
        if (nextType == EventType.END_MOVING && t < nextStopTime) {
            return generateEvents(robots, nextStopTime, EventType.END_MOVING);
        }
        double nextT = t + (random.nextDouble() * (max - min)) + min;
        return generateEvents(robots, nextT, nextType);
    }

    /**
     * Generate a list of the same events for all robots
     * @param robots the robots
     * @param timestamp the timestamp of the events
     * @param type the type of the events
     * @return a list of the generated events
     */
    private List<Event> generateEvents(Robot[] robots, double timestamp, EventType type) {
        List<Event> eventList = new ArrayList<>();
        for (Robot r : robots) {
            eventList.add(new Event(type, timestamp, r));
        }
        return eventList;
    }

    @Override
    public void addEvent(Event e) {
        if (e.type == EventType.END_MOVING && e.t > nextStopTime) {
            nextStopTime = e.t;
        }
    }
}
