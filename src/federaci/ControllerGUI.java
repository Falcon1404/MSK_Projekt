package federaci;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;

public class ControllerGUI
{
    @FXML public Button buttonStart;
    @FXML public Button buttonStop;
    @FXML public TextArea log;

    @FXML
    public void buttonStartAction(ActionEvent actionEvent)
    {
        System.out.println("Start");
    }

    @FXML
    public void buttonStopAction(ActionEvent actionEvent)
    {
        System.out.println("Stop");
    }
}
