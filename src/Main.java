import fxml.GuiClass;

/**
 * The public class that we will use to start our GUI. This is an example class of how the simulator may be used.
 * This does not contain any logic regarding the simulator whatsoever, but rather sets it up and runs it.
 */

public class Main{

    public static void main(String[] args){
        // We're keeping this
        System.out.println("Most awesome simulator ever.");
        GuiClass gui = new GuiClass();
        gui.startGUI(args);
    }
}
