package nl.tue.oblotsim.GUI;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.util.converter.IntegerStringConverter;

import java.util.function.Consumer;
import java.util.regex.Pattern;

/**
 * Constains a set of methods that are useful when working with JavaFX.
 */
public class ControllerUtil {
    /**
     * Bidirectionally bind a {@link SimpleIntegerProperty} to a {@link TextField} while checking for valid format.
     *
     * @param numberProp            The property to link.
     * @param storeToPreventGC      Important: store a reference to the object given as long as the bind must remain valid!
     * @param textField             The {@link TextField} to link it to.
     */
    public static void bindTextboxToNumber(SimpleIntegerProperty numberProp, Consumer<Object> storeToPreventGC, TextField textField) {
        TextFormatter<Integer> formatter = new TextFormatter<>(
                new IntegerStringConverter(),
                1,
                c -> Pattern.matches("\\d*", c.getText()) ? c : null );
        ObjectProperty<Integer> asObj = numberProp.asObject();
        storeToPreventGC.accept(asObj);
        formatter.valueProperty().bindBidirectional(asObj);
        textField.setTextFormatter(formatter);
    }
}
