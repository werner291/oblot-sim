package Schedulers;

import Simulator.Robot;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A scheduler that produces events based on a list of events.
 */
public class ListScheduler extends Scheduler {

    /**
     * An array that contains all events that will happen, sorted on timestamp
     */
    protected List<Event> events = new ArrayList<>();

    /**
     * The index in the {@link ListScheduler#events} array we last accessed. This is useful because
     * sequential access is the most likely use case.
     * If this is events.size, this means that the last timestamp requested was after the last event
     */
    private int currentIndex;

    /**
     * Check if a list of events is sorted on timestamp
     * @param l the list
     * @return true if the list is sorted on timestamp, false otherwise
     */
    private boolean isSorted(List<Event> l) {
        Event lastEvent = null;
        for (Event e : l) {
            if (lastEvent == null) {
                lastEvent = e;
            } else if (lastEvent.t > e.t) {
                return false;
            }
        }
        return true;
    }

    /**
     * Merge two event lists to be sorted on timestamp. Assume a and b are already sorted.
     * @param a the first event list
     * @param b the second event list
     * @return an event array sorted on timestamp
     */
    protected List<Event> merge(List<Event> a, List<Event> b) {
        if (!isSorted(a)) {
            throw new IllegalArgumentException("Array a is not sorted");
        }
        if (!isSorted(b)) {
            throw new IllegalArgumentException("Array b is not sorted");
        }

        List<Event> result = new ArrayList<>(a.size() + b.size());

        int i = 0; // index in a
        int j = 0; // index in b
        for (int k = 0; k < a.size() + b.size(); k++) { // k is the index in the result array
            if (i == a.size()) { // just add b
                result.add(b.get(j));
                j++;
            } else if (j == b.size()) { // just add a
                result.add(a.get(i));
                i++;
            } else {
                // cannot happen both because k = a.length + b.length, so in this case we have
                // i < a.length && j < b.length
                if (a.get(i).t < b.get(j).t) {
                    result.add(a.get(i));
                    i++;
                } else { // in case they are equal it does not matter
                    result.add(b.get(j));
                    j++;
                }
            }
        }
        return result;
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
            if (mid == 0 && events.get(0).t > t) {
                return 0;
            }

            // check the special cases at the end
            if (mid == events.size() - 2 && events.get(mid+1).t <= t) { // -2 because mid calculation will always be rounded down
                return events.size();
            }
            if (events.get(mid).t <= t && events.get(mid+1).t > t) {
                return mid+1;
            }

            // recurse on left or right part of the array
            if (events.get(mid).t > t) {
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
        // There can be many events, while the most likely access is in sequential order.
        // Therefore, we maintain the current index and first check if it is the next one.
        // In all other cases, we do a binary search.

        if (currentIndex == events.size() && events.get(currentIndex - 1).t <= t) {
            return null;
        }
        if (currentIndex == events.size() - 1 && events.get(currentIndex).t <= t) {
            currentIndex++;
            return null;
        }
        if (currentIndex == 0 && events.get(currentIndex).t > t) {
            return getSameEvents(currentIndex);
        }

        if (currentIndex != events.size()) {
            // if there are multiple events with the same timestamp, increase currentIndex until the last one
            while (currentIndex < events.size() - 1 && events.get(currentIndex).t == events.get(currentIndex + 1).t) {
                currentIndex++;
            }
            // if we now are at the end of the events, there are no new events anymore.
            if (currentIndex == events.size() - 1) {
                return null;
            }

            // check the easy case if the next is the following event
            if (events.get(currentIndex).t <= t && events.get(currentIndex + 1).t > t) {
                return getSameEvents(++currentIndex);
            }
        }

        // If the checks with the currentIndex fail, fall back to binary search
        currentIndex = binarySearch(t);
        if (currentIndex == events.size()) {
            return null;
        } else {
            return getSameEvents(currentIndex);
        }
    }

    /**
     * From a starting index, it traverses the event array and adds
     * all events with the same timestamp as the event at the starting index to a list
     * @param start the index to start looking
     * @return a list of all events with the same timestamp as the event at index start
     */
    private List<Event> getSameEvents(int start) {
        List<Event> eventsFound = new ArrayList<>();
        int i = start;
        while (i < events.size() && events.get(i).t == events.get(start).t) {
            eventsFound.add(events.get(i));
            i++;
        }
        return eventsFound;
    }

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
                // remove the next event for this robot and add the new one
                events.remove(nextEventForRobot);
                events = merge(events, toAdd);
            }
        }
    }

}
