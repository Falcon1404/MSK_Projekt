package federaci;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;

public class ControllerGUI
{
    @FXML public Button buttonStart;
    @FXML public TextArea log;

    public void buttonStartAction(ActionEvent actionEvent)
    {
        //buttonStart.setDisable(true);
    }
}
