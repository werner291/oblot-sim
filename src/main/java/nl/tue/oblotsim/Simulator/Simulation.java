package nl.tue.oblotsim.Simulator;

import nl.tue.oblotsim.RobotPaths.RobotPath;
import nl.tue.oblotsim.Schedulers.*;
import nl.tue.oblotsim.Util.Config;
import nl.tue.oblotsim.Util.Vector;

import java.util.*;
import java.util.stream.Collectors;

/**
 * The main simulator. Works by extracting an event list of the schedule,
 * and asking the robots accordingly how they want to handle their current states
 * Is also capable of saving the end result.
 *
 * A simulation is effectively stateless, the only stateful component about it is that it is computed gradually,
 * which effectively corresponds to a lazily-loaded sequence of simulation data.
 */
public class Simulation implements Iterable<CalculatedEvent> {
    /**
     * The {@link Config} for this simulator. Can be changed on the fly.
     */
    public Config config;

    /**
     * The list of events that have happened up till the current simulated timestamp.
     * This should be sorted in the order of timestamps of the inner events.
     * The previous events can be extracted from this.
     */
    private NavigableMap<Double, CalculatedEvent> timeline = new TreeMap<>();

    private List<Event> upcomingEvents;

    /**
     * The {@link Scheduler} that regulates which robots activate when.
     */
    public Scheduler scheduler;

    public Simulation(Config c, Collection<Robot> robots, Scheduler scheduler) {
        this.config = c;
        this.scheduler = scheduler;
        // Making assumption: No events at time 0. Dangerous?
        final List<Robot> snapshot = List.copyOf(robots);
        timeline.put(0.0 , new CalculatedEvent(0.0, List.of(), snapshot));
        upcomingEvents = scheduler.getNextEvent(Collections.unmodifiableList(snapshot), 0.0, c.interuptable);
    }

    /**
     * Find out how much time of the simulation has been computed so far.
     * <p>
     * This may be infinite if nothing needs to be computed anymore according to the current scheduler.
     */
    public double computedTimelineUntil() {
        if (upcomingEvents != null) {
            return timeline.lastKey();
        } else {
            return Double.POSITIVE_INFINITY;
        }
    }

    /**
     * Extend the timeline until it covers at least the given time t.
     *
     * Returns a list of all events that occurred.
     */
    public List<CalculatedEvent> simulateTillTimestamp(double t) {
        List<CalculatedEvent> resultingEvents = new ArrayList<>();
        while (computedTimelineUntil() < t) {
            simulateTillNextEvent().ifPresent(resultingEvents::add);
        }
        return resultingEvents;
    }

    /**
     * Provide the highest-known lower bound on the termination time of the simulation.
     * Termination, here, is defined as no more events occurring and no more robots moving.
     */
    public double highestKnownLastEventTimeLowerBound() {
        return upcomingEvents == null ? timeline.lastEntry().getValue().timeUntilAllStop() : upcomingEvents.get(0).getT();
    }

    /**
     * Extend the currently calculated timeline with the next upcoming event.
     *
     * @return Wether a new event was actually computed (as opposed to no new events being available)
     */
    public Optional<CalculatedEvent> simulateTillNextEvent() {

        // Up no new events are coming up, don't compute any new events.
        if (upcomingEvents == null) {
            return Optional.empty();
        }

        // Get the time at which the events will occur.
        double eventsTime = upcomingEvents.get(0).getT();

        Map<Integer, Robot> robots = timeline.lastEntry().getValue().getSnapshot().values().stream()
                .map(robot -> robot.extrapolatedToTime(eventsTime))
                .collect(Collectors.toMap(Robot::getId, robot -> robot));

        Vector[] snapshot = robots.values().stream().map(robot -> robot.getPos()).toArray(Vector[]::new);

        for (Event event : upcomingEvents) {
            robots.put(event.getTargetId(), applyEventToRobot(event, robots.get(event.getTargetId()), snapshot));
        }

        final CalculatedEvent newEvent = new CalculatedEvent(eventsTime, upcomingEvents, robots.values());
        assert !timeline.containsKey(eventsTime);
        timeline.put(eventsTime, newEvent);

        //noinspection Convert2MethodRef
        upcomingEvents = scheduler.getNextEvent(List.copyOf(robots.values()), eventsTime, config.interuptable);
        assert upcomingEvents.get(0).getT() > eventsTime;

        return Optional.of(newEvent);
    }

    private Robot applyEventToRobot(Event event, Robot robot, Vector[] snapshot) {
        assert event.getTargetId() == robot.getId();

        State newState;
        RobotPath newPath;

        switch (event.getType()) {
            case START_COMPUTE:
                assert robot.getState() == State.SLEEPING;
                newPath = robot.calculate(snapshot);
                assert newPath != null;
                newState = State.COMPUTING;
                break;
            case START_MOVING:
                assert robot.getState() == State.COMPUTING;
                newPath = robot.getPath();
                assert newPath != null;
                newState = State.MOVING;
                break;
            case END_MOVING:
                assert robot.getState() == State.MOVING;
                newPath = null;
                newState = State.SLEEPING;
                break;
            default:
                throw new IllegalStateException("Switch is supposed to be exhaustive.");
        }

        return new Robot(robot.getId(),
                robot.getAlgorithm(),
                robot.getPos(),
                robot.getTrans(),
                newPath,
                newState,
                robot.getSpeed(),
                event.getT());
    }

    /**
     * Returns the list of calculated events
     *
     * @return the list of calculated events
     */
    public NavigableMap<Double, CalculatedEvent> getTimeline() {
        return Collections.unmodifiableNavigableMap(timeline);
    }

    public List<Robot> robotsAtTime(double timestamp) {

        simulateTillTimestamp(timestamp);

        final CalculatedEvent evt = timeline.floorEntry(timestamp).getValue();

        return evt.getSnapshot().values().stream().map(robot -> robot.movedTo(robot.positionAtTimeWithoutStateChange(timestamp))).collect(Collectors.toList());
    }

    class SimulationIterator implements Iterator<CalculatedEvent> {

        CalculatedEvent next = timeline.isEmpty() ? simulateTillNextEvent().orElse(null) : timeline.firstEntry().getValue();

        @Override
        public boolean hasNext() {
            return next != null;
        }

        @Override
        public CalculatedEvent next() {
            CalculatedEvent toReturn = next;

            if (computedTimelineUntil() <= toReturn.getTimestamp()) {
                simulateTillNextEvent();
            }

            Map.Entry<Double, CalculatedEvent> lastEntry = timeline.tailMap(toReturn.getTimestamp(), false).firstEntry();
            next = lastEntry == null ? null : lastEntry.getValue();

            return toReturn;
        }
    }

    @Override
    public Iterator<CalculatedEvent> iterator() {
        return new SimulationIterator();
    }
}

