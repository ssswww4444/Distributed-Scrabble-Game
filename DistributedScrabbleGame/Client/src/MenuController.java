import com.jfoenix.controls.JFXDialog;
import com.jfoenix.controls.JFXDialogLayout;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
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
public class MenuController implements Initializable {

    @FXML
    private AnchorPane rootPane;
    /* UI elements */

    @FXML
    private Button btnRefreshPlayers;
    @FXML
    private Button btnCreateRoom;

    @FXML
    private Label userLabel;

    @FXML
    private TableView<PlayerModel> playerList;
    @FXML
    private TableColumn username;
    @FXML
    private TableColumn status;
    @FXML
    private StackPane dialogPane;

    @FXML
    public void createBtnClick(ActionEvent event) {
        try {
            clientObj.createRoom();
            this.btnCreateRoom.setDisable(true);
        } catch (Exception e) {
            e.printStackTrace();
            displayMsg();
        }
    }

    @FXML
    public void refreshBtnClick(ActionEvent event) {
        btnRefreshPlayers.setDisable(true);
//        refreshPlayerList();
        this.clientObj.renderPlayerList();
        btnRefreshPlayers.setDisable(false);
    }

    private GameClient clientObj;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        /* Initialize the association of columns in TableView elements */
        username.setCellValueFactory(new PropertyValueFactory<>("username"));
        status.setCellValueFactory(new PropertyValueFactory<>("status"));
    }

    public void refresh() {
        this.userLabel.setText(this.clientObj.getUsername());
//        refreshPlayerList();
        this.clientObj.renderPlayerList();
    }

    public void updatePlayerList(ArrayList<PlayerModel> updatedPlayers) {
        Platform.runLater(() -> {
            MenuController.this.playerList.getItems().clear();
            for (PlayerModel player : updatedPlayers) {
                MenuController.this.playerList.getItems().add(player);
            }
        });
    }

//    public void refreshPlayerList() {
//        ArrayList<PlayerModel> refreshedPlayers = clientObj.getPlayerList();
//        Platform.runLater(() -> {
//            MenuController.this.playerList.getItems().clear();
//            for (PlayerModel player : refreshedPlayers) {
//                MenuController.this.playerList.getItems().add(player);
//            }
//        });
//    }


    /* This method is used to provide a smoother transition between scences */
    public void fadeOut(boolean isHost, HashMap<String, ArrayList<Integer>> roomPlayerInfoMap) {
        FadeTransition fadeTransition = new FadeTransition();
        fadeTransition.setDuration(Duration.millis(500));
        fadeTransition.setNode(rootPane);
        fadeTransition.setFromValue(1);
        fadeTransition.setToValue(0);

        fadeTransition.setOnFinished(event -> loadRoomScene(isHost, roomPlayerInfoMap));
        fadeTransition.play();
    }

    @FXML
    public void invitationMsg(String username, int roomNumber) {
        dialogPane.getChildren().clear();
        JFXDialogLayout dialogContent = new JFXDialogLayout();
        dialogContent.setHeading(new Text("Room Invitation"));
        dialogContent.setBody(new Text("User " + username + " invited you to Room: " + roomNumber + ". Accept?"));
        JFXDialog dialog = new JFXDialog(dialogPane, dialogContent, JFXDialog.DialogTransition.CENTER);
        dialog.setOverlayClose(false);
        Button btnYes = new Button("Yes");
        btnYes.setOnAction(event -> {
            dialog.close();
            dialogPane.setVisible(false);
            clientObj.acceptInvitation(roomNumber);
        });

        Button btnNo = new Button("No");
        btnNo.setOnAction(event -> {
            dialog.close();
            dialogPane.setVisible(false);
        });

        dialogContent.setActions(btnYes, btnNo);
        dialogPane.setVisible(true);
        dialog.show();
    }

    @FXML
    public void loadRoom(boolean isHost, HashMap<String, ArrayList<Integer>> roomPlayerInfoMap) {
        fadeOut(isHost, roomPlayerInfoMap);
    }

    @FXML
    public void displayMsg() {
        dialogPane.getChildren().clear();
        JFXDialogLayout dialogContent = new JFXDialogLayout();
        dialogContent.setHeading(new Text("Error Message"));
        dialogContent.setBody(new Text("The room is full, dismissed or game already started."));
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

    private void loadRoomScene(boolean isHost, HashMap<String, ArrayList<Integer>> roomPlayerInfoMap) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("Room.fxml"));
            Parent roomView = loader.load();
            Scene roomScene = new Scene(roomView);
            RoomController controller = loader.getController();
            controller.setClientObj(this.clientObj);
            this.clientObj.setRoomController(controller);
            this.clientObj.removeMenuController();
            controller.startup(isHost, roomPlayerInfoMap);
            Stage currentStage = (Stage) rootPane.getScene().getWindow();

            // override the onCloseRequest and notify server to remove user.
            currentStage.setOnCloseRequest(t -> {
                System.out.println("Closing at the Room scene. ");
                clientObj.logout();
                Platform.exit();
                System.exit(0);
            });

            currentStage.setScene(roomScene);

        } catch (IOException e) {
            System.out.println("Cannot find room scene fxml");
        }
    }

    @FXML
    public void serverDownMsg() {
        Platform.runLater(()->{
            dialogPane.getChildren().clear();
            JFXDialogLayout dialogContent = new JFXDialogLayout();
            dialogContent.setHeading(new Text("Error Message"));
            dialogContent.setBody(new Text("Server is down. Please reconnect later."));
            JFXDialog dialog = new JFXDialog(dialogPane, dialogContent, JFXDialog.DialogTransition.CENTER);
            dialog.setOverlayClose(false);
            Button btnClose = new Button("Okay");
            btnClose.setOnAction(event -> {
                dialog.close();
                dialogPane.setVisible(false);
                loadLoginScene();
            });
            dialogContent.setActions(btnClose);
            dialogPane.setVisible(true);
            dialog.show();
        });
    }

    private void loadLoginScene() {
        Platform.runLater(()->{
            FadeTransition fadeTransition = new FadeTransition();
            fadeTransition.setDuration(Duration.millis(500));
            fadeTransition.setNode(rootPane);
            fadeTransition.setFromValue(1);
            fadeTransition.setToValue(0);

            fadeTransition.setOnFinished(event -> {
                try {

                    FXMLLoader loader = new FXMLLoader(getClass().getResource("Login.fxml"));
                    Parent loginView = loader.load();
                    Scene loginScene = new Scene(loginView);
                    Stage currentStage = (Stage) rootPane.getScene().getWindow();
                    currentStage.setOnCloseRequest(t -> {
                        System.out.println("Primary Stage is closing, process is killed! ");
                        Platform.exit();
                        System.exit(0);
                    });
                    currentStage.setScene(loginScene);

                } catch (IOException e) {
                    System.out.println("Cannot find login scene fxml");
                }
            });

            fadeTransition.play();
        });
    }

    public void setClientObj(GameClient clientObj) {
        this.clientObj = clientObj;
    }

}
