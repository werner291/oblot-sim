import Algorithms.*;
import PositionTransformations.RotationTransformation;
import Schedulers.FileScheduler;
import Schedulers.Scheduler;
import Simulator.Simulator;
import Simulator.Robot;
import GUI.GUI;

/**
 * The public class that we will use to start our GUI. This is an example class of how the simulator may be used.
 * This does not contain any logic regarding the simulator whatsoever, but rather sets it up and runs it.
 */

public class Main{

    public static void main(String[] args) {
        // We're keeping this
        System.out.println("Most awesome simulator ever.");
        Robot[] robots = Robot.fromFile("testRobots2", new GatheringWithMultiplicity(), null);
        for (Robot r : robots) {
            r.trans = new RotationTransformation().randomize(false, false, false);
        }
        Scheduler s = new FileScheduler("testSchedule", robots);
        Util.Config c = new Util.Config(true, -1);
        Simulator simulator = new Simulator(c, robots, s);

        Class[] algorithms = new Class[]{GatheringWithMultiplicity.class, GoToCoG.class};

        GUI.runGUI(args, simulator, algorithms);
    }
}
