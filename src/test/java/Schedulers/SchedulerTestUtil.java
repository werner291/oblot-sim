package Schedulers;

import Algorithms.Robot;
import Schedulers.Event;
import Schedulers.EventType;
import Schedulers.Scheduler;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SchedulerTestUtil {
    static void addRandomArrivalToSchedule(Random r, Scheduler scheduler, List<Event> events, EventType expectedType, double after) {
        for (Event event : events) {
        // Events may only be returned together if they occur at the same time.
            assertEquals(event.t, after);

            if (expectedType == EventType.START_MOVING) {
                // Tell the scheduler that the robot will stop moving at a random time strictly in the future.
                scheduler.addEvent(new Event(EventType.END_MOVING, after + r.nextDouble() + 0.1, event.r));
            }
        }
    }

    static void assertOneEventForEachRobot(Robot[] robots, List<Event> events) {
        // Convert the list of robots to a set for faster lookup.
        // This will allow us to cross the robots off one by one to make sure we see all of them exactly once.
        Set<Robot> checkAllRobots = new HashSet<>(Arrays.asList(robots));

        for (Event event : events) {
            // Check the robot off the set of robots we expect to see.
            assertTrue(checkAllRobots.remove(event.r));
        }

        // Make sure we have all robots (required condition for FSYNC)
        assertTrue(checkAllRobots.isEmpty());
    }

    static void assertAllOfType(List<Event> events, EventType expectedType) {
        for (Event event : events) {
            // Check to make sure all events are of the right type.
            assertEquals(expectedType, event.type);
        }
    }
}
