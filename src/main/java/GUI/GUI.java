package GUI;

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
    @Override
    public void start(Stage stage) {
        try {
            URL fxmlFile = getClass().getClassLoader().getResource("Main.fxml");
            if (fxmlFile == null) {
                throw new NullPointerException("The Main.fxml file does not exist.");
            }
            Parent root = FXMLLoader.load(fxmlFile);
            stage.setTitle("Oblivious Point Robot Simulator");
            stage.initStyle(StageStyle.DECORATED);
            stage.setScene(new Scene(root));
            // Show GUI
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Starts the gui
     * @param args the arguments to start the application
     */
    public void startGUI(String[] args) {
        Application.launch(args);
    }
}
