package nl.tue.oblotsim.algorithms;

import nl.tue.oblotsim.RobotPaths.LinearPath;
import nl.tue.oblotsim.RobotPaths.RobotPath;
import nl.tue.oblotsim.Util.Circle;
import nl.tue.oblotsim.Util.Config;
import nl.tue.oblotsim.Util.SmallestEnclosingCircle;
import nl.tue.oblotsim.Util.Vector;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * A proof of concept algorithm that solves gathering with multiplicity detection
 * and infinite visibility for an even amount of robots > 2
 */
public class GatheringWithMultiplicity extends Algorithm {
    @Override
    public RobotPath doAlgorithm(Vector[] snapshot) {
        if (snapshot.length == 2) {
            throw new IllegalArgumentException("Gathering with multiplicity does not support 2 robots");
        }
        if (snapshot.length % 2 != 0) {
            throw new IllegalArgumentException("Gathering with multiplicity Does not (yet) support an uneven amount of robots. Please pick another algorithm.");
        }

        // make a frequency map of elements
        Map<Vector, Long> freq = Arrays.stream(snapshot).collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
        long maxFreq = freq.values().stream().max(Long::compareTo).get();
        if (maxFreq != 1) { // there is multiplicity
            Vector[] densePoints = freq.entrySet().stream().filter(e -> e.getValue() == maxFreq).map(Map.Entry::getKey).toArray(Vector[]::new);
            if (densePoints.length > 1) {
                throw new IllegalStateException("Multiple dense points with density: " + maxFreq);
            } else {
                return new LinearPath(densePoints[0]);
            }
        }

        Vector CoB = centerOfBiangularity(snapshot);
        if (CoB != null) { // if the configuration is biangular, go to the center of biangularity
            return new LinearPath(CoB);
        }
        // there is no dense point, so do the angle thingy.
        // first check if there is a robot at the CoC
        Circle SEC = SmallestEnclosingCircle.makeCircle(Arrays.asList(snapshot));
        Vector CoC = SEC.c; // the center of the smallest enclosing circle

        boolean robotAtCoC = Arrays.asList(snapshot).contains(CoC);
        // check if the SA is mixed
        // order the points in anticlockwise manner
        Vector[] orderedPoints = Arrays.stream(snapshot).sorted(Comparator.comparingDouble(p -> Vector.angle(snapshot[0], CoC, p))).toArray(Vector[]::new);
        // create the string of angles
        double[] stringOfAngles = new double[orderedPoints.length];
        for (int i = 0; i < orderedPoints.length - 1; i++) {
            stringOfAngles[i] = Vector.angle(orderedPoints[i], CoC, orderedPoints[i + 1]);
        }
        stringOfAngles[stringOfAngles.length-1] = Vector.angle(orderedPoints[stringOfAngles.length - 1], CoC, orderedPoints[0]);

        boolean SAmixed = Arrays.stream(stringOfAngles).filter(d -> Math.abs(d) < Config.EPSILON).count() > 0;

        // check the amount of robots inside the SEC (Number Inside)
        int NI = (int)Arrays.stream(snapshot).filter(p -> SEC.c.dist(p) < SEC.r - Config.EPSILON).count();

        // if this robot is not a leader robot, do nothing. This assumes only 1 leader. Other cases are not implemented.
        Vector leader = getLeaderRobot(stringOfAngles, orderedPoints, SEC);
        if (!leader.equals(Vector.ZERO)) return new LinearPath(Vector.ZERO);

        // the 4 cases
        if (!robotAtCoC) {
            if (!SAmixed) {
                if (NI > 1) {
                    return new LinearPath(SEC.getPointOnCircle(Vector.ZERO)); // we are always looking from the perspective of the current robot
                } else { // NI <= 1
                    return new LinearPath(SEC.c);
                }
            } else {
                // pick a random one to go to
                return new LinearPath(snapshot[0].equals(Vector.ZERO) ? snapshot[1] : snapshot[0]);
            }
        } else {
            if (!SAmixed) {
                return new LinearPath(snapshot[0].equals(Vector.ZERO) ? snapshot[1] : snapshot[0]);
            } else {
                return new LinearPath(SEC.c);
            }
        }
    }

    /**
     * Get the leader robot from the string of angles
     * @param SA the string of angles
     * @param orderedPoints the robots in the same order as in the string of angles.
     *                      Such that SA[0] is the angle between the points[0] and points[1]
     * @param SEC the smallest enclosing circle. A robot can only be a leader if it is not on the SEC
     * @return the leader robot
     */
    private Vector getLeaderRobot(double[] SA, Vector[] orderedPoints, Circle SEC) {
        if(Math.abs(Arrays.stream(SA).sum() - 2 * Math.PI) > Config.EPSILON) {
            throw new IllegalArgumentException("The sum of the string of angles is not a full circle");
        }
        LinkedList<Double> SAList = Arrays.stream(SA).boxed().collect(Collectors.toCollection(LinkedList::new));
        LinkedList<Vector> orderedPointsList = new LinkedList<>(Arrays.asList(orderedPoints));
        LinkedList<Double> currentSmallest = new LinkedList<>(SAList);
        ArrayList<Vector> currentSmallestOrderedPoints = new ArrayList<>(orderedPointsList);
        for (int i = 0; i < SA.length - 1; i++) {
            // shift both the SA and the orderedPoints
            SAList.addFirst(SAList.removeLast());
            orderedPointsList.addFirst(orderedPointsList.removeLast());

            // check normal order
            int comparison = compare(currentSmallest, SAList);
            if (comparison < 0) {
                currentSmallest = new LinkedList<>(SAList);
                currentSmallestOrderedPoints = new ArrayList<>(orderedPointsList);
            } else if (comparison == 0) {
                throw new UnsupportedOperationException("The case with multiple leaders has not yet been implemented");
            }
            // check reversed order
            List<Double> reversedSAList = new ArrayList<>(SAList);
            Collections.reverse(reversedSAList);
            LinkedList<Vector> reversedOrderedPoints = new LinkedList<>(orderedPointsList);
            Collections.reverse(reversedOrderedPoints);
            // shift the last vertex to the first position. We still want to count from the same vertex
            reversedOrderedPoints.addFirst(reversedOrderedPoints.removeLast());

            comparison = compare(currentSmallest, reversedSAList);
            if (comparison < 0) {
                currentSmallest = new LinkedList<>(reversedSAList);
                currentSmallestOrderedPoints = new ArrayList<>(reversedOrderedPoints);
            } else if (comparison == 0) {
                throw new UnsupportedOperationException("The case with multiple leaders has not yet been implemented");
            }
        }
        for (int i = 0; i < currentSmallest.size(); i++) {
            Vector pointToTest = currentSmallestOrderedPoints.get(i);
            if (!SEC.on(pointToTest)) {
                // we want to have this one as leader
                return pointToTest;
            }
        }
        // they are all on the SEC, so just return the first one
        return currentSmallestOrderedPoints.get(0);
    }

    /**
     * Lexicographically compare two lists.
     * @param a first list
     * @param b second list
     * @return 1 if a before b, 0 if a == b, -1 if b before a
     */
    private int compare(List<Double> a, List<Double> b) {
        Iterator<Double> aIterator = a.iterator();
        Iterator<Double> bIterator = b.iterator();
        while(aIterator.hasNext() && bIterator.hasNext()) {
            Double aNext = aIterator.next();
            Double bNext = bIterator.next();
            if (aNext < bNext) {
                return 1;
            }
            if (aNext > bNext) {
                return -1;
            }
        }
        if (!aIterator.hasNext() && !bIterator.hasNext()) { // same size
            return 0;
        } else if (!aIterator.hasNext()) { // b longer
            return 1;
        } else { // a longer
            return -1;
        }
    }

    /**
     * Calculate the center of biangularity if it exists.
     * Anderegg, L., Cieliebak, M., & Prencipe, G. (2003). The weber point can be found in linear time for points in biangular configuration.
     * @param points the points
     * @return the center of biangularity if it exists, null otherwise.
     */
    public Vector centerOfBiangularity(Vector[] points) {
        // Get leftmost point
        Vector x = Arrays.stream(points).min(Comparator.comparingDouble(p -> p.x)).get(); // always present
        Vector toCompareAgainstX = points[0] == x ? points[1] : points[0];
        double[] slopesFromLeftMost = Arrays.stream(points).filter(p -> p != x).mapToDouble(p -> Vector.angle(toCompareAgainstX, x, p)).toArray();
        int medianIndexFromLeftMost = median(slopesFromLeftMost);
        // no biangularity if there are multiple points with the same slope
        long amountSameSlope = Arrays.stream(slopesFromLeftMost).filter(s -> s == slopesFromLeftMost[medianIndexFromLeftMost]).count();
        if (amountSameSlope > 1) {
            return null;
        }
        Vector xMedian = Arrays.stream(points).filter(p -> p != x).toArray(Vector[]::new)[medianIndexFromLeftMost];

        double maxSlope = Arrays.stream(slopesFromLeftMost).max().getAsDouble();
        int maxSlopeIndex = getIndex(slopesFromLeftMost, maxSlope);
        Vector y = Arrays.stream(points).filter(p -> p != x).toArray(Vector[]::new)[maxSlopeIndex];
        Vector toCompareAgainstY = points[0] == y ? points[1] : points[0];
        double[] slopesFromMaxSlope = Arrays.stream(points).filter(p -> p != y).mapToDouble(p -> Vector.angle(toCompareAgainstY, y, p)).toArray();
        int medianIndexFromMaxSlope = median(slopesFromMaxSlope);
        // no biangularity if there are multiple points with the same slope
        amountSameSlope = Arrays.stream(slopesFromLeftMost).filter(s -> s == slopesFromMaxSlope[medianIndexFromMaxSlope]).count();
        if (amountSameSlope > 1) {
            return null;
        }
        Vector yMedian = Arrays.stream(points).filter(p -> p != y).toArray(Vector[]::new)[medianIndexFromMaxSlope];

        // if there is a center of biangularity, it is on the intersection of these two lines
        Vector CoB = intersection(x, xMedian, y, yMedian);
        if (CoB == null) {
            throw new IllegalStateException("The CoB is null after intersection. This is probably a bug.");
        }
        // if the CoB is coinciding with a point, return the point.
        if (Arrays.asList(points).contains(CoB)) {
            return CoB;
        }
        // the angles from x to CoB to the point, sorted
        double[] angles = Arrays.stream(points).mapToDouble(p -> Vector.angle(x, CoB, p)).sorted().toArray();
        // the differences between these angles should be the same two values alternating
        double firstAngle = Double.MIN_VALUE;
        double secondAngle = Double.MIN_VALUE;
        for (int i = 1; i < angles.length; i++) {
            if (i % 2 == 1) {
                if (firstAngle == Double.MIN_VALUE) { // undefined yet
                    firstAngle = angles[i] - angles[i-1];
                } else {
                    if (Math.abs(firstAngle - (angles[i] - angles[i-1])) > Config.EPSILON) { // not the same angles
                        return null;
                    }
                }
            } else {
                if (secondAngle == Double.MIN_VALUE) { // undefined yet
                    secondAngle = angles[i] - angles[i-1];
                } else {
                    if (Math.abs(secondAngle - (angles[i] - angles[i-1])) > Config.EPSILON) { // not the same angles
                        return null;
                    }
                }
            }
        }
        return CoB;
    }

    /**
     * An array of random doubles. Get the index of the median double. If there are more, return an arbitrary one
     * @param array the double array to look into. Should have an uneven length
     * @return the index of the median element
     */
    private int median(double[] array) {
        if (array.length % 2 != 1) {
            throw new IllegalArgumentException("Array not uneven length");
        }
        double median = Arrays.stream(array).sorted().skip(array.length/2).findFirst().getAsDouble();
        return getIndex(array, median);
    }

    /**
     * Find the index of an object in a double array. -1 if it does not exist
     * @param array the array to look into
     * @param item the item to look for
     * @return the index of the item in the array. -1 if it does not exist
     */
    private int getIndex(double[] array, double item) {
        for (int i = 0; i < array.length; i++) {
            if (array[i] == item) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Calculate the intersection between the lines through
     * @param a the first point of first line
     * @param b the second point of first line
     * @param c the first point of second line
     * @param d the second point of second line
     * @return the intersectino between the two lines, null if they are parallel.
     */
    private Vector intersection(Vector a, Vector b, Vector c, Vector d) {
        // Line AB represented as a1x + b1y = c1
        double a1 = b.y - a.y;
        double b1 = a.x - b.x;
        double c1 = a1*(a.x) + b1*(a.y);

        // Line CD represented as a2x + b2y = c2
        double a2 = d.y - c.y;
        double b2 = c.x - d.x;
        double c2 = a2*(c.x)+ b2*(c.y);

        double determinant = a1*b2 - a2*b1;

        if (determinant == 0)
        {
            // The lines are parallel. This is simplified
            // by returning a pair of FLT_MAX
            return null;
        }
        else
        {
            double x = (b2*c1 - b1*c2)/determinant;
            double y = (a1*c2 - a2*c1)/determinant;
            return new Vector(x, y);
        }
    }
}
