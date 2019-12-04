package Algorithms;

import java.util.Arrays;

/**
 * An abstract class that can transform global coordinates to the local coordinate system of a {@link Robot}
 * Note that when implementing this class, p == globalToLocal(localToGlobal(p)) && p == localToGlobal(globalToLocal(p)),
 * i.e. they are each others inverses.
 */
public abstract class PositionTransformation {

    /**
     * Transforms a global coordinate to a local coordinate
     * @param p the coordinate to transform
     * @param origin the origin of the local coordinate system
     * @return the corresponding local coordinate
     */
    public abstract Vector globalToLocal(Vector p, Vector origin);

    /**
     * Transforms a local coordinate to a global coordinate
     * @param p the coordinate to transform
     * @param origin the origin of the local coordinate system
     * @return the corresponding global coordinate
     */
    public abstract Vector localToGlobal(Vector p, Vector origin);

    /**
     * Convert an array of coordinates from global to local
     * @param a the array to convert
     * @param origin the origin of the local coordinate system
     * @return the corresponding local coordinates in the same order
     */
    public Vector[] globalToLocal(Vector[] a, Vector origin) {
        return Arrays.stream(a).map(p -> globalToLocal(p, origin)).toArray(Vector[]::new);
    }

    /**
     * Convert an array of coordinates from local to global
     * @param a the array to convert
     * @param origin the origin of the local coordinate system
     * @return the corresponding global coordinates in the same order
     */
    public Vector[] localToGlobal(Vector[] a, Vector origin) {
        return Arrays.stream(a).map(p -> localToGlobal(p, origin)).toArray(Vector[]::new);
    }
}
