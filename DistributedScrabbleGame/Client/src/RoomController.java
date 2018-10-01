/*
    COMP90015 Project 1 ClientAppStartController.java
    Student Name: Haowen Tang
    Student Number: 706892
    Tutor: Alisha Aneja
 */

import com.jfoenix.controls.JFXDialog;
import com.jfoenix.controls.JFXDialogLayout;
import javafx.animation.FadeTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

/* Controller class for the start scene of the Client App */
public class RoomController implements Initializable {

    /* UI elements */
    @FXML
    private AnchorPane rootPane;
    @FXML
    private Button btnStart;
    @FXML
    private Label roomNumber;
    @FXML
    private Label hostUsername;
    @FXML
    private Button btnHost;
    @FXML
    private Button btnPlayer2;
    @FXML
    private Button btnPlayer3;
    @FXML
    private Button btnPlayer4;
    @FXML
    private StackPane dialogPane;
    @FXML
    private Button btnLeave;


    private GameClient clientObj;

    private ArrayList<String> roomPlayers;

    /* UI element methods */

    @FXML
    public void startBtnClick(ActionEvent event){
        try{
            clientObj.newGame();
            fadeOut();
        }catch(Exception e){

        }
    }

    @FXML
    public void inviteBtnClick(){
        JFXDialogLayout dialogContent = new JFXDialogLayout();
        dialogContent.setHeading(new Text("Please select a player from the list"));
        JFXDialog dialog = new JFXDialog(dialogPane, dialogContent, JFXDialog.DialogTransition.CENTER);
        dialog.setOverlayClose(false);

        ArrayList<String> availablePlayers = clientObj.getAvailablePlayers();
        if(availablePlayers.isEmpty()){
            dialogContent.setBody(new Text("No available players. Try again later."));
            Button btnCancel = new Button("Okay");
            dialogContent.setActions(btnCancel);
            btnCancel.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    dialog.close();
                    dialogPane.setVisible(false);
                }
            });
        }else{
            ListView<String> playerList = new ListView<String>();
            ObservableList<String> playerNames = FXCollections.observableArrayList();
            playerNames.addAll(availablePlayers);
            playerList.setItems(playerNames);
            playerList.setPlaceholder(new Text("NO"));
            playerList.setPrefHeight(230);
            Button btnInvite = new Button("Invite");
            btnInvite.setDisable(true);
            btnInvite.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    clientObj.invite(playerList.getSelectionModel().getSelectedItem());
                    dialog.close();
                    dialogPane.setVisible(false);
                }
            });

            playerList.setOnMouseClicked(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    if(playerList.getSelectionModel().getSelectedItem()!=null){
                        btnInvite.setDisable(false);
                    }
                }
            });

            Button btnCancel = new Button("Cancel");
            btnCancel.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    dialog.close();
                    dialogPane.setVisible(false);
                }
            });

            dialogContent.setActions(playerList, btnInvite, btnCancel);
        }

        dialogPane.setVisible(true);
        dialog.show();
    }

    public void replyInvitation(String username, boolean accept){
        if(accept){
            if(roomPlayers.size()<=4){
                Button freeButton = this.getFreeButton();
                if(freeButton!=null){
                    freeButton.setText(username);
                    freeButton.setDisable(true);
                }
                this.roomPlayers.add(username);
            }else {

            }
        }else{

        }
    }

    private Button getFreeButton(){
        if(!this.btnPlayer2.isDisable()){
            return btnPlayer2;
        }else if(!this.btnPlayer3.isDisable()){
            return btnPlayer3;
        }else if(!this.btnPlayer4.isDisable()){
            return btnPlayer4;
        }else {
            return null;
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources){

    }

    public void startup(boolean isHost){
        if(isHost){
            hostUsername.setText(this.clientObj.getUsername());
            btnHost.setText(this.clientObj.getUsername());
            this.roomPlayers = new ArrayList<String>(4);
            roomPlayers.add(this.clientObj.getUsername());
            btnLeave.setText("Dismiss");
        }

        roomNumber.setText(Integer.toString(this.clientObj.getRoomNumber()));
    }

    public void setClientObj(GameClient clientObj){
        this.clientObj = clientObj;
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("Game.fxml"));
            Parent gameView = loader.load();
            Scene gameScene = new Scene(gameView);
            GameController controller = loader.getController();
            controller.setClientObj(this.clientObj);
            this.clientObj.setGameController(controller);
            controller.startup();
            Stage currentStage = (Stage) rootPane.getScene().getWindow();
            currentStage.setScene(gameScene);

        }catch(IOException e){
            System.out.println("Cannot find game scene fxml");
        }
    }
}