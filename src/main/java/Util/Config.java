package Util;

/**
 * A class containing parameters for the {@link Simulator}
 */
public class Config {

    public Config(boolean multiplicity, double visibility) {
        this.multiplicity = multiplicity;
        this.visibility = visibility;
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
     * If two floats differ less than this value, they are considered to be equal.
     */
    public static final double EPSILON = 1e-14;
}
