package nl.tue.oblotsim.positiontransformations;

import nl.tue.oblotsim.Util.Vector;

/**
 * A transformation that scales and rotates the coordinate system.
 */
public class RotationTransformation extends PositionTransformation {
    /**
     * The rotation with which the local coordinate system is turned.
     */
    private double rotation;

    /**
     * The unit length of the local coordinate system
     */
    private double unitLength;

    /**
     * Whether or not the x axis is flipped
     */
    private boolean chirality;


    public RotationTransformation(double unitLength, double rotation, boolean chirality) {
        if (unitLength <= 0) {
            throw new IllegalArgumentException("unitLength should be strictly greater than 0");
        }
        if (rotation >= Math.PI || rotation < -Math.PI) {
            throw new IllegalArgumentException("rotation ("+rotation+") should be in the range [-pi, pi)");
        }
        this.unitLength = unitLength;
        this.rotation = rotation;
        this.chirality = chirality;
    }

    public RotationTransformation(double rotation) {
        this(1, rotation, false);
    }

    public RotationTransformation() {
        this(0);
    }

    @Override
    public Vector globalToLocal(Vector p, Vector origin) {
        Vector newP = p.sub(origin).mult(unitLength).rotate(rotation);
        if (chirality) {
            newP.x = -newP.x;
        }
        return newP;
    }

    @Override
    public Vector localToGlobal(Vector p, Vector origin) {
        Vector newP = new Vector(p);
        if (chirality) {
            newP.x = -newP.x;
        }
        return newP.rotate(-rotation).mult(1/unitLength).add(origin);
    }

    /**
     * Randomize this transformation
     * @param sameChirality whether or not the y axis should all be the same
     * @param sameUnitLength whether or not the unit length should be the same
     * @param sameRotation whether or not the rotation should be te same
     * @return the modified transformation
     */
    public PositionTransformation randomize(boolean sameChirality, boolean sameUnitLength, boolean sameRotation) {
        this.unitLength = sameUnitLength ? 1 : (random.nextDouble() + 0.5) * 2 - 0.5;
        this.chirality = sameChirality ? false : random.nextBoolean();
        this.rotation = sameRotation ? 0 : (random.nextDouble() * 2 * Math.PI) - Math.PI;
        return this;
    }

    public static PositionTransformation IDENTITY = new RotationTransformation(0.0);
}
