package Schedulers;

import Algorithms.Robot;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.stream.IntStream;

/**
 * Scheduler that reads from a file input. The input should have the following format:<br>
 * a1, b1, c1, a2, b2, c2, ...<br>
 * a3, b3, c3, ...<br>
 * ...<br>
 * Where these are all integer timestamps and:<br>
 * a = start compute<br>
 * b = start moving<br>
 * c = end moving<br>
 * Every line is a new robot. The indices are the same as for the robots array given on creation.
 */
public class FileScheduler extends Scheduler {

    /**
     * An array that contains all events that will happen, sorted on timestamp
     */
    private Event[] events = new Event[0];

    /**
     * The index in the {@link FileScheduler#events} array we last accessed. This is useful because
     * sequential access is the most likely use case.
     * If this is events.length, this means that the last timestamp requested was after the last event
     */
    private int currentIndex;

    /**
     * Reads a file and stores an ordered array of events. Does this by reading a file and then merging it with the total
     * This is not the most efficient way, as insertion per event would be better. However, this was easier to implement
     * and it will only be done once anyway.
     * @param filePath the path of the file this scheduler should load
     * @param robots the list of robots for which this scheduler needs to load the schedule
     */
    public FileScheduler(String filePath, Robot[] robots) {
        try {

            int lineIndex = 0;
            Scanner scanner = new Scanner(new File(filePath));
            while (scanner.hasNextLine()) { // while there are still lines
                if (lineIndex >= robots.length) {
                    throw new IllegalArgumentException(String.format("The amount of lines in the schedule file %s does not match " +
                            "the amount of given robots! (more lines)", filePath));
                }

                String line = scanner.nextLine();
                // convert to array of timestamps
                int[] timestamps = Arrays.stream(line.split(", ")).mapToInt(Integer::parseInt).toArray();

                Event[] eventsForRobot = new Event[timestamps.length];
                Event.EventType currentType = Event.EventType.START_COMPUTE;
                for (int i = 0; i < timestamps.length; i++) {
                    eventsForRobot[i] = new Event(currentType, timestamps[i], robots[lineIndex]);
                    currentType = Event.EventType.next(currentType);
                }
                events = merge(events, eventsForRobot);
                lineIndex++;
            }

            if (lineIndex != robots.length) {
                throw new IllegalArgumentException(String.format("The amount of lines in the schedule file %s does not match " +
                        "the amount of given robots! (more robots)", filePath));
            }


        } catch (FileNotFoundException e) {
            System.err.println(String.format("File not found: %s", filePath));
            System.err.println("Does it exists and does it have read access?");
        } catch (NumberFormatException e) {
            System.err.println("The file does not have the correct format.");
        } catch (IllegalArgumentException e) {
            System.err.println(e.getMessage());
        }
    }

    /**
     * Merge two event arrays to be sorted on timestamp. Assume a and b are already sorted.
     * @param a the first event array
     * @param b the second event array
     * @return an event array sorted on timestamp
     */
    private Event[] merge(Event[] a, Event[] b) {
        boolean ASorted = IntStream.range(0, a.length - 1).noneMatch(i -> a[i].timeStamp > a[i + 1].timeStamp);
        boolean BSorted = IntStream.range(0, b.length - 1).noneMatch(i -> b[i].timeStamp > b[i + 1].timeStamp);
        if (!ASorted) {
            throw new IllegalArgumentException("Array a is not sorted");
        }
        if (!BSorted) {
            throw new IllegalArgumentException("Array b is not sorted");
        }

        Event[] result = new Event[a.length + b.length];
        int i = 0; // index in a
        int j = 0; // index in b
        for (int k = 0; k < result.length; k++) { // k is the index in the result array
            if (i == a.length) { // just add b
                result[k] = b[j];
                j++;
            } else if (j == b.length) { // just add a
                result[k] = a[i];
                i++;
            } else {
                // cannot happen both because k = a.length + b.length, so in this case we have
                // i < a.length && j < b.length
                if (a[i].timeStamp < b[j].timeStamp) {
                    result[k] = a[i];
                    i++;
                } else { // in case they are equal it does not matter
                    result[k] = b[j];
                    j++;
                }
            }
        }
        return result;
    }

    /**
     * Finds the index i in the event array such that event[i].timestamp > t && event[i-1].timestamp <= t or -1
     * It returns events.length if event[events.length-1].timestamp <= t
     * @param t the timestamp to look for
     * @return the index of the correct event
     */
    private int binarySearch(double t) {
        return binarySearch(0, events.length - 1, t);
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
            if (mid == 0 && events[0].timeStamp > t) {
                return 0;
            }

            // check the special cases at the end
            if (mid == events.length - 2 && events[mid+1].timeStamp <= t) { // -2 because mid calculation will always be rounded down
                return events.length;
            }
            if (events[mid].timeStamp <= t && events[mid+1].timeStamp > t) {
                return mid+1;
            }

            // recurse on left or right part of the array
            if (events[mid].timeStamp > t) {
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
        if (currentIndex == events.length && events[currentIndex-1].timeStamp <= t) {
            return null;
        }
        if (currentIndex == events.length - 1 && events[currentIndex].timeStamp <= t) {
            currentIndex++;
            return null;
        }
        if (currentIndex == 0 && events[currentIndex].timeStamp > t) {
            return getSameEvents(currentIndex);
        }
        // if there are multiple events with the same timestamp, increase currentIndex until the last one
        while (currentIndex != events.length - 1 && events[currentIndex].timeStamp == events[currentIndex+1].timeStamp) {
            currentIndex++;
        }

        // check the easy case if the next is the following event
        if (events[currentIndex].timeStamp <= t && events[currentIndex + 1].timeStamp > t) {
            return getSameEvents(++currentIndex);
        }

        // If the checks with the currentIndex fail, fall back to binary search
        currentIndex = binarySearch(t);
        if (currentIndex == events.length) {
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
        while (i < events.length && events[i].timeStamp == events[start].timeStamp) {
            eventsFound.add(events[i]);
            i++;
        }
        return eventsFound;
    }

}
