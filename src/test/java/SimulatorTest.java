import Algorithms.*;
import Schedulers.FSyncScheduler;
import Schedulers.TestUtil;
import Simulator.Simulator;
import Util.Config;
import Util.Vector;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SimulatorTest {

    @Test
    void testGatherAtCOGFSync() {

        /*
         * A description of a simulation of a simple gathering algorithm in the FSYNC scheduler.
         */

        // Put two robots at random positions.
        Robot[] robots = TestUtil.generateRobotCloud(TestUtil.GO_TO_COG, 1.0, 5);

        // Under an FSYNC scheduler, we expect the robots to eventually reach the center of gravity.
        // `.get()` is safe since we know there is at least one robot.
        //noinspection OptionalGetWithoutIsPresent
        Vector expected = Arrays.stream(robots).map(robot -> robot.pos).reduce((vA, vB) -> vA.add(vB)).get().mult(1 / (double) robots.length);

        // Initialize the simulator.
        // Multiplicity doesn't matter, but we set it to true since this is a simpler option.
        // Infinite visibility, since we want things unrestricted.
        Config config = new Config(true, Double.POSITIVE_INFINITY);

        // Perhaps consider making this a single static method?
        Simulator sim = new Simulator(config, robots, new FSyncScheduler());

        // Run until it all stops. Note that this implies that it stops.
        sim.simulateTillTimestamp(Double.POSITIVE_INFINITY);

        // Expect the robots to have gathered exactly at the COG.
        assertEquals(expected, robots[0].pos);
        assertEquals(expected, robots[1].pos);

    }

}