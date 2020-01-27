package RobotPaths;

import PositionTransformations.PositionTransformation;
import Util.Config;
import Util.Vector;

public class LinearPath extends RobotPath {

    public LinearPath(Vector start, Vector end) {
        super(start, end);
    }

    public LinearPath(Vector end) {
        super(end);
    }

    @Override
    public Vector interpolate(double tStart, double tEnd, double t) {
        if (tStart == tEnd) {
            if (getStart().equals(getEnd())) {
                return getStart();
            } else {
                throw new IllegalArgumentException("Start and end are not the same, but the timestamps are");
            }
        }
        if (tStart >= tEnd) {
            throw new IllegalArgumentException("tStart should be strictly smaller than tEnd");
        }
        if (t <= tStart - Config.EPSILON) {
            return getStart();
        } else if (t >= tEnd + Config.EPSILON) {
            return getEnd();
        }
        if (tStart > t || t > tEnd) {
            throw new IllegalArgumentException("the timestamp to interpolate to should lie strictly in" +
                    "between the start and end timestamps");
        }
        double fractionToMove = (t - tStart) / (tEnd - tStart); // how much of that vector should be traversed
        Vector dir = getEnd().sub(getStart()); // the vector from start to end
        if (fractionToMove < 0 || fractionToMove > 1) {
            throw new IllegalArgumentException("The input timestamps are not correct");
        }
        Vector dirToMove = dir.mult(fractionToMove); // the fraction that should be traversed
        return getStart().add(dirToMove); // traverse from start, so add together
    }

    @Override
    public double getLength() {
        return getStart().dist(getEnd());
    }

    @Override
    public void convertFromLocalToGlobal(PositionTransformation trans, Vector origin) {
        start = trans.localToGlobal(getStart(), origin);
        end = trans.localToGlobal(getEnd(), origin);
    }
}
