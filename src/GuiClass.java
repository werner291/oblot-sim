import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class GuiClass extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        Parent root = FXMLLoader.load((getClass().getResource("fxml/Main.fxml")));
        stage.setTitle("Oblivious Point Robot Simulator");
        stage.initStyle(StageStyle.DECORATED);
        stage.setScene(new Scene(root));
        // Show GUI
        stage.show();
    }

    public void startGUI(String[] args) {
        Application.launch(args);
    }
}
