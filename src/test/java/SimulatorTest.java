import nl.tue.oblotsim.Schedulers.FSyncScheduler;
import nl.tue.oblotsim.Schedulers.TestUtil;
import nl.tue.oblotsim.Simulator.Simulation;
import nl.tue.oblotsim.Simulator.Robot;
import nl.tue.oblotsim.Util.Config;
import nl.tue.oblotsim.Util.Vector;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SimulatorTest {

    @Test
    @Timeout(5)
    @Disabled
    void testGatherAtCOGFSync() {

        /*
         * A description of a simulation of a simple gathering algorithm in the FSYNC scheduler.
         */

        // Put two robots at random positions.
        Robot[] robots = TestUtil.generateRobotCloud(TestUtil.GO_TO_COG, 1.0, 5);

        // Under an FSYNC scheduler, we expect the robots to eventually reach the center of gravity.
        // `.get()` is safe since we know there is at least one robot.
        //noinspection OptionalGetWithoutIsPresent
        Vector expected = Arrays.stream(robots).map(robot -> robot.getPos()).reduce((vA, vB) -> vA.add(vB)).get().mult(1 / (double) robots.length);

        // Initialize the simulator.
        // Multiplicity doesn't matter, but we set it to true since this is a simpler option.
        // Infinite visibility, since we want things unrestricted.
        Config config = new Config(true, Double.POSITIVE_INFINITY, true);

        // Perhaps consider making this a single static method?
        Simulation sim = new Simulation(config, List.of(robots), new FSyncScheduler());

        // Run until it all stops. Note that this implies that it stops.
        sim.simulateTillTimestamp(Double.POSITIVE_INFINITY);

        // Expect the robots to have gathered exactly at the COG.
        for (Robot r : robots) {
            assertEquals(robots[0].getPos(), r.getPos());
        }

    }

}