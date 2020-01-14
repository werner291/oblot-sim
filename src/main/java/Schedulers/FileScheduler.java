package Schedulers;

import Simulator.Robot;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;
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
public class FileScheduler extends ListScheduler {

    /**
     * Reads a file and stores an ordered array of events. Does this by reading a file and then merging it with the total
     * This is not the most efficient way, as insertion per event would be better. However, this was easier to implement
     * and it will only be done once anyway.
     *
     * @param file the file this scheduler should load
     * @param robots the list of robots for which this scheduler needs to load the schedule
     */
    public FileScheduler(File file, Robot[] robots) throws FileNotFoundException, IllegalArgumentException {
        String filePath = file.getAbsolutePath(); // used for printing
        try {
            int lineIndex = 0;
            Scanner scanner = new Scanner(file);
            while (scanner.hasNextLine()) { // while there are still lines
                if (lineIndex >= robots.length) {
                    throw new IllegalArgumentException(String.format("The amount of lines in the schedule file %s does not match " +
                            "the amount of given robots! (more lines)", filePath));
                }

                String line = scanner.nextLine();
                // convert to array of timestamps
                double[] timestamps = Arrays.stream(line.split(", ")).mapToDouble(Double::parseDouble).toArray();
                if (!isSorted(timestamps)) {
                    throw new IllegalArgumentException("The timestamps are not in increasing order");
                }

                List<Event> eventsForRobot = new ArrayList<>(timestamps.length);
                EventType currentType = EventType.START_COMPUTE;
                for (double timestamp : timestamps) {
                    eventsForRobot.add(new Event(currentType, timestamp, robots[lineIndex]));
                    currentType = EventType.next(currentType);
                }
                events = merge(events, eventsForRobot);
                lineIndex++;
            }

            if (lineIndex != robots.length) {
                throw new IllegalArgumentException(String.format("The amount of lines in the schedule file %s does not match " +
                        "the amount of given robots! (more robots)", filePath));
            }


        } catch (FileNotFoundException e) {
            throw new FileNotFoundException(String.format("File not found: %s. Does it exists and does it have read access?", filePath));
        } catch (NumberFormatException e) {
            throw new NumberFormatException("The file does not have the correct format.");
        }
    }

    /**
     * Checks if an array of timestamps is sorted. Same ints are allowed, but they should be next to each other.
     * @param array the array of ints
     * @return if the array is sorted
     */
    private boolean isSorted(double[] array) {
        double previous = Double.NEGATIVE_INFINITY;
        for (double value : array) {
            if (previous > value) {
                return false;
            }
            previous = value;
        }
        return true;
    }
}
