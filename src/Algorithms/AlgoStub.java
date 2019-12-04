package Algorithms;

import java.util.ArrayList;
import java.util.List;

/**
 * An really simple algorithm that does not let the robots move at all. Can be used for testing
 */
public class AlgoStub extends Algorithm {
    @Override
    public List<Vector> doAlgorithm(Vector[] snapshot, Vector pos) {
        List<Vector> targets = new ArrayList<>();
        targets.add(pos);
        return targets;
    }
}
