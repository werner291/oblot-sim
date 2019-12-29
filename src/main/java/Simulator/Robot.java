package Simulator;

import Algorithms.Algorithm;
import PositionTransformations.PositionTransformation;
import Util.Vector;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Scanner;

/**
 * A class containing one oblivious robot
 */
public class Robot {

    /**
     * The current position of the robot.
     */
    public Vector pos;
    /**
     * The current state of this robot.
     */
    public State state;
    /**
     * The speed of the robot.
     */
    public double speed;

    /**
     * The transformation object to transform global to local coordinates.
     */
    private PositionTransformation trans;
    /**
     * The algorithm the robot will run.
     */
    private Algorithm algo;


    /**
     * Creates a new robot
     * @param algo the algorithm the robot will be using
     * @param startPos the starting position of the robot
     * @param trans the transformation the robot will use to convert to local coordinates
     */
    public Robot(Algorithm algo, Vector startPos, PositionTransformation trans) {
        this.algo = algo;
        this.pos = startPos;
        this.trans = trans;
        this.state = State.SLEEPING;
        this.speed = 1.0;
    }

    /**
     * Calculate where the robot wants to go
     * @param snapshot a snapshot of the positions of the robots at a certain timestamp in the global coordinate system
     * @return A list of positions the robot wants to go to.
     */
    public Vector calculate(Vector[] snapshot) {
        Vector[] localSnapshot = trans.globalToLocal(snapshot, pos);
        Vector calculatedPosition = algo.doAlgorithm(localSnapshot);
        return trans.localToGlobal(calculatedPosition, pos);
    }

    @Override
    public String toString() {
        return "Robot: " + pos.toString() + " state: "+ state;
    }

    /**
     * A method to read in a robot configuration from a file.
     * The format should be as follows:<br>
     * n<br>
     * x, y<br>
     * x, y<br>
     * ...<br>
     * First the amount of robots n, then on separate lines, the starting positions of the robots.
     * @param fileName the path of the file to read the starting configuration from
     * @param algo the algorithm all robots should follow
     * @param t the transformation to global coordinate system the robots should use
     * @return a list of robots that start on the positions specified in the file
     */
    public static Robot[] fromFile(String fileName, Algorithm algo, PositionTransformation t) {
        try {
            URL filePath = Robot.class.getClassLoader().getResource(fileName);
            if (filePath == null) {
                throw new FileNotFoundException();
            }
            Scanner s = new Scanner(new File(filePath.toURI()));
            int n = Integer.parseInt(s.nextLine());
            Robot[] robots = new Robot[n];
            int i = 0;
            while (s.hasNextLine()) {
                String nextLine = s.nextLine();
                String[] coordsString = nextLine.split(", ");
                double x = Double.parseDouble(coordsString[0]);
                double y = Double.parseDouble(coordsString[1]);
                Vector pos = new Vector(x, y);
                robots[i] = new Robot(algo, pos, t);
                i++;
            }
            return robots;
        } catch (FileNotFoundException | URISyntaxException e) {
            System.err.println(String.format("File not found: %s", fileName));
            System.err.println("Does it exists and does it have read access?");
        } catch (NumberFormatException e) {
            System.err.println("The file does not have the correct format.");
        }
        return null;
    }

    public Robot copy() {
        return new Robot(this.algo, this.pos, this.trans);
    }

    public void setAlgorithm(Algorithm a) {
        this.algo = a;
    }

    public void setTransformation(PositionTransformation pt) {
        this.trans = pt;
    }
}
