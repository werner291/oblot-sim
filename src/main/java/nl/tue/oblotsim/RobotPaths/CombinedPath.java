package nl.tue.oblotsim.RobotPaths;

import nl.tue.oblotsim.positiontransformations.PositionTransformation;
import nl.tue.oblotsim.Util.Config;
import nl.tue.oblotsim.Util.Vector;

import java.util.Arrays;
import java.util.List;

public class CombinedPath extends RobotPath {

    private List<RobotPath> paths;

    /**
     * Creates a combined path of smaller paths
     * @param paths a list of paths. The start of every path should match the end of the previous path.
     */
    public CombinedPath(List<RobotPath> paths) {
        super(paths.get(0).getStart(), paths.get(paths.size() - 1).getEnd());
        for (int i = 0; i < paths.size() - 1; i++) {
            Vector end = paths.get(i).getEnd();
            Vector start = paths.get(i + 1).getStart();
            if (!end.equals(start)) {
                throw new IllegalArgumentException("The end of some path does not match the start of the next");
            }
        }
        this.paths = paths;
    }

    @Override
    public Vector interpolate(double tStart, double tEnd, double t) {
        if (tStart == tEnd) {
            if (getStart().equals(getEnd())) {
                return getStart();
            } else {
                throw new IllegalArgumentException("Start and end are not the same, but the timestamps are");
            }
        }
        if (tStart >= tEnd) {
            throw new IllegalArgumentException("tStart should be strictly smaller than tEnd");
        }
        if (t <= tStart - Config.EPSILON) {
            return getStart();
        } else if (t >= tEnd + Config.EPSILON) {
            return getEnd();
        }
        if (tStart > t || t > tEnd) {
            throw new IllegalArgumentException("the timestamp to interpolate to should lie strictly in" +
                    "between the start and end timestamps");
        }
        double fractionToMove = (t - tStart) / (tEnd - tStart); // how much of that vector should be traversed

        double totalLength = getLength();
        double[] individualLengths = paths.stream().mapToDouble(RobotPath::getLength).toArray();
        double[] normalizedLengths = Arrays.stream(individualLengths).map(l -> l / totalLength).toArray();

        RobotPath currentPath = null;
        double currentNormalizedPathLength = 0;
        double sum = 0;
        for (int i = 0; i < normalizedLengths.length + 1; i++) {
            if (fractionToMove < sum) {
                currentPath = paths.get(i - 1);
                sum -= normalizedLengths[i - 1];
                currentNormalizedPathLength = normalizedLengths[i - 1];
                break;
            } else {
                sum += normalizedLengths[i];
            }
        }
        if (currentPath == null) {
            throw new IllegalStateException("The found path is null. This can only happen if the input timestamps are wrong somehow.");
        }
        // now sum is the fraction of the path where this path starts
        double moveTime = tEnd - tStart;
        double startTime = tStart + sum * moveTime;
        double endTime = tStart + (currentNormalizedPathLength * moveTime);
        return currentPath.interpolate(startTime, endTime, t);
    }

    @Override
    public double getLength() {
        return paths.stream().mapToDouble(RobotPath::getLength).sum();
    }

    @Override
    public void convertFromLocalToGlobal(PositionTransformation trans, Vector origin) {
        this.start = trans.localToGlobal(this.getStart(), origin);
        this.end = trans.localToGlobal(this.getEnd(), origin);
        for (RobotPath p : paths) {
            p.convertFromLocalToGlobal(trans, origin);
        }
    }
}
