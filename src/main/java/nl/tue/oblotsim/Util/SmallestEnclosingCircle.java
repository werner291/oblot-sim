package nl.tue.oblotsim.Util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

/**
 * Find the smallest enclosing circle for a set of points in expected O(n) time using Welzl's algorithm
 */
public class SmallestEnclosingCircle {

    /**
     * The random object used for the randomized algorithm.
     */
    private static Random random = new Random();

    /**
     * Find the smallest enclosing circle of a set of points in expected O(n) time.
     * @param P the points to find the circle for
     * @return the smallest enclosing circle
     */
    public static Circle makeCircle(List<Vector> P) {
        //filter duplicates by changing to a set otherwise we do not always have enough when we find three points on the circle
        P = new ArrayList<>(new HashSet<>(P));
        return makeCircle(P, new ArrayList<>());
    }

    /**
     * Find the minimum enclosing circle for points P with points R on the boundary
     * @param P the points for which to find the SEC
     * @param R the points that we already know are on the boundary
     * @return the SEC of points P with points R on the boundary
     */
    private static Circle makeCircle(List<Vector> P, List<Vector> R) {
        if (P.size() == 0 || R.size() == 3) {
            return trivial(R);
        }
        if (R.size() > 3) {
            throw new IllegalArgumentException("Cannot guarantee a circle through more than 3 points");
        }
        int randomIndex = random.nextInt(P.size());
        Vector p = P.get(randomIndex);
        List<Vector> newP = new ArrayList<>(P);
        newP.remove(randomIndex);
        Circle D = makeCircle(newP, R);
        if (D != null && D.contains(p) && !D.on(p)) {
            return D;
        }
        List<Vector> newR = new ArrayList<>(R);
        newR.add(p);
        return makeCircle(newP, newR);
    }

    /**
     * Finding the SEC for the trivial cases of 0, 1, 2 or 3 points.
     * @param R the points to find the SEC for
     * @return the SEC of points R
     */
    private static Circle trivial(List<Vector> R) {
        switch (R.size()) {
            case 0:
                return null;
            case 1:
                return new Circle(R.get(0), 0);
            case 2:
                return makeCircle(R.get(0), R.get(1));
            case 3:
                return makeCircle(R.get(0), R.get(1), R.get(2));
            default:
                throw new IllegalArgumentException("Only a circle through 3 or less points is trivial");
        }
    }

    /**
     * Make the circle through 2 points
     * @param a first point
     * @param b second point
     * @return the circle through 2 points
     */
    private static Circle makeCircle(Vector a, Vector b) {
        Vector center = a.add(b).mult(1.0/2.0);
        return new Circle(center, a.dist(b) / 2.0);
    }

    /**
     * Find the circle through 3 points
     * @param a first point
     * @param b second point
     * @param c third point
     * @return the circle through 3 points
     */
    private static Circle makeCircle(Vector a, Vector b, Vector c) {
        double d = 2 * (a.x * (b.y - c.y) + b.x * (c.y - a.y) + c.x * (a.y - b.y));
        double x = ((a.x*a.x + a.y*a.y)*(b.y - c.y) + (b.x*b.x + b.y*b.y)*(c.y - a.y) + (c.x*c.x + c.y*c.y)*(a.y - b.y)) / d;
        double y = ((a.x*a.x + a.y*a.y)*(c.x - b.x) + (b.x*b.x + b.y*b.y)*(a.x - c.x) + (c.x*c.x + c.y*c.y)*(b.x - a.x)) / d;
        Vector CoC = new Vector(x, y);
        double r = CoC.dist(a);
        return new Circle(CoC, r);
    }
}
