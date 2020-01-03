package Schedulers;

import Simulator.Robot;
import Simulator.State;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class SchedulerTest {

    @Test
    void fileSchedulerTest() {

        Random rng = new Random(1337);

        // Generate a set of robots (really only the object Ids matter)
        Robot[] robots = TestUtil.generateRobotCloud(TestUtil.DO_NOTHING, 10.0, 50);

        // Pre-generate a schedule for each robot.
        Map<Robot, SortedSet<Event>> schedule = makeRandomFixedSchedule(rng, robots);

        // Get the maximum event time to determine a sample range.
        //noinspection OptionalGetWithoutIsPresent Exception can be ignored since we always have some events in the schedule.
        double maxT = schedule.values().stream().mapToDouble(events -> events.last().t).max().getAsDouble();

        try {
            // Create a temp file.
            File file = File.createTempFile("test_schedule", ".csv");
            scheduleToFile(robots, schedule, file);

            {
                // Now, load the same file in the FileScheduler.
                Scheduler scheduler = new FileScheduler(file, robots);

                for (int i = 0; i < 1000; i++) {
                    // Take 1000 random timestamps.
                    double sampleT = rng.nextDouble() * maxT * 1.01;
                    probeScheduleCorrectness(robots, schedule, scheduler, sampleT);
                }
            }

            // Run the regular scheduler test as well, using a new instance of the scheduler since running this test
            // adds movement stop events to the schedule.
            SchedulerTest.testScheduler(new FileScheduler(file, robots), (robots1, events) -> { /* No additional checks. */ });

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void probeScheduleCorrectness(Robot[] robots, Map<Robot, SortedSet<Event>> schedule, Scheduler scheduler, double sampleT) {
        // Ask the scheduler which events are next.
        List<Event> events = scheduler.getNextEvent(robots, sampleT);

        if (events == null) {
            for (Robot robot : robots) {
                assertTrue( schedule.get(robot).last().t <= sampleT);
            }
        } else {
            for (Event event : events) {
                // In the schedule, for the given robot, find the first event with a timestamp strictly greater
                // than the sample time.
                //noinspection OptionalGetWithoutIsPresent Throwing an exception here is fine since it'll fail like the test like it should.
                Event expected = schedule.get(event.r).tailSet(event).stream().filter(event1 -> event1.t > sampleT).findFirst().get();

                // We should expect the event in the schedule to match with the event from the FileScheduler.
                // We do inexact comparison to account for rounding errors while enconding/decoding.
                assertTrue(Math.abs(event.t - expected.t) < 0.000001);
            }
        }
    }

    private void scheduleToFile(Robot[] robots, Map<Robot, SortedSet<Event>> schedule, File file) {
        // For each robot, write the schedule to it.
        try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
            for (Robot robot : robots) {
                for (Iterator<Event> iterator = schedule.get(robot).iterator(); iterator.hasNext(); ) {
                    Event event = iterator.next();
                    writer.print(event.t);
                    if (iterator.hasNext()) {
                        writer.print(", ");
                    }
                }
                writer.println();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Map<Robot, SortedSet<Event>> makeRandomFixedSchedule(Random rng, Robot[] robots) {
        return Arrays.stream(robots).collect(Collectors.toMap(Function.identity(), (Robot robot) -> {

            // Pick a starting time for the first event.
            double t = rng.nextDouble() * 10.0;

            // Determine the number of events in the schedule.
            int numEvents = rng.nextInt(500);

            // Create a sorted set of events.
            TreeSet<Event> events = new TreeSet<>(Comparator.comparing((Event o) -> o.t));

            // Generate the events in sequence
            for (int i = 0; i < numEvents; i++) {
                // Add an event with random type at timestamp t.
                // TODO: Simulate possibly missing event types? Need to clarify.
                // We could use null event types for that?
                events.add(new Event(EventType.values()[i%3], t, robot));
                // Advance by random time delta
                t += rng.nextDouble() + 0.01;
            }

            // Freeze the set so we don't modify the schedule accidentally.
            return Collections.unmodifiableSortedSet(events);
        }));
    }

    /**
     * An FSYNC schedule should consist of an alternation between START_COMPUTE and START_MOVING schedule events.
     */
    @Test
    void testIsSynchronous() {
        testScheduler(new SSyncScheduler(),
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
     * <p>
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
     * <p>
     * This is mainly used for the synchronous schedulers where
     * events of a given type are always synchronized.
     *
     * @param events       List of events to check.
     * @param expectedType Event type to expect.
     */
    static void assertAllOfType(List<Event> events, EventType expectedType) {
        for (Event event : events) {
            // Check to make sure all events are of the right type.
            assertEquals(expectedType, event.type);
        }
    }

    /**
     * Method that runs a given scheduler through a test environment resembling a simulation and verifies output validity.
     * <p>
     * Robot movement times are randomized and provided to the scheduler.
     *
     * @param scheduler      The scheduler to test.
     * @param checkNewEvents A callback that can be used to check additional things about individual batches of events.
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
            List<Event> events = scheduler.getNextEvent(robots, t);

            for (Event event : events) {
                // Expect the ordering of event types to be correct.
                assertEquals(expectedType.get(event.r), event.type);
                // Events returned by getNextEvent must occur at the same timestep.
                assertEquals(event.t, events.get(0).t);

                expectedType.compute(event.r, (robot, eventType) -> {
                    assertNotNull(eventType);
                    return EventType.next(eventType);
                });
            }

            // Get the timestamp of the first event and make sure it's strictly in the future.
            double eventT = events.get(0).t;
            assertTrue(eventT > t);

            for (Event event : events) {
                switch (event.type) {

                    case START_COMPUTE:
                        event.r.state = State.COMPUTING;
                        break;
                    case START_MOVING:
                        event.r.state = State.MOVING;
                        break;
                    case END_MOVING:
                        event.r.state = State.SLEEPING;
                        break;
                }
            }

            // Run additional checks that may be relevant to the particular type of scheduler.
            checkNewEvents.accept(robots, events);

            // Add any relevant movement end events to the scheduler.
            addRandomArrivalToSchedule(r, scheduler, events, eventT);

            for (int j = 0; j < 50; j++) {
                // Poke at the schedule at random times to make sure there aren't weird state bugs.
                // This may be in the future!
                List<Event> events1 = scheduler.getNextEvent(robots, r.nextDouble() * eventT * 1.01);
                checkNewEvents.accept(robots, events1);
            }

            // Jump into the future until the events occur.
            t = eventT;
        }
    }

}