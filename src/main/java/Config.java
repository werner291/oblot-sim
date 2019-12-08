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
    boolean multiplicity;
    /**
     * The range the robots can see other robots in. -1 for infinite visibility.
     */
    double visibility;
}
