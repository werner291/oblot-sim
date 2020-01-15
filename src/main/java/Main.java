import Algorithms.*;
import PositionTransformations.RotationTransformation;
import Schedulers.AsyncScheduler;
import Schedulers.FSyncScheduler;
import Schedulers.FileScheduler;
import Schedulers.ManualScheduler;
import Schedulers.Scheduler;
import Simulator.Simulator;
import Simulator.Robot;
import GUI.GUI;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * The public class that we will use to start our GUI. This is an example class of how the simulator may be used.
 * This does not contain any logic regarding the simulator whatsoever, but rather sets it up and runs it.
 */

public class Main{

    public static void main(String[] args) {
        // We're keeping this
        System.out.println("Most awesome simulator ever.");
        Robot[] robots = loadTestRobots();

//        Scheduler s = new FSyncScheduler(1, 1, 1, 1);
        Scheduler s = new FSyncScheduler();
        Util.Config c = new Util.Config(true, -1, true);
        Simulator simulator = new Simulator(c, robots, s);
        simulator.setScheduler(new ManualScheduler(simulator));

        Class[] algorithms = new Class[]{GatheringWithMultiplicity.class, GoToCoG.class, GoToRightMost.class, MoveAlongSEC.class};

        GUI.runGUI(args, simulator, algorithms);
    }

    /**
     * Load a set of robots with randomized position transformations.
     * @return An array of Robot instances that have a randomized positions, and using the MoveAlongSEC algorithm.
     */
    public static Robot[] loadTestRobots() {
        URL filePath = Robot.class.getClassLoader().getResource("testRobots2");
        if (filePath == null) {
            System.err.println("Cannot find resource testRobots2");
        }
        Robot[] robots = new Robot[0];
        try {
            robots = Robot.fromFile(new GatheringWithMultiplicity(), null, new File(filePath.toURI()));
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        for (Robot r : robots) {
            r.trans = new RotationTransformation().randomize(false, false, false);
        }
        return robots;
    }
}
