package nl.tue.oblotsim.algorithms;

import nl.tue.oblotsim.RobotPaths.LinearPath;
import nl.tue.oblotsim.RobotPaths.RobotPath;
import nl.tue.oblotsim.Util.Vector;

/**
 * An algorithm that computes the center of gravity of all visible robots and goes there.
 */
public class GoToCoG extends Algorithm {
    @Override
    public RobotPath doAlgorithm(Vector[] snapshot) {
        Vector total = new Vector(0, 0);
        for (Vector v : snapshot) {
            total = total.add(v);
        }
        return new LinearPath(new Vector(total.x / snapshot.length, total.y / snapshot.length));
    }
}
