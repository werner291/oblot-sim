import PositionTransformations.PositionTransformation;
import PositionTransformations.RotationTransformation;
import Util.Vector;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.constraints.DoubleRange;
import org.junit.jupiter.api.Test;

public class PositionTransformationTest {

    @Property
    boolean testRotationReversible(@ForAll @DoubleRange(min=-Math.PI,max=Math.PI) double r,
                                   @ForAll @DoubleRange(min=-10E10,max=10E10) double oX,
                                   @ForAll @DoubleRange(min=-10E10,max=10E10) double oY,
                                   @ForAll @DoubleRange(min=-10E10,max=10E10) double pX,
                                   @ForAll @DoubleRange(min=-10E10,max=10E10) double pY) {
        Vector origin = new Vector(oX, oY);
        Vector p = new Vector(pX, pY);
        PositionTransformation posTrans = new RotationTransformation(r);

        return p.equals(posTrans.localToGlobal(posTrans.globalToLocal(p, origin), origin));
    }

    @Property
    boolean testRotationReversible2(@ForAll double r, @ForAll double oX, @ForAll double oY, @ForAll double pX, @ForAll double pY) {
        Vector origin = new Vector(oX, oY);
        Vector p = new Vector(pX, pY);
        PositionTransformation posTrans = new RotationTransformation(r);

        return p.equals(posTrans.globalToLocal(posTrans.localToGlobal(p, origin), origin));
    }

}
