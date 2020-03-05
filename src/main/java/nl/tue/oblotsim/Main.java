package nl.tue.oblotsim;

import nl.tue.oblotsim.algorithms.*;
import nl.tue.oblotsim.positiontransformations.RotationTransformation;
import nl.tue.oblotsim.Schedulers.FSyncScheduler;
import nl.tue.oblotsim.Schedulers.Scheduler;
import nl.tue.oblotsim.Simulator.Simulation;
import nl.tue.oblotsim.Simulator.Robot;
import nl.tue.oblotsim.gui.GUI;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;

/**
 * The public class that we will use to start our nl.tue.oblotsim.GUI. This is an example class of how the simulator may be used.
 * This does not contain any logic regarding the simulator whatsoever, but rather sets it up and runs it.
 */

public class Main{

    public static void main(String[] args) {
        // We're keeping this
        System.out.println("Most awesome simulator ever.");
        List<Robot> robots = loadTestRobots();

//        Scheduler s = new FSyncScheduler(1, 1, 1, 1);
        Scheduler s = new FSyncScheduler();
        nl.tue.oblotsim.Util.Config c = new nl.tue.oblotsim.Util.Config(true, -1, true);
        Simulation simulation = new Simulation(c, robots, s);

        Class[] algorithms = new Class[]{GatheringWithMultiplicity.class, GoToCoG.class, GoToRightMost.class, MoveAlongSEC.class};

        GUI.runGUI(simulation, algorithms);
    }

    /**
     * Load a set of robots with randomized position transformations.
     * @return An array of Robot instances that have a randomized positions, and using the MoveAlongSEC algorithm.
     */
    public static List<Robot> loadTestRobots() {
        URL filePath = Robot.class.getClassLoader().getResource("testRobots2");
        if (filePath == null) {
            System.err.println("Cannot find resource testRobots2");
        }
        final RotationTransformation basisTransform = new RotationTransformation();
        List<Robot> robots;
        try {
            robots = Robot.robotsFromFile(
                    new MoveAlongSEC(),
                    () -> basisTransform.randomize(false, false, false),
                    new File(filePath.toURI()));
        } catch (URISyntaxException e) {
            e.printStackTrace();
            throw new IllegalStateException("Should never happen!", e);
        }
        return robots;
    }
}
