package Schedulers;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ManualScheduler extends ListScheduler {

    @Override
    public void addEvent(Event e) {
        List<Event> toAdd = new ArrayList<>();
        toAdd.add(e);

        Event nextEventForRobot = events.stream().filter(event -> event.r.equals(e.r) && event.t >= e.t).findFirst().orElse(null);
        if (nextEventForRobot == null) {
            events = merge(events, toAdd);
        } else {
            EventType nextEventType = nextEventForRobot.type;
            if (nextEventType == e.type) { // if the event is of the correct type
                // remove all events for this robot and add the new one
                List<Event> eventsForThisRobot = events.stream().filter(event -> event.r.equals(e.r)).collect(Collectors.toList());
                events.removeAll(eventsForThisRobot);
                events = merge(events, toAdd);
            }
        }
    }
}
