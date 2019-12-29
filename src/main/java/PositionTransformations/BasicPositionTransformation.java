package PositionTransformations;

import Util.Vector;

/**
 * A basic transformation from global to local coordinates and back. It just transforms the coordinates without changing
 * the axis.
 */
public class BasicPositionTransformation extends PositionTransformation {
    @Override
    public Vector globalToLocal(Vector p, Vector origin) {
        return p.sub(origin);
    }

    @Override
    public Vector localToGlobal(Vector p, Vector origin) {
        return p.add(origin);
    }
}
