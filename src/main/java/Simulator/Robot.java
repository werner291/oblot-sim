package Simulator;

import Algorithms.Algorithm;
import PositionTransformations.PositionTransformation;
import RobotPaths.RobotPath;
import Schedulers.Event;
import Schedulers.EventType;
import Util.Vector;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.OptionalDouble;
import java.util.Scanner;
import java.util.function.Supplier;

/**
 * A class containing the state and metadata of one oblivious robot.
 *
 * Immutable: it is safe to store references to this.
 */
public class Robot {

    private int id;
    private Vector pos;
    private State state;
    private double inCurrentStateSince;
    private double speed;
    private PositionTransformation trans;
    private Algorithm algo;

    // Invariant: This is non-null if and only if state == MOVING
    // Maybe subclassing the state is better?
    private RobotPath path;

    /**
     * Creates a new robot
     * @param algo the algorithm the robot will be using
     * @param pos the starting position of the robot
     * @param trans the transformation the robot will use to convert to local coordinates
     * @param path
     * @param state
     * @param speed
     * @param inCurrentStateSince
     */
    public Robot(int id, Algorithm algo, Vector pos, PositionTransformation trans, RobotPath path, State state, double speed, double inCurrentStateSince) {
        this.id = id;
        this.algo = algo;
        this.pos = pos;
        this.trans = trans;
        this.path = path;
        this.state = state;
        this.speed = speed;
        this.inCurrentStateSince = inCurrentStateSince;
    }

    /**
     * Write an array of robot positions to the specified file.
     *
     * Algorithm and transformation are NOT saved, but these are not required for replay.
     *
     * @param file File to write to.
     * @param robots The robots to same to the file.
     * @throws FileNotFoundException If the file location cannot be found.
     */
    public static void toFile(File file, Robot[] robots) throws FileNotFoundException {
        try (PrintWriter w = new PrintWriter(new FileOutputStream(file))) {

            w.println(robots.length); // Print number of robots first.

            for (Robot robot : robots) {
                w.println(robot.getPos().x + ", " + robot.getPos().y); // Then x and y positions, 1 per line.
            }
        }
    }

    /**
     * Calculate where the robot wants to go
     * @param snapshot a snapshot of the positions of the robots at a certain timestamp in the global coordinate system
     * @return A list of positions the robot wants to go to.
     */
    public RobotPath calculate(Vector[] snapshot) {
        Vector[] localSnapshot = getTrans().globalToLocal(snapshot, getPos());
        RobotPath calculatedPath = getAlgo().doAlgorithm(localSnapshot);
        calculatedPath.convertFromLocalToGlobal(this.getTrans(), this.getPos());
        return calculatedPath;
    }

    @Override
    public String toString() {
        return "Robot: " + getId() + " state: "+ getState();
    }

    /**
     * A method to read in a robot configuration from a file.
     * The format should be as follows:<br>
     * n<br>
     * x, y<br>
     * x, y<br>
     * ...<br>
     * First the amount of robots n, then on separate lines, the starting positions of the robots.
     * @param algo the algorithm all robots should follow
     * @param t the transformation to global coordinate system the robots should use
     * @param file the file to read the starting configuration from
     * @return a list of robots that start on the positions specified in the file
     */
    public static Robot[] robotsFromFile(Algorithm algo, Supplier<PositionTransformation> t, File file) {
        try {
            Scanner s = new Scanner(file);
            int n = Integer.parseInt(s.nextLine());
            Robot[] robots = new Robot[n];
            int i = 0;
            while (s.hasNextLine()) {
                String nextLine = s.nextLine();
                String[] coordsString = nextLine.split(", ");
                double x = Double.parseDouble(coordsString[0]);
                double y = Double.parseDouble(coordsString[1]);
                Vector pos = new Vector(x, y);
                robots[i] = new Robot(i, algo, pos, t.get(), null, State.SLEEPING, 1.0, 0.0);
                i++;
            }
            return robots;
        } catch (FileNotFoundException e) {
            System.err.println(String.format("File not found: %s", file.getPath()));
            System.err.println("Does it exists and does it have read access?");
        } catch (NumberFormatException e) {
            System.err.println("The file does not have the correct format.");
        }
        return null;
    }

    public Algorithm getAlgorithm() {
        return getAlgo();
    }

    /**
     * The id of the robot
     */
    public int getId() {
        return id;
    }

    /**
     * The current position of the robot.
     */
    public Vector getPos() {
        return pos;
    }

    /**
     * The current state of this robot.
     */
    public State getState() {
        return state;
    }

    /**
     * The timestamp of the last change in state
     */
    public double getInCurrentStateSince() {
        return inCurrentStateSince;
    }

    /**
     * The speed of the robot.
     */
    public double getSpeed() {
        return speed;
    }

    /**
     * The transformation object to transform global to local coordinates.
     */
    public PositionTransformation getTrans() {
        return trans;
    }

    /**
     * The algorithm the robot will run.
     */
    public Algorithm getAlgo() {
        return algo;
    }

    public RobotPath getPath() {
        return path;
    }

    public Robot movedTo(Vector pos) {
        return new Robot(id, algo, pos, trans, path, state, speed, inCurrentStateSince);
    }

    public Vector positionAtTimeWithoutStateChange(double timestamp) {
        if (state == State.MOVING) {
            double movementEndTime = path.getEndTime(inCurrentStateSince, speed);

            return timestamp > movementEndTime ? path.getEnd() : path.interpolate(inCurrentStateSince, movementEndTime, timestamp);
        } else {
            return this.pos;
        }
    }

    public Robot extrapolatedToTime(double timestamp) {
        return new Robot(id, algo, positionAtTimeWithoutStateChange(timestamp), trans, path, state, speed, inCurrentStateSince);
    }

    /**
     * Timestamp after which this robot is sure to have stopped moving on its' own (barring other events).
     * If it is not moving, NONE will be returned.
     */
    public OptionalDouble willStopBefore() {
        return state == State.MOVING ? OptionalDouble.of(path.getEndTime(inCurrentStateSince, speed)) : OptionalDouble.empty();
    }
}
