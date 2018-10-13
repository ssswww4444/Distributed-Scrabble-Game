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
import javafx.scene.layout.Background;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
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

    private HashMap<String, ArrayList<Integer>> roomPlayerInfoMap;  // reference of roomPlayers in gc

    /* UI element methods */

    @FXML
    public void startBtnClick(ActionEvent event) {

        if (clientObj.isHost()) {  // start game
            try {
                clientObj.startGame();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {    // ready
            clientObj.ready();
            btnStart.setDisable(true);
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

    public void playerReady(int pos) {
        Button targetButton = buttonAtPos(pos);
        targetButton.setStyle("-fx-text-fill: #0000ff");  // blue
        if (clientObj.isHost()) {
            if (allReady()) {
                btnStart.setDisable(false);
            }
        }
    }

    /**
     * Check if all players ready
     */
    private boolean allReady() {
        // check if all ready
        for (ArrayList<Integer> infoList: roomPlayerInfoMap.values()) {
            if (infoList.get(1) == 0) {  // not ready
                return false;
            }
        }
        return true;
    }

    private Button buttonAtPos(int pos) {
        switch (pos) {
            case 1:
                return btnHost;
            case 2:
                return btnPlayer2;
            case 3:
                return btnPlayer3;
            case 4:
                return btnPlayer4;
        }
        return null;
    }

    public void joinRoom(String username, int pos) {  // user join room at pos
        if (clientObj.isHost()) {
            btnStart.setDisable(true);  // disable start when any new player enter room
        }

        Button targetButton = buttonAtPos(pos);
        if (targetButton != null) {
            targetButton.setText(username);
            targetButton.setDisable(true);
        }
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
    public void leaveRoom(int pos, boolean isHost) {

//        int pos = roomPlayerInfoMap.get(username).get(0);
        Button targetButton = buttonAtPos(pos);
        targetButton.setText(Constants.EMPTY_BUTTON_TEXT);
        targetButton.setStyle("-fx-text-fill: #000000");   // black
        if (isHost) {
            targetButton.setDisable(false);
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

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    public void startup(boolean isHost, HashMap<String, ArrayList<Integer>> roomPlayerInfoMap){
        this.roomPlayerInfoMap = roomPlayerInfoMap;  // get reference of roomPlayers in gc
        roomNumber.setText(Integer.toString(this.clientObj.getRoomNumber()));

        for (String name: roomPlayerInfoMap.keySet()) {
            int pos = roomPlayerInfoMap.get(name).get(0);

            if (pos == 1) {
                hostUsername.setText(name);
                btnHost.setText(name);
                btnHost.setDisable(true);
            } else {
                Button targetButton = buttonAtPos(pos);
                targetButton.setText(name);
                targetButton.setDisable(true);
                if (roomPlayerInfoMap.get(name).get(1) == 1) {  // is ready
                    targetButton.setStyle("-fx-text-fill: #0000ff");
                }
            }
        }

        if(isHost){
            btnLeave.setText("Dismiss");
            if (allReady()) {
                btnStart.setDisable(false);
            } else {
                btnStart.setDisable(true);  // need players to "ready"
            }
        } else {
            btnPlayer2.setDisable(true);
            btnPlayer3.setDisable(true);
            btnPlayer4.setDisable(true);
            btnLeave.setText("Leave");
            btnStart.setText("Ready");
            btnStart.setDisable(false);
        }
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
                fadeTransition.setOnFinished(event -> loadGameScene());
                break;
            case "Menu":
                fadeTransition.setOnFinished(event -> loadMenuScene());
                break;
        }

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
            this.clientObj.removeRoomController();
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
            this.clientObj.removeRoomController();
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
