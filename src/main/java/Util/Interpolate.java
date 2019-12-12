package Util;

public final class Interpolate {

    // prevent instantiating
    private Interpolate() {}

    /**
     * Interpolate a position. Given a starting position and time, and an end position and time, find the position at
     * some timestamp in between the start and end timestamps when the object is
     * moving with constant speed from start to end
     *
     * @param start the position to start at
     * @param tStart the timestamp to start at
     * @param end the position to end at
     * @param tEnd the timestamp to end at
     * @param t the timestamp to get the position of
     * @return the position of the object at time t
     */
    public static Vector linearInterpolate(Vector start, double tStart, Vector end, double tEnd, double t) {
        if (tStart == tEnd) {
            if (start.equals(end)) {
                return start;
            } else {
                throw new IllegalArgumentException("Start and end are not the same, but the timestamps are");
            }
        }
        if (tStart >= tEnd) {
            throw new IllegalArgumentException("tStart should be strictly smaller than tEnd");
        }
        if (tStart > t || t > tEnd) {
            throw new IllegalArgumentException("the timestamp to interpolate to should lie strictly in" +
                    "between the start and end timestamps");
        }
        Vector dir = end.sub(start); // the vector from start to end
        double fractionToMove = (t - tStart) / (tEnd - tStart); // how much of that vector should be traversed
        if (fractionToMove < 0 || fractionToMove > 1) {
            throw new IllegalArgumentException("The input timestamps are not correct");
        }
        Vector dirToMove = dir.mult(fractionToMove); // the fraction that should be traversed
        return start.add(dirToMove); // traverse from start, so add together
    }

    /**
     * Find when an object moving at a constant speed will reach a certain position.
     * Assuming a linear trajectory and constant speed.
     * @param start the start position
     * @param tStart the timestamp at which to start moving
     * @param end the goal position to reach
     * @param speed the constant units per timestamp that the object is moving at
     * @return the timestamp at which the object will reach the goal position
     */
    public static double getEndTime(Vector start, double tStart, Vector end, double speed) {
        if (speed < 0) {
            throw new IllegalArgumentException("Speed cannot be negative");
        }
        // calculate the distance to travel
        double dist = end.dist(start);
        // calculate the time traveling
        double timeTraveling = dist / speed;
        // return the arrival time
        return tStart + timeTraveling;
    }
}
