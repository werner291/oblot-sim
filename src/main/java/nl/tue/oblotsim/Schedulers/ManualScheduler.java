package nl.tue.oblotsim.Schedulers;

import nl.tue.oblotsim.Simulator.Simulation;

import java.util.ArrayList;
import java.util.List;

public class ManualScheduler extends ListScheduler {

    private Simulation sim;
    private final double TIME_OFFSET = 0.001;

    public ManualScheduler(Simulation sim) {
        this.sim = sim;
    }

    @Override
    public void addEvent(Event e) {
        List<Event> toAdd = new ArrayList<>();
        toAdd.add(e);

//        e.t = e.getT() + TIME_OFFSET;
//
//        Event nextEventForRobot = events.stream().filter(event -> event.r.equals(e.r) && event.getT() >= e.getT()).findFirst().orElse(null);
//        if (nextEventForRobot == null) {
//            events = merge(events, toAdd);
//        } else {
//            EventType nextEventType = nextEventForRobot.getType();
//
//            if (nextEventType == e.getType()) { // if the event is of the same type, replace it
//                // remove all events for this robot and add the new one
//                List<Event> eventsForThisRobot = events.stream().filter(event -> event.r.equals(e.r)).collect(Collectors.toList());
//                events.removeAll(eventsForThisRobot);
//                events = merge(events, toAdd);
//
//            // in this case, an event has been added at the same timestamp as the previous event for this robot. This is not allowed to happen, so we offset it
//            } else if (EventType.next(nextEventType) == e.getType() && e.getT() == nextEventForRobot.getT() + TIME_OFFSET) {
//                // in this case, we do not need to remove anything.
//                events = merge(events, toAdd);
//            }
//        }
    }

    /**
     * Resets the event list
     */
    public void reset() {
        this.events = new ArrayList<>();
        currentIndex = 0;
    }
}
