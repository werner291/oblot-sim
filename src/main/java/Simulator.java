import Algorithms.Robot;
import Algorithms.Vector;
import Schedulers.Scheduler;
import Schedulers.SyncScheduler;

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


    /**
     * The {@link Scheduler} that regulates which robots activate when.
     */
    private Scheduler scheduler;

    public Simulator(Config c, Robot[] robots, Scheduler scheduler) {
        this.config = c;
        this.robots = robots;
        this.scheduler = scheduler;
    }

    /**
     * Simulate the robots until timestamp t
     * @param t the timestamp to simulate to
     */
    public void simulateTillTimestamp(double t) {

    }

    /**
     * Simulate the robots until the next event that will be requested from the scheduler.
     * It could be the case that there are no events anymore. In this case, the simulator will simulate until all robots
     * are not moving anymore.
     */
    public void simulateTillNextEvent() {

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

    /**
     * Run the simulation from the start until either the last event occurs, or the maximum time is reached.
     *
     * // TODO: Add some kind of callback such that the simulation can be observed?
     *
     * @param maxTime The time at which to stop the simulation. This may be Double.POSITIVE_INFINITY to let it run.
     */
    public void run(double maxTime) {
        // TODO Implement.
    }
}
