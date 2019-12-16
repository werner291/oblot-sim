package GUI;

import Simulator.Simulator;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;
import java.net.URL;

/**
 * Responsible for starting the GUI and fetching the Main.fxml layout file
 * If you want to edit what the functions hooked up to gui elements do, look at the {@link FxFXMLController}
 * If you want to edit what functions GUI elements trigger either edit the Main.fxml file or the Controller file
 */
public class GUI extends Application {
    // only one simulator for every gui. Needs to be static because Application.launch launches a new GUI instance
    private static Simulator simulator;
    public static Stage stage;
    @Override
    public void start(Stage stage) {
        try {
            URL fxmlFile = getClass().getClassLoader().getResource("Main.fxml");
            if (fxmlFile == null) {
                throw new NullPointerException("The Main.fxml file does not exist.");
            }
            FXMLLoader loader = new FXMLLoader(fxmlFile);
            Parent root = loader.load();
            loader.<FxFXMLController>getController().setSimulator(GUI.simulator); // set the simulator of the controller

            GUI.stage = stage;
            stage.setTitle("Oblivious Point Robot Simulator.Simulator");
            stage.initStyle(StageStyle.DECORATED);
            stage.setScene(new Scene(root));
            stage.setWidth(1920);
            stage.setHeight(1080);
            // Show GUI
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Starts the gui. Does not return until the gui is exited.
     * @param args the arguments to start the application
     * @param simulator the simulator to start the application with
     */
    public static void runGUI(String[] args, Simulator simulator) {
        GUI.simulator = simulator;
        Application.launch(args);
    }
}
