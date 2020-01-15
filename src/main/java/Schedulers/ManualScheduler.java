package Schedulers;

import Simulator.Simulator;
import Simulator.Robot;
import Simulator.State;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class ManualScheduler extends ListScheduler {

    private Simulator sim;
    private final double TIME_OFFSET = 0.001;

    public ManualScheduler(Simulator sim) {
        this.sim = sim;
    }

    @Override
    public void addEvent(Event e) {
        List<Event> toAdd = new ArrayList<>();
        toAdd.add(e);


        Event nextEventForRobot = events.stream().filter(event -> event.r.equals(e.r) && event.t >= e.t).findFirst().orElse(null);
        if (nextEventForRobot == null) {
            if (e.t == 0) {
                e.t += TIME_OFFSET;
            }
            events = merge(events, toAdd);
        } else {
            EventType nextEventType = nextEventForRobot.type;
            // if the time is 0, or if we want to add a following event on the same timestamp, we shift it s little
            if (e.t == 0 || (EventType.next(nextEventType) == e.type && e.t == nextEventForRobot.t)) {
                e.t += TIME_OFFSET;
            }

            if (nextEventType == e.type) { // if the event is of the same type, replace it
                // remove all events for this robot and add the new one
                List<Event> eventsForThisRobot = events.stream().filter(event -> event.r.equals(e.r)).collect(Collectors.toList());
                events.removeAll(eventsForThisRobot);
                events = merge(events, toAdd);

            // in this case, an event has been added at the same timestamp as the previous event for this robot. This is not allowed to happen, so we offset it
            } else if (EventType.next(nextEventType) == e.type && e.t == nextEventForRobot.t + TIME_OFFSET) {
                // in this case, we do not need to remove anything.
                events = merge(events, toAdd);
            }
        }

        // if the event added has the same timestamp as the simulator is currently at, set the simulator back in time
//        if (e.t == sim.getTime()) {
//            Robot[] robots = sim.getRobots();
//            List<CalculatedEvent> eventsList = sim.getCalculatedEvents();
//            // remove the last calcEvent from the eventlist
//            if (eventsList.size() != 0) {
//                // possibly multiple events get added with the same timestamp. Then we only want to remove the last calculated event once
//                if (eventsList.get(eventsList.size() - 1).events.get(0).t == e.t) {
//                    CalculatedEvent removed = eventsList.remove(eventsList.size() - 1);
//                    // for every event in the removed calculatedEvent, set the state of the robot back
//                    List<Event> backwardsEvents = removed.events;
//                    Collections.reverse(backwardsEvents);
//                    for (Event removedEvent : backwardsEvents) { // backwards because one robot can have multiple events in one calculatedevent
//                        Robot eventRobot = Arrays.stream(robots).filter(r -> r.equals(removedEvent.r)).findFirst().get();
//                        if (!Arrays.asList(sim.getRobots()).contains(eventRobot)) {
//                            throw new IllegalStateException();
//                        }
//                        switch (removedEvent.type) {
//                            case START_COMPUTE:
//                                eventRobot.state = State.SLEEPING;
//                                break;
//                            case START_MOVING:
//                                eventRobot.state = State.COMPUTING;
//                                break;
//                            case END_MOVING:
//                                eventRobot.state = State.MOVING;
//                                break;
//                        }
//                    }
//                }
//            }
//            double time = sim.getTime() - Double.MIN_VALUE; // set the time back a minimum amount
//            sim.setState(robots, eventsList, time);
//        }
    }
}
