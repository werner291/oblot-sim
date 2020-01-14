package Simulator;

import Schedulers.EventType;

import static Schedulers.EventType.*;

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
                return State.SLEEPING;
            case START_MOVING:
                return  State.COMPUTING;
            case END_MOVING:
                return  State.MOVING;
        }
        throw new IllegalStateException("Shouldn't reach here, switch is supposed to be exhaustive.");
    }


}
