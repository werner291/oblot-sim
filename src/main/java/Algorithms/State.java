package Algorithms;

/**
 * The different states robots can be in.
 */
public enum State {
    CALCULATING,
    MOVING,
    SLEEPING;

    public static State next(State e) {
        return State.values()[(e.ordinal() + 1) % State.values().length];
    }
}
