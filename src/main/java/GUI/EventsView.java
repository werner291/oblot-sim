package GUI;

import Schedulers.CalculatedEvent;
import Schedulers.Event;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;

import java.util.function.DoubleConsumer;

/**
 * A ScrollPane that displays a list of events that happened during the simulation.
 */
public class EventsView extends ScrollPane {

    /**
     * Property that holds the time picking listener.
     * It will be called with a timestamp whenever the user picks an event.
     */
    public DoubleConsumer timePickedCB = v -> {/* Do nothing */};
    /**
     * List of {@link CalculatedEvent} to display. These are flattened out automatically.
     */
    public final ListProperty<CalculatedEvent> events = new SimpleListProperty<>(FXCollections.observableArrayList());

    public VBox list;

    /**
     * Function that should be called to rebuild the eventlist from scratch. Necessary when the past changes.
     */
    private void rebuildList() {
        list = new VBox();
        for (CalculatedEvent cevt : events.get()) {
            for (Event evt : cevt.getEvents()) {
                list.getChildren().add(createEventButton(evt.getTargetId() + 1, evt.getType().toString(), evt.getT()));
            }
        }
    }

    public EventsView() {

        // set the values for the events scrollpane
        if (list == null) {
            list = new VBox();
            list.setSpacing(1);
            list.setPadding(new Insets(1));
        }
        // Each time that the list changes, regenerate the list of events.
        events.addListener((ListChangeListener<CalculatedEvent>) c -> {

            while (c.next()) {
                if (c.wasAdded()) {
                    for (CalculatedEvent cevt : c.getAddedSubList()) {
                        for (Event evt : cevt.getEvents()) {
                            list.getChildren().add(createEventButton(evt.getTargetId() + 1, evt.getType().toString(), evt.getT()));
                        }
                    }
                }
                if (c.wasRemoved()) {
                    rebuildList();
                }
            }

            setContent(list);
        });
    }

    /**
     * Create a button for in the event scrollpane
     * @param eventName Name of the event, probably the type of event
     * @param timeStamp Timestamp of when the event took place
     * @return The object that can be clicked by the user to return to a certain timestamp/event
     */
    private Button createEventButton(int robotnr, String eventName, double timeStamp) {
        EventButton eventButton = new EventButton( "Robot: " + robotnr + " | " + eventName + " | @: " + timeStamp, timeStamp);
        eventButton.fontProperty().set(Font.font(12.0));
        eventButton.prefWidthProperty().bind(widthProperty());
        eventButton.setOnAction(actionEvent -> timePickedCB.accept(timeStamp));
        return eventButton;
    }
}
