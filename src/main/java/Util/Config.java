package Util;

/**
 * A class containing parameters for the {@link Simulator}
 */
public class Config {

    public Config(boolean multiplicity, double visibility, boolean interuptable) {
        this.multiplicity = multiplicity;
        this.visibility = visibility;
        this.interuptable = interuptable;
    }

    /**
     * Whether or not the robots can detect multiplicity.
     */
    public boolean multiplicity;
    /**
     * The range the robots can see other robots in. -1 for infinite visibility.
     */
    public double visibility;
    /**
     * Whether or not the robots will always reach their target position. If false, the scheduler will never stop the robots before they reach their target.
     */
    public boolean interuptable;

    /**
     * If two floats differ less than this value, they are considered to be equal.
     */
    public static final double EPSILON = 1e-14;
}
