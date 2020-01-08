package RobotPaths;

import PositionTransformations.PositionTransformation;
import Util.Vector;

import java.util.Arrays;
import java.util.List;

public class CombinedPath extends RobotPath {

    private List<RobotPath> paths;

    /**
     * Creates a combined path of smaller paths
     * @param paths a list of paths. The start of every path should match the end of the previous path.
     */
    public CombinedPath(List<RobotPath> paths) {
        super(paths.get(0).start, paths.get(paths.size() - 1).end);
        for (int i = 0; i < paths.size() - 1; i++) {
            Vector end = paths.get(i).end;
            Vector start = paths.get(i + 1).start;
            if (!end.equals(start)) {
                throw new IllegalArgumentException("The end of some path does not match the start of the next");
            }
        }
    }

    @Override
    public Vector interpolate(double tStart, double tEnd, double t) {
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
        double fractionToMove = (t - tStart) / (tEnd - tStart); // how much of that vector should be traversed

        double totalLength = getLength();
        double[] individualLengths = paths.stream().mapToDouble(RobotPath::getLength).toArray();
        double[] normalizedLengths = Arrays.stream(individualLengths).map(l -> l / totalLength).toArray();

        RobotPath currentPath = null;
        double currentNormalizedPathLength = 0;
        double sum = 0;
        for (int i = 0; i < normalizedLengths.length; i++) {
            if (fractionToMove < sum) {
                currentPath = paths.get(i);
                sum -= normalizedLengths[i - 1];
                currentNormalizedPathLength = normalizedLengths[i];
            } else {
                sum += normalizedLengths[i];
            }
        }
        // now sum is the fraction of the path where this path starts
        fractionToMove = fractionToMove - sum;
        double moveTime = tEnd - tStart;
        double startTime = tStart + (fractionToMove * moveTime);
        double endTime = tStart + (currentNormalizedPathLength * moveTime);
        return currentPath.interpolate(startTime, endTime, t);
    }

    @Override
    public double getLength() {
        return paths.stream().mapToDouble(RobotPath::getLength).sum();
    }

    @Override
    public void convertFromLocalToGlobal(PositionTransformation trans, Vector origin) {
        for (RobotPath p : paths) {
            p.convertFromLocalToGlobal(trans, origin);
        }
    }
}
