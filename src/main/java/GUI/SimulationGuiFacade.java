package GUI;

import GUI.FxFXMLController;
import Schedulers.CalculatedEvent;
import Simulator.Robot;
import Simulator.Simulation;
import javafx.animation.AnimationTimer;
import javafx.beans.property.*;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.List;

/**
 * A class that bridges the property-based world of JavaFX and the functionally-immutuable Simulation object.
 */
public class SimulationGuiFacade {

    final private Simulation simulation;

    final private ReadOnlyListWrapper<Robot> robots;
    final private ReadOnlyListWrapper<CalculatedEvent> events;
    final private DoubleProperty currentTime = new SimpleDoubleProperty(0.0);
    final private ReadOnlyDoubleWrapper bound = new ReadOnlyDoubleWrapper(0.0);

    public SimulationGuiFacade(Simulation simulation) {
        this.simulation = simulation;
        this.robots = new ReadOnlyListWrapper<>(FXCollections.observableList(simulation.robotsAtTime(0.0)));
        this.events = new ReadOnlyListWrapper<>(FXCollections.observableArrayList(simulation.getTimeline().values()));
        this.bound.set(simulation.highestKnownLastEventTimeLowerBound());

        this.currentTime.addListener(observable -> {
            final double t = this.currentTime.get();
            this.events.get().addAll(simulation.simulateTillTimestamp(t));
            adjustBound();
            this.robots.setAll(simulation.robotsAtTime(this.currentTime.get()));
        });

    }

    public double getCurrentTime() {
        return currentTime.get();
    }

    public DoubleProperty currentTimeProperty() {
        return currentTime;
    }

    public ReadOnlyDoubleProperty boundProperty() {
        return bound.getReadOnlyProperty();
    }

    private void adjustBound() {
        final double newBound = simulation.highestKnownLastEventTimeLowerBound();
        this.bound.set(newBound);
        if (this.currentTime.get() > newBound) {
            this.currentTime.set(newBound);
        }
    }

    public ReadOnlyListProperty<Robot> robotsProperty() {
        return robots.getReadOnlyProperty();
    }

    public ReadOnlyListProperty<CalculatedEvent> eventsProperty() {
        return events.getReadOnlyProperty();
    }
}
