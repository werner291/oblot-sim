package nl.tue.oblotsim.gui;

import nl.tue.oblotsim.Simulator.Simulation;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;
import java.net.URL;

/**
 * Responsible for starting the nl.tue.oblotsim.GUI and fetching the nl.tue.oblotsim.Main.fxml layout file
 * If you want to edit what the functions hooked up to gui elements do, look at the {@link FxFXMLController}
 * If you want to edit what functions nl.tue.oblotsim.GUI elements trigger either edit the nl.tue.oblotsim.Main.fxml file or the Controller file
 */
public class GUI extends Application {
    // only one simulator for every gui. Needs to be static because Application.launch launches a new nl.tue.oblotsim.GUI instance
    private static Simulation simulation;
    private static Class[] algorithms;

    public static Stage stage;
    @Override
    public void start(Stage stage) {
        try {
            URL fxmlFile = getClass().getClassLoader().getResource("Main.fxml");
            if (fxmlFile == null) {
                throw new NullPointerException("The nl.tue.oblotsim.Main.fxml file does not exist.");
            }
            FXMLLoader loader = new FXMLLoader(fxmlFile);
            Parent root = loader.load();
            loader.<FxFXMLController>getController().setSimulation(GUI.simulation); // set the simulator of the controller
            loader.<FxFXMLController>getController().setAlgorithms(GUI.algorithms); // set the simulator of the controller

            GUI.stage = stage;
            stage.setTitle("Oblivious Point Robot nl.tue.oblotsim.Simulator.nl.tue.oblotsim.Simulator");
            stage.initStyle(StageStyle.DECORATED);
            stage.setScene(new Scene(root));
            stage.setWidth(1600);
            stage.setHeight(900);
            // Show nl.tue.oblotsim.GUI
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Starts the gui. Does not return until the gui is exited.
     * @param args the arguments to start the application
     * @param simulation the simulator to start the application with
     */
    public static void runGUI(Simulation simulation, Class[] algorithms) {
        GUI.simulation = simulation;
        GUI.algorithms = algorithms;
        Application.launch();
    }
}
