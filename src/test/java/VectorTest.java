import Util.Vector;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.constraints.DoubleRange;

public class VectorTest {

    @Property
    boolean rotationReversibleTest(@ForAll @DoubleRange(min=-10E10, max=10E10) double angle,
                                   @ForAll @DoubleRange(min=-10E10, max=10E10) double startX,
                                   @ForAll @DoubleRange(min=-10E10, max=10E10) double startY) {
        Vector original = new Vector(startX, startY);
        return original.rotate(angle).rotate(-angle).equalsWithinEpsilon(original, 10E-5);
    }

    @Property
    boolean rotationAngleTest(@ForAll @DoubleRange(min=-10E10, max=10E10) double angle,
                              @ForAll @DoubleRange(min=-10E10, max=10E10) double startX,
                              @ForAll @DoubleRange(min=-10E10, max=10E10) double startY) {
        Vector original = new Vector(startX, startY);
        double r2 = original.rotate(angle).angle(original);

        return original.rotate(angle).equalsWithinEpsilon(original.rotate(-r2), 10E-5);
    }

}
