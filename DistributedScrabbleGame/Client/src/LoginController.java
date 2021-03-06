/*
    COMP90015 Project 2 LoginController.java
    Group Name: Distributed Otaku
    Tutor: Alisha Aneja
 */

import com.jfoenix.controls.JFXDialogLayout;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import javafx.util.Duration;
import javafx.stage.Stage;
import javafx.scene.Scene;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import com.jfoenix.controls.JFXDialog;

/* Controller class for the start scene of the Client App */
public class LoginController implements Initializable {

    private GameClient clientObj;

    /* UI elements */
    @FXML
    private AnchorPane rootPane;
    @FXML
    private Button btnConnect;
    @FXML
    private TextField usernameField;
    @FXML
    private TextField hostname;
    @FXML
    private StackPane dialogPane;

    @FXML
    public void connectBtnClick(ActionEvent event){
        try{
            this.clientObj = new GameClient(hostname.getText(), usernameField.getText());
            this.btnConnect.setDisable(true);
            fadeOut();
        } catch(Exception e){
            this.displayMsg(e.getMessage());
        }
    }

    @FXML
    public void displayMsg(String message){
        dialogPane.getChildren().clear();
        JFXDialogLayout dialogContent = new JFXDialogLayout();
        dialogContent.setHeading(new Text("Error Message"));
//        dialogContent.setBody(new Text("Username not available. Please choose another one."));
        dialogContent.setBody(new Text(message));
        JFXDialog dialog = new JFXDialog(dialogPane, dialogContent, JFXDialog.DialogTransition.CENTER);
        dialog.setOverlayClose(false);
        Button btnClose = new Button("Okay");
        btnClose.setOnAction(event -> {
            dialog.close();
            dialogPane.setVisible(false);
        });
        dialogContent.setActions(btnClose);
        dialogPane.setVisible(true);
        dialog.show();
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

        fadeTransition.setOnFinished(event -> loadMenuScene());
        fadeTransition.play();
    }

    private void loadMenuScene(){
        try{
            FXMLLoader loader = new FXMLLoader(getClass().getResource("Menu.fxml"));
            Parent menuView = loader.load();
            Scene menuScene = new Scene(menuView);
            MenuController controller = loader.getController();
            controller.setClientObj(this.clientObj);
            this.clientObj.setMenuController(controller);
            controller.refresh();
            Stage currentStage = (Stage) rootPane.getScene().getWindow();

            // override the onCloseRequest and notify server to remove user.
            currentStage.setOnCloseRequest(t -> {
                System.out.println("Closing at the Menu scene. ");
                clientObj.logout();
                Platform.exit();
                System.exit(0);
            });

            currentStage.setScene(menuScene);

        }catch(IOException e){
            System.out.println("Cannot find Menu.fxml");
        }
    }
}
