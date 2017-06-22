package federaci;

import ambasador.GUIAmbassador;
import hla.rti.*;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;

import static federaci.AbstractFederat.log;

public class GUIStart extends Application
{
    static FederatGUI federatGUI;
    @Override
    public void start(Stage primaryStage) throws Exception
    {
        Parent root = FXMLLoader.load(getClass().getResource("federatGUI.fxml"));
        primaryStage.setTitle("I6B2S4 Joanna Bednarko i Joanna_Koszela");
        primaryStage.setScene(new Scene(root, 1024, 786));
        primaryStage.setResizable(false);

        primaryStage.show();
    }

    public static void main(String[] args)
    {
        launch(args);
//        try
//        {
//            federatGUI = new FederatGUI();
//            federatGUI.runFederate();
//            log("Wystartowal " + federatGUI.federateName);
//        }
//        catch(Exception e)
//        {
//            e.printStackTrace();
//        }
    }

}
