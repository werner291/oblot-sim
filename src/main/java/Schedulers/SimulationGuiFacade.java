package Schedulers;

import Simulator.Robot;
import Simulator.Simulation;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * A class that bridges the property-based world of JavaFX and the functionally immutuable Simulation object.
 */
public class SimulationGuiFacade {

    final private Simulation simulation;

    final private ReadOnlyListWrapper<Robot> robots;

    public double getCurrentTime() {
        return currentTime.get();
    }

    public DoubleProperty currentTimeProperty() {
        return currentTime;
    }

    final private DoubleProperty currentTime = new SimpleDoubleProperty(0.0);

    public ReadOnlyDoubleProperty boundProperty() {
        return bound.getReadOnlyProperty();
    }

    final private ReadOnlyDoubleWrapper bound = new ReadOnlyDoubleWrapper(0.0);

    public SimulationGuiFacade(Simulation simulation)
    {
        this.simulation = simulation;
        this.robots = new ReadOnlyListWrapper<>(FXCollections.observableList(simulation.robotsAtTime(0.0)));
        this.bound.set(simulation.highestKnownLastEventTimeLowerBound());

        this.currentTime.addListener(observable -> {
            final double t = this.currentTime.get();
            simulation.simulateTillTimestamp(t);
            adjustBound();
            this.robots.setAll(simulation.robotsAtTime(this.currentTime.get()));
        });
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
}
