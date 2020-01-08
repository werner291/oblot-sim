package Algorithms;

import RobotPaths.CircularPath;
import RobotPaths.CombinedPath;
import RobotPaths.LinearPath;
import RobotPaths.RobotPath;
import Util.Circle;
import Util.SmallestEnclosingCircle;
import Util.Vector;

import java.util.ArrayList;
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
