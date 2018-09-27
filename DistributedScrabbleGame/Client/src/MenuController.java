/*
    COMP90015 Project 1 ClientAppStartController.java
    Student Name: Haowen Tang
    Student Number: 706892
    Tutor: Alisha Aneja
 */

import javafx.animation.FadeTransition;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

/* Controller class for the start scene of the Client App */
public class MenuController implements Initializable {

    @FXML
    private AnchorPane rootPane;
    /* UI elements */
    @FXML
    private Button btnConnect;

    @FXML
    public void connectBtnClick(ActionEvent event){
        fadeOut();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources){

    }

    /* This method is used to provide a smoother transition between scences */
    public void fadeOut(){
        FadeTransition fadeTransition = new FadeTransition();
        fadeTransition.setDuration(Duration.millis(500));
        fadeTransition.setNode(rootPane);
        fadeTransition.setFromValue(1);
        fadeTransition.setToValue(0);

        fadeTransition.setOnFinished(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                loadMainScence();
            }
        });
        fadeTransition.play();
    }

    private void loadMainScence(){
        try{
            FXMLLoader loader = new FXMLLoader(getClass().getResource("Menu.fxml"));
            Parent mainView = loader.load();
            Scene mainScene = new Scene(mainView);
            Stage currentStage = (Stage) rootPane.getScene().getWindow();
            currentStage.setScene(mainScene);

        }catch(IOException e){
            System.out.println("Cannot find main scene fxml");
        }
    }
}
