package Schedulers;

import Algorithms.Robot;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class FSyncSchedulerTest {

    /**
     * An FSYNC schedule should consist of an alternation between START_COMPUTE and START_MOVING schedule events.
     */
    @Test
    void testIsFullySynchronous() {

        Random r = new Random(1337);

        // The robots given here are just a simple cloud of 5 robots.
        Robot[] robots = TestUtil.generateRobotCloud(TestUtil.GO_TO_COG, 10.0, 5);

        // Get an FSyncScheduler to test.
        Scheduler scheduler = new FSyncScheduler();

        // Start at time 0.
        double t = 0.0;

        // Run for a hundred iterations.
        for (int i = 0; i < 100; i++) {

            // Check when the next event occurs.
            List<Event> events = scheduler.getNextEvent(robots, t);

            // Expect a strict alternation between START_COMPUTE and START_MOVING
            EventType expectedType = i % 2 == 0 ? EventType.START_COMPUTE : EventType.START_MOVING;

            // Get the timestamp of the first event and make sure it's strictly in the future.
            double eventT = events.get(0).t;
            assertTrue(eventT > t);

            SchedulerTestUtil.assertAllOfType(events, expectedType);

            SchedulerTestUtil.assertOneEventForEachRobot(robots, events);

            SchedulerTestUtil.addRandomArrivalToSchedule(r, scheduler, events, expectedType, eventT);

            for (int j = 0; j < 50; j++) {
                // Poke at the schedule at random times to make sure there aren't wierd state bugs.
                // This may be in the future!
                List<Event> events1 = scheduler.getNextEvent(robots, r.nextDouble() * eventT * 1.01);
                // Make sure we have one for each robot.
                SchedulerTestUtil.assertOneEventForEachRobot(robots, events1);
                // Make sure they're all of the same type.
                SchedulerTestUtil.assertAllOfType(events1, events1.get(0).type);
            }

            // Jump into the future until the events occur.
            t = eventT;
        }
    }

}