package federaci;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class FederatGUI extends Application
{
    @Override
    public void start(Stage primaryStage) throws Exception
    {
        Parent root = FXMLLoader.load(getClass().getResource("federatGUI.fxml"));
        primaryStage.setTitle("I6B2S4 Joanna Bednarko i Joanna_Koszela.pdf");
        primaryStage.setScene(new Scene(root, 1024, 786));
        primaryStage.setResizable(false);
        primaryStage.show();
    }


    public static void main(String[] args)
    {
        launch(args);
    }
}
