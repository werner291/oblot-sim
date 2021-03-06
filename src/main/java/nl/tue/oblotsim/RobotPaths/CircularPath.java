package nl.tue.oblotsim.RobotPaths;

import nl.tue.oblotsim.positiontransformations.PositionTransformation;
import nl.tue.oblotsim.Util.Config;
import nl.tue.oblotsim.Util.Vector;

public class CircularPath extends RobotPath {

    public Vector center;
    /**
     * The positive angle from start to end in the direction defined by the variable clockwise.
     */
    public double angle;
    /**
     * Whether the rotation is clockwise or anticlockwise
     */
    public boolean clockwise;

    public CircularPath(Vector start, Vector center, Vector end, boolean clockwise) {
        super(start, end);
        this.center = center;
        this.clockwise = clockwise;
        if (Math.abs(end.dist(center) - start.dist(center)) > Config.EPSILON) {
            throw new IllegalArgumentException("The distance between start en center is not equal to the distance between end and center");
        }
        this.angle = calculateAngle(start, center, end);
    }

    private double calculateAngle(Vector start, Vector center, Vector end) {
        double angle = Vector.angle(start, center, end);
        // when the angle measured is the anticlockwise angle, but we want clockwise, turn it around
        if (clockwise && angle < 0) {
            angle = 2*Math.PI - angle;
        }
        // when the angle measured is the clockwise angle, but we want to anticlockwise angle, turn it around
        if (!clockwise && angle > 0) {
            angle = 2*Math.PI - angle;
        }
        while (angle <= -Math.PI) {
            angle += 2 * Math.PI;
        }
        while (angle > Math.PI) {
            angle -= 2 * Math.PI;
        }
        return angle;
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
        double fractionToMove = (t - tStart) / (tEnd - tStart); // The fraction of the total path that should be traversed
        double angleToMove = angle * fractionToMove;
        if (!clockwise) {
            angleToMove = -angleToMove;
        }
        return getStart().rotate(angleToMove, center);
    }

    @Override
    public double getLength() {
        double totalLength = 2 * Math.PI * getEnd().dist(center);
        double fraction = angle / (2 * Math.PI);
        return totalLength * fraction;
    }

    @Override
    public void convertFromLocalToGlobal(PositionTransformation trans, Vector origin) {
        Vector newStart = trans.localToGlobal(getStart(), origin);
        Vector newEnd = trans.localToGlobal(getEnd(), origin);
        Vector newCenter = trans.localToGlobal(center, origin);
        double newAngle = calculateAngle(newStart, newCenter, newEnd);
        // the new angle and the old angle should be the same (+- 2PI)
        if (angle < 0 && newAngle > 0) {
            if (Math.abs(angle + 2 * Math.PI - newAngle) > Config.EPSILON) {
                throw new IllegalStateException("Conversion has gone wrong. Angle before and after are different");
            }
        } else if (angle > 0 && newAngle < 0) {
            if (Math.abs(angle - 2 * Math.PI - newAngle) > Config.EPSILON) {
                throw new IllegalStateException("Conversion has gone wrong. Angle before and after are different");
            }
        } else if (Math.abs(angle - calculateAngle(newStart, newCenter, newEnd)) > Config.EPSILON) {
                throw new IllegalStateException("Conversion has gone wrong. Angle before and after are different");
        }
        start = newStart;
        end = newEnd;
        center = newCenter;
        }
        // the angle should still be the same after conversion
}
