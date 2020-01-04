import PositionTransformations.PositionTransformation;
import PositionTransformations.RotationTransformation;
import Util.Vector;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.constraints.DoubleRange;
import org.junit.jupiter.api.Test;

public class PositionTransformationTest {

    @Property
    boolean testRotationReversible(@ForAll @DoubleRange(min=-Math.PI,max=Math.PI-10E-10) double r,
                                   @ForAll @DoubleRange(min=-10E10,max=10E10) double oX,
                                   @ForAll @DoubleRange(min=-10E10,max=10E10) double oY,
                                   @ForAll @DoubleRange(min=-10E10,max=10E10) double pX,
                                   @ForAll @DoubleRange(min=-10E10,max=10E10) double pY,
                                   @ForAll @DoubleRange(min=10E-10,max=10.0) double unitLength,
                                   @ForAll boolean chirality) {

        Vector origin = new Vector(oX, oY);
        Vector p = new Vector(pX, pY);
        PositionTransformation posTrans = new RotationTransformation(unitLength, r, chirality);

        return p.equalsWithinEpsilon(posTrans.localToGlobal(posTrans.globalToLocal(p, origin), origin), 10E-5)
                && p.equalsWithinEpsilon(posTrans.globalToLocal(posTrans.localToGlobal(p, origin), origin), 10E-5);
    }

}
