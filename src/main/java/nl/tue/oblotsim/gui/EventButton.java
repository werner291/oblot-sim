package nl.tue.oblotsim.gui;
import javafx.scene.control.Button;

public class EventButton extends Button {
    private double timeStamp = 0;

    public EventButton(String label, double timeStamp) {
        super.setText(label);
        this.timeStamp = timeStamp;
    }

    public void setTimeStamp(double timeStamp) {
        this.timeStamp = timeStamp;
    }

    public double getTimeStamp() {
        return timeStamp;
    }
}
