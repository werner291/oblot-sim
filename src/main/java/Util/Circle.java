package Util;

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


    /**
     * Find out if a single point is contained in the circle.
     * @param p the point to check
     * @return true if the point lies inside or on the circle
     */
    public boolean contains(Vector p) {
        return c.dist(p) <= r * (1 + Config.EPSILON);
    }

    /**
     * Find out if a collection of points is contained in the circle
     * @param ps the points
     * @return true if all points lie inside or on the circle
     */
    public boolean contains(Collection<Vector> ps) {
        for (Vector p : ps) {
            if (!contains(p))
                return false;
        }
        return true;
    }

    /**
     * Find out if a point lies on the circle
     * @param p the point to check
     * @return true if the point lies on the circle
     */
    public boolean on(Vector p) {
        return p.dist(c) < r * (1 + Config.EPSILON) && p.dist(c) > r * (1 - Config.EPSILON);
    }

    /**
     * Get the point on the circle that is closest to r
     * @param p the robot to check for
     * @return the point on this circle that is closest to r
     */
    public Vector getPointOnCircle(Vector p) {
        if (on(p)) {
            return p;
        }
        Vector CtoR = p.sub(c);
        double len = CtoR.len();
        double factor = r / len;
        Vector CtoRCorrectLength = CtoR.mult(factor);
        return CtoRCorrectLength.add(c);
    }


    public String toString() {
        return String.format("Circle(x=%g, y=%g, r=%g)", c.x, c.y, r);
    }

}
