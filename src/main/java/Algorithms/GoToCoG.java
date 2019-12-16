package Algorithms;

import Util.Vector;

/**
 * An algorithm that computes the center of gravity of all visible robots and goes there.
 */
public class GoToCoG extends Algorithm {
    @Override
    public Vector doAlgorithm(Vector[] snapshot) {
        Vector total = new Vector(0, 0);
        for (Vector v : snapshot) {
            total = total.add(v);
        }
        return new Vector(total.x / snapshot.length, total.y / snapshot.length);
    }
}
