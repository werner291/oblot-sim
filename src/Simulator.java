import Algorithms.Robot;
import Algorithms.Vector;

import java.util.Arrays;

/**
 * The main simulator. Works by extracting an event list of the schedule,
 * and asking the robots accordingly how they want to handle their current states
 * Is also capable of saving the end result.
 */
public class Simulator {
    /**
     * The {@link Config} for this simulator. Can be changed on the fly.
     */
    public Config config;

    /**
     * The robots that will be simulated
     */
    private Robot[] robots;

    public Simulator(Config c, Robot[] robots) {
        this.config = c;
        this.robots = robots;
    }

    /**
     * Get a snapshot of the current view (in global coordinates) for a specific robot, given that it has a visibility and a current position.
     * @param r the robot to get the snapshot for
     * @return a snapshot of the positions of all robots in global space for a specific robot
     */
    public Vector[] getSnapshot(Robot r) {
        Vector[] positions = Arrays.stream(robots).map(robot -> robot.pos).toArray(Vector[]::new);
        // first check the visibility
        if (config.visibility != -1) {
            positions = Arrays.stream(positions).filter(p -> r.pos.dist(p) < config.visibility).toArray(Vector[]::new);
        }
        // then remove duplicates if there is no multiplicity detection
        if (!config.multiplicity) {
            positions = Arrays.stream(positions).distinct().toArray(Vector[]::new);
        }
        return positions;
    }

}
