package nl.tue.oblotsim.algorithms;

import nl.tue.oblotsim.RobotPaths.CircularPath;
import nl.tue.oblotsim.RobotPaths.CombinedPath;
import nl.tue.oblotsim.RobotPaths.LinearPath;
import nl.tue.oblotsim.RobotPaths.RobotPath;
import nl.tue.oblotsim.Util.Circle;
import nl.tue.oblotsim.Util.SmallestEnclosingCircle;
import nl.tue.oblotsim.Util.Vector;

import java.util.Arrays;
import java.util.List;

public class MoveAlongSEC extends Algorithm {
    @Override
    public RobotPath doAlgorithm(Vector[] snapshot) {
        Circle SEC = SmallestEnclosingCircle.makeCircle(Arrays.asList(snapshot));
        Vector onCircle = SEC.getPointOnCircle(origin);
        Vector toCenter = SEC.c.sub(onCircle);
        RobotPath circlePath =  new CircularPath(onCircle, SEC.c, onCircle.add(toCenter).add(toCenter), false);
        RobotPath linePath = new LinearPath(onCircle);
        List<RobotPath> paths = Arrays.asList(linePath, circlePath);
        return new CombinedPath(paths);
    }
}
