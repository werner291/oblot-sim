package nl.tue.oblotsim.Simulator;

import nl.tue.oblotsim.Schedulers.EventType;

/**
 * The different states robots can be in.
 */
public enum State {
    COMPUTING,
    MOVING,
    SLEEPING;

    public static State next(State e) {
        return State.values()[(e.ordinal() + 1) % State.values().length];
    }

    public static State  resultingFromEventType(EventType et) {
        switch (et) {
            case START_COMPUTE:
                return State.COMPUTING;
            case START_MOVING:
                return  State.MOVING;
            case END_MOVING:
                return  State.SLEEPING;
        }
        throw new IllegalStateException("Shouldn't reach here, switch is supposed to be exhaustive.");
    }


}
