package Util;

import Util.Vector;

import java.util.Collection;

/**
 * A circle, to use in the smallest enclosing circle algorithm.
 */
public class Circle {

    public final Vector c;   // Center
    public final double r;  // Radius


    public Circle(Vector c, double r) {
        this.c = c;
        this.r = r;
    }


    public boolean contains(Vector p) {
        return c.dist(p) <= r * (1 + Config.EPSILON);
    }


    public boolean contains(Collection<Vector> ps) {
        for (Vector p : ps) {
            if (!contains(p))
                return false;
        }
        return true;
    }

    public boolean on(Vector p) {
        return p.dist(c) < r * (1 + Config.EPSILON) && p.dist(c) > r * (1 - Config.EPSILON);
    }


    public String toString() {
        return String.format("Circle(x=%g, y=%g, r=%g)", c.x, c.y, r);
    }

}
