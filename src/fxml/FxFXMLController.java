package fxml;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import javax.swing.*;

public class FxFXMLController
{
    @FXML
    // The reference of inputText will be injected by the FXML loader
    private ProgressBar progressBarSimulation;

    @FXML
    // The reference of inputText will be injected by the FXML loader
    private Button playButton;

    @FXML
    // The reference of inputText will be injected by the FXML loader
    private ScrollPane eventList;

    // The reference of outputText will be injected by the FXML loader
    @FXML
    private Slider dragBarSimulation;

    // location and resources will be automatically injected by the FXML loader
    @FXML
    private URL help;

    @FXML
    private ResourceBundle resources;

    // Add a public no-args constructor
    public FxFXMLController()
    {
    }

    @FXML
    private void initialize()
    {
    }

    @FXML
    private void onSave()
    {
        System.out.println("Save");
    }

    @FXML
    private void onLoad()
    {
        System.out.println("Load");
    }

    @FXML
    private void onClear()
    {
        System.out.println("Clear");
    }

    @FXML
    private void onQuit()
    {
        System.exit(0);
    }

    @FXML
    private void onFsync()
    {
        System.out.println("Fsync");
    }

    @FXML
    private void onSsync()
    {
        System.out.println("Ssync");
    }

    @FXML
    private void onAsync()
    {
        System.out.println("Async");
    }

    @FXML
    private void onFullshared()
    {
        System.out.println("Full Shared");
    }

    @FXML
    private void onOrienShared()
    {
        System.out.println("Orientation Shared");
    }

    @FXML
    private void onNothingShared()
    {
        System.out.println("Nothing Shared");
    }

    @FXML
    private void onGettingStarted()
    {
        System.out.println("Getting Started");
    }

    @FXML
    private void onAbout()
    {
        System.out.println("About");
    }

    @FXML
    private void playSimulation()
    {
        System.out.println("Playing Simulation");
        progressBarSimulation.setProgress(100);

        eventList.setContent(buildContent());
        eventList.setPannable(true); // it means that the user should be able to pan the viewport by using the mouse.
        playButton.setText("ReCompute");
    }

    private Node buildContent() {
        VBox events = new VBox();
        events.getChildren().addAll(createEventButton(), createEventButton(), createEventButton(), createEventButton(), createEventButton(), createEventButton(),
                createEventButton(), createEventButton(), createEventButton(), createEventButton(), createEventButton(), createEventButton(), createEventButton(),
                createEventButton(), createEventButton(), createEventButton(), createEventButton(), createEventButton(), createEventButton(), createEventButton(),
                createEventButton(), createEventButton(), createEventButton(), createEventButton(), createEventButton(), createEventButton(), createEventButton(),
                createEventButton(), createEventButton(), createEventButton(), createEventButton(), createEventButton(), createEventButton(), createEventButton(),
                createEventButton(), createEventButton(), createEventButton(), createEventButton(), createEventButton(), createEventButton(), createEventButton());
        events.setSpacing(1);
        events.setPadding(new Insets(1));

        return events;
    }

    static private int eventNr = 0;
    private Button createEventButton() {
        Button eventButton = new Button("EVENT: " + eventNr);
        eventButton.setPrefWidth(150);
        eventButton.setMaxWidth(200);
        eventButton.setMinWidth(25);
        eventNr++;
        return eventButton;
    }
}
