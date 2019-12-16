package GUI;
import Schedulers.Event;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;
import java.net.URL;

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
