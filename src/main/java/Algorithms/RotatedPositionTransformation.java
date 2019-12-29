package Algorithms;

import Util.Vector;

/**
 * A transformation that translates all positions to be relative to the robot,
 * and which applies a rotation to the points afterwards to potentially give
 * different robots differently-oriented frames of reference.
 */
public class RotatedPositionTransformation extends PositionTransformation {
    private double r;

    /**
     * Construct a rotation transformation.
     *
     * @param r The angle to rotate the world by in radians.
     */
    public RotatedPositionTransformation(double r) {
        this.r = r;
    }

    @Override
    public Vector globalToLocal(Vector p, Vector origin) {
        return p.sub(origin).rotate(r);
    }

    @Override
    public Vector localToGlobal(Vector p, Vector origin) {
        return p.rotate(-r).add(origin);
    }
}
