package Schedulers;

import Algorithms.Robot;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import static Schedulers.EventType.START_MOVING;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SchedulerTest {

    /**
     * An FSYNC schedule should consist of an alternation between START_COMPUTE and START_MOVING schedule events.
     */
    @Test
    void testIsSynchronous() {
        testScheduler(new SyncScheduler(),
                (robots, events1) -> {
                    // Each new set of events must strictly be of the same type.
                    assertAllOfType(events1, events1.get(0).type);
                }
        );
    }

    /**
     * An FSYNC schedule should consist of an alternation between START_COMPUTE and START_MOVING schedule events.
     */
    @Test
    void testIsFullySynchronous() {

        testScheduler(new FSyncScheduler(),
                (robots, events1) -> {
                    // Each new set of events must strictly be of the same type.
                    assertAllOfType(events1, events1.get(0).type);
                    // Must come with exactly one event for each robot under FSYNC
                    assertOneEventForEachRobot(robots, events1);
                }
        );
    }

    /**
     *
     * @param r         Random number generator to use (so you can seed it)
     * @param scheduler The Scheduler being tested.
     * @param events    A list of events occurring during this timestamp
     * @param afterT    The current timestamp that the arrival time must be scheduled after.
     */
    static void addRandomArrivalToSchedule(Random r, Scheduler scheduler, List<Event> events, double afterT) {
        for (Event event : events) {
            // Events may only be returned together if they occur at the same time.
            assertEquals(event.t, afterT);

            if (event.type == EventType.START_MOVING) {
                // Tell the scheduler that the robot will stop moving at a random time strictly in the future.
                scheduler.addEvent(new Event(EventType.END_MOVING, afterT + r.nextDouble() + 0.1, event.r));
            }
        }
    }

    /**
     * Assert that there is exactly one robot for each event.
     *
     * This is mainly used in the FSYNC scheduler check.
     *
     * @param robots Array of robots
     * @param events Array of events.
     */
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

    /**
     * Shorthand method to check whether all events have a given type.
     *
     * This is mainly used for the synchronous schedulers where
     * events of a given type are always synchronized.
     *
     * @param events        List of events to check.
     * @param expectedType  Event type to expect.
     */
    static void assertAllOfType(List<Event> events, EventType expectedType) {
        for (Event event : events) {
            // Check to make sure all events are of the right type.
            assertEquals(expectedType, event.type);
        }
    }

    /**
     *
     * Method that runs a given scheduler through a test environment resembling a simulation and verifies output validity.
     *
     * Robot movement times are randomized and provided to the scheduler.
     *
     * @param scheduler         The scheduler to test.
     * @param checkNewEvents    A callback that can be used to check additional things about individual batches of events.
     */
    static void testScheduler(Scheduler scheduler, BiConsumer<Robot[], List<Event>> checkNewEvents) {

        // Initialize the rng with a seed to make tests reproducible.
        Random r = new Random(1337);

        // The robots given here are just a simple cloud of 50 robots.
        Robot[] robots = TestUtil.generateRobotCloud(TestUtil.GO_TO_COG, 10.0, 50);

        // Start at time 0.
        double t = 0.0;

        // For every robot, expect the first command to be START_COMPUTE
        Map<Robot, EventType> expectedType = Arrays.stream(robots).collect(Collectors.toMap(o -> o, robot -> EventType.START_COMPUTE));

        // Run for a thousand iterations.
        for (int i = 0; i < 1000; i++) {

            // Check when the next event occurs.
            List<Event> events = ((Scheduler) scheduler).getNextEvent(robots, t);

            for (Event event : events) {
                assertEquals(expectedType.get(event.r), event.t);
                // Expect a strict alternation between START_COMPUTE and START_MOVING per robot.
                expectedType.compute(event.r, (robot, eventType) -> eventType == EventType.START_MOVING ? EventType.START_COMPUTE : START_MOVING);
            }

            // Get the timestamp of the first event and make sure it's strictly in the future.
            double eventT = events.get(0).t;
            assertTrue(eventT > t);

            // Run additional checks that may be relevant to the particular type of scheduler.
            checkNewEvents.accept(robots, events);

            // Add any relevant movement end events to the scheduler.
            addRandomArrivalToSchedule(r, scheduler, events, eventT);

            for (int j = 0; j < 50; j++) {
                // Poke at the schedule at random times to make sure there aren't wierd state bugs.
                // This may be in the future!
                List<Event> events1 = ((Scheduler) scheduler).getNextEvent(robots, r.nextDouble() * eventT * 1.01);
                checkNewEvents.accept(robots, events1);
            }

            // Jump into the future until the events occur.
            t = eventT;
        }
    }

}