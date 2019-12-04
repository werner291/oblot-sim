package Algorithms;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.Scanner;

/**
 * A class containing one oblivious robot
 */
public class Robot {

    /**
     * The algorithm the robot will run.
     */
    Algorithm algo;
    /**
     * The current position of the robot.
     */
    Vector pos;
    /**
     * The transformation object to transform global to local coordinates.
     */
    PositionTransformation trans;

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
    }

    /**
     * Calculate where the robot wants to go
     * @param snapshot a snapshot of the positions of the robots at a certain timestamp
     * @return A list of positions the robot wants to go to.
     */
    public List<Vector> calculate(Vector[] snapshot) {
        return algo.doAlgorithm(trans.globalToLocal(snapshot, pos), pos);
    }

    /**
     * A method to read in a robot configuration from a file.
     * The format should be as follows:<br>
     * n<br>
     * x, y<br>
     * x, y<br>
     * ...<br>
     * First the amount of robots n, then on separate lines, the starting positions of the robots.
     * @param filePath the path of the file to read the starting configuration from
     * @param algo the algorithm all robots should follow
     * @param t the transformation to global coordinate system the robots should use
     * @return a list of robots that start on the positions specified in the file
     */
    public static Robot[] fromFile(String filePath, Algorithm algo, PositionTransformation t) {
        try {
            Scanner s = new Scanner(new File(filePath));
            int n = Integer.parseInt(s.nextLine());
            Robot[] robots = new Robot[n];
            int i = 0;
            while (s.hasNextLine()) {
                String nextLine = s.nextLine();
                String[] coordsString = nextLine.split(", ");
                int x = Integer.parseInt(coordsString[0]);
                int y = Integer.parseInt(coordsString[1]);
                Vector pos = new Vector(x, y);
                robots[i] = new Robot(algo, pos, t);
                i++;
            }
            return robots;
        } catch (FileNotFoundException e) {
            System.err.println(String.format("File not found: %s", filePath));
            System.err.println("Does it exists and does it have read access?");
        } catch (NumberFormatException e) {
            System.err.println("The file does not have the correct format.");
        }
        return null;
    }
}
