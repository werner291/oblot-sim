package Algorithms;

/**
 * An really simple algorithm that does not let the robots move at all. Can be used for testing
 */
public class AlgoStub extends Algorithm {

    @Override
    public Vector doAlgorithm(Vector[] snapshot) {
        return origin;
    }
}
