import com.jfoenix.controls.JFXDialog;
import com.jfoenix.controls.JFXDialogLayout;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.AnchorPane;
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
    public void startBtnClick(ActionEvent event) {
        try {
            clientObj.startGame();
        } catch (Exception e) {

        }
    }

    @FXML
    public void inviteBtnClick() {
        JFXDialogLayout dialogContent = new JFXDialogLayout();
        dialogContent.setHeading(new Text("Please select a player from the list"));
        JFXDialog dialog = new JFXDialog(dialogPane, dialogContent, JFXDialog.DialogTransition.CENTER);
        dialog.setOverlayClose(false);

        ArrayList<String> availablePlayers = clientObj.getAvailablePlayers();
        if (availablePlayers.isEmpty()) {
            dialogContent.setBody(new Text("No available players. Try again later."));
            Button btnCancel = new Button("Okay");
            dialogContent.setActions(btnCancel);
            btnCancel.setOnAction(event -> {
                dialog.close();
                dialogPane.setVisible(false);
            });
        } else {
            ListView<String> playerList = new ListView<String>();
            ObservableList<String> playerNames = FXCollections.observableArrayList();
            playerNames.addAll(availablePlayers);
            playerList.setItems(playerNames);
            playerList.setPlaceholder(new Text("NO"));
            playerList.setPrefHeight(230);
            Button btnInvite = new Button("Invite");
            btnInvite.setDisable(true);
            btnInvite.setOnAction(event -> {
                clientObj.invite(playerList.getSelectionModel().getSelectedItem());
                dialog.close();
                dialogPane.setVisible(false);
            });

            playerList.setOnMouseClicked(event -> {
                if (playerList.getSelectionModel().getSelectedItem() != null) {
                    btnInvite.setDisable(false);
                }
            });

            Button btnCancel = new Button("Cancel");
            btnCancel.setOnAction(event -> {
                dialog.close();
                dialogPane.setVisible(false);
            });

            dialogContent.setActions(playerList, btnInvite, btnCancel);
        }

        dialogPane.setVisible(true);
        dialog.show();
    }


//    public void replyInvitation(String username, boolean accept) {
//        if (accept) {
//            if (roomPlayers.size() <= 4) {
//                Button freeButton = this.getFreeButton();
//                if (freeButton != null) {
//                    freeButton.setText(username);
//                    freeButton.setDisable(true);
//                }
//                this.roomPlayers.add(username);
//            } else {
//
//            }
//        } else {
//
//        }
//    }

    public void joinRoom(String username, boolean isHost) {
        Button freeButton = this.getFreeButton();
        if (isHost) {
            btnStart.setDisable(false);
        }
        if (freeButton != null) {
            freeButton.setText(username);
            freeButton.setDisable(true);
        }
        this.roomPlayers.add(username);
    }

    /**
     * Leave / Dismiss button clicked
     */
    @FXML
    public void leaveBtnClick() {
        this.clientObj.leaveRoom();
    }

    /**
     * Update UI when some player left room
     */
    public void leaveRoom(String username, boolean isHost) {
        if (btnPlayer2.getText().equals(username)) {
            btnPlayer2.setText(Constants.EMPTY_BUTTON_TEXT);
            if (isHost) {
                btnPlayer2.setDisable(false);
            }
        } else if (btnPlayer3.getText().equals(username)) {
            btnPlayer3.setText(Constants.EMPTY_BUTTON_TEXT);
            if (isHost) {
                btnPlayer3.setDisable(false);
            }
        } else if (btnPlayer4.getText().equals(username)) {
            btnPlayer4.setText(Constants.EMPTY_BUTTON_TEXT);
            btnPlayer4.setDisable(false);
            if (isHost) {
                btnPlayer4.setDisable(false);
            }
        }
        this.roomPlayers.remove(username);

        if (roomPlayers.size() < Constants.GAME_MIN_PLAYER) {  // need at least 2 players
            btnStart.setDisable(true);
        }
    }

    /**
     * Update UI when host dismissed room
     */
    public void dismissRoom(String username) {
        JFXDialogLayout dialogContent = new JFXDialogLayout();
        dialogContent.setHeading(new Text("Room Dismissed"));
        dialogContent.setBody(new Text("The host " + username + " has dismissed this room."));
        JFXDialog dialog = new JFXDialog(dialogPane, dialogContent, JFXDialog.DialogTransition.CENTER);
        dialog.setOverlayClose(false);
        Button btnClose = new Button("Okay");
        btnClose.setOnAction(event -> {
            dialog.close();
            dialogPane.setVisible(false);
            fadeOut("Menu");
        });
        dialogContent.setActions(btnClose);
        dialogPane.setVisible(true);
        dialog.show();
    }

    private Button getFreeButton() {
        if (this.btnPlayer2.getText().equals(Constants.EMPTY_BUTTON_TEXT)) {
            return btnPlayer2;
        } else if (this.btnPlayer3.getText().equals(Constants.EMPTY_BUTTON_TEXT)) {
            return btnPlayer3;
        } else if (this.btnPlayer4.getText().equals(Constants.EMPTY_BUTTON_TEXT)) {
            return btnPlayer4;
        } else {
            return null;
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    public void startup(boolean isHost, ArrayList<String> roomPlayers){
        hostUsername.setText(roomPlayers.get(0));
        btnHost.setText(roomPlayers.get(0));
        for(String username : roomPlayers){
            if(!username.equals(roomPlayers.get(0))){
                Button freeButton = getFreeButton();
                if(freeButton!=null){
                    freeButton.setText(username);
                }
            }
        }
        if(isHost){
            this.roomPlayers = new ArrayList<>(4);
            this.roomPlayers.add(this.clientObj.getUsername());
            btnLeave.setText("Dismiss");
        } else {
            this.roomPlayers = roomPlayers;
            btnPlayer2.setDisable(true);
            btnPlayer3.setDisable(true);
            btnPlayer4.setDisable(true);
            btnLeave.setText("Leave");
        }

        btnStart.setDisable(true);
        roomNumber.setText(Integer.toString(this.clientObj.getRoomNumber()));
    }


    public void setClientObj(GameClient clientObj) {
        this.clientObj = clientObj;
    }


    /* This method is used to provide a smoother transition between scenes */
    public void fadeOut(String scene) {
        FadeTransition fadeTransition = new FadeTransition();
        fadeTransition.setDuration(Duration.millis(500));
        fadeTransition.setNode(rootPane);
        fadeTransition.setFromValue(1);
        fadeTransition.setToValue(0);

        switch(scene) {
            case "Game":
                loadGameScene();
                break;
            case "Menu":
                loadMenuScene();
                break;
        }

        fadeTransition.setOnFinished(event -> loadGameScene());
        fadeTransition.play();
    }

    private void loadMenuScene() {
        try {
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

        } catch (IOException e) {
            System.out.println("Cannot find Menu.fxml");
        }
    }


    private void loadGameScene() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("Game.fxml"));
            Parent gameView = loader.load();
            Scene gameScene = new Scene(gameView);
            GameController controller = loader.getController();
            controller.setClientObj(this.clientObj);
            this.clientObj.setGameController(controller);
            controller.startup();
            Stage currentStage = (Stage) rootPane.getScene().getWindow();

            // override the onCloseRequest and notify server to remove user.
            currentStage.setOnCloseRequest(t -> {
                System.out.println("Closing at the Game scene. ");
                clientObj.logout();
                Platform.exit();
                System.exit(0);
            });

            currentStage.setScene(gameScene);

        } catch (IOException e) {
            System.out.println("Cannot find Game.fxml");
        }
    }
}
