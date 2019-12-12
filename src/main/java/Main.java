import Algorithms.BasicPositionTransformation;
import Algorithms.GoToCoG;
import Algorithms.Robot;
import Schedulers.FileScheduler;
import Schedulers.Scheduler;

/**
 * The public class that we will use to start our GUI. This is an example class of how the simulator may be used.
 * This does not contain any logic regarding the simulator whatsoever, but rather sets it up and runs it.
 */

public class Main{

    public static void main(String[] args) {
        // We're keeping this
        System.out.println("Most awesome simulator ever.");
        Robot[] r = Robot.fromFile("testRobots", new GoToCoG(), new BasicPositionTransformation());
        Scheduler s = new FileScheduler("testSchedule", r);
        Config c = new Config(false, -1);
        Simulator simulator = new Simulator(c, r, s);
        while (true) {
            simulator.simulateTillNextEvent();
        }
//        new GUI().startGUI(args);
    }
}
