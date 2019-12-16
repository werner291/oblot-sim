import Algorithms.*;
import Schedulers.FSyncScheduler;
import Simulator.Simulator;
import Util.Config;
import Util.Vector;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Random;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SimulatorTest {

    /*
     * This algorithm is very simple: Just direct the robot to go to the center of gravity of all robot positions.
     */
    public static final Algorithm GO_TO_COG = new Algorithm() {
        @Override
        public Vector doAlgorithm(Vector[] snapshot) {
            //noinspection OptionalGetWithoutIsPresent (We know the robot itself is present, so the COG is defined)
            return Arrays.stream(snapshot).reduce((vA, vB) -> vA.add(vB)).get().mult(1 / (double) snapshot.length);
        }
    };

    @Test
    void testGatherAtCOGFSync() {

        /*
         * A description of a simulation of a simple gathering algorithm in the FSYNC scheduler.
         */

        // Put two robots at random positions.
        Robot[] robots = (Robot[]) Stream.generate(() -> new Robot(GO_TO_COG, new Vector(Math.random() - 0.5, Math.random() - 0.5), new BasicPositionTransformation())).limit(5).toArray();

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
        //sim.run(Double.POSITIVE_INFINITY);

        // Expect the robots to have gathered exactly at the COG.
        assertEquals(expected, robots[0].pos);
        assertEquals(expected, robots[1].pos);

    }

}