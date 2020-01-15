package RobotPaths;

import PositionTransformations.PositionTransformation;
import Util.Vector;

public abstract class RobotPath {

    public Vector start;
    public Vector end;

    public RobotPath(Vector start, Vector end) {
        this.start = start;
        this.end = end;
    }

    public RobotPath(Vector end) {
        this(new Vector(0, 0), end);
    }

    /**
     * Get the position of the robot along the path at a specific time when the robot started at tStart and ended at tEnd
     * @param tStart the time at which the robot started
     * @param tEnd the time at which the robot should arrive
     * @param t the time to get the position of the robot at
     * @return the position of the robot when it follows this path at time t
     */
    public abstract Vector interpolate(double tStart, double tEnd, double t);

    /**
     * Get the length of the path
     * @return the length of the path
     */
    public abstract double getLength();

    /**
     * This function converts the path from local robot coordinate space to global coordinate space.
     * Usually, this requires no more than just converting all anchor points of the path
     * through the PositionTransformation. For an example, see {@link LinearPath#convertFromLocalToGlobal}
     * @param trans the position transformation to use
     * @param origin the origin of the local coordinate system
     */
    public abstract void convertFromLocalToGlobal(PositionTransformation trans, Vector origin);
    
    /**
     * Get the time at which a robot would have completed this path when started
     * at a specific time and moving with a constant speed.
     * @param start the start time
     * @param speed the speed of the robot
     * @return the time at which the robot should be at the end of the path
     */
    public double getEndTime(double start, double speed) {
        if (speed < 0) {
            throw new IllegalArgumentException("Speed cannot be negative");
        }
        // calculate the distance to travel
        double dist = getLength();
        // calculate the time traveling
        double timeTraveling = dist / speed;
        // return the arrival time
        return start + timeTraveling;
    }
}
