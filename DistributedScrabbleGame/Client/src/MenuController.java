import com.jfoenix.controls.JFXDialog;
import com.jfoenix.controls.JFXDialogLayout;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
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
    public void createBtnClick(ActionEvent event){
        try{
            clientObj.createRoom();
            this.btnCreateRoom.setDisable(true);
            fadeOut();
        }catch(Exception e){
            displayMsg();
        }
    }
    @FXML
    public void refreshBtnClick(ActionEvent event){
        btnRefreshPlayers.setDisable(true);
        refreshPlayerList();
        btnRefreshPlayers.setDisable(false);
    }

    private GameClient clientObj;

    @Override
    public void initialize(URL location, ResourceBundle resources){
        /* Initialize the association of columns in TableView elements */
        username.setCellValueFactory(new PropertyValueFactory<>("username"));
        status.setCellValueFactory(new PropertyValueFactory<>("status"));
    }

    public void refresh(){
        this.userLabel.setText(this.clientObj.getUsername());
        refreshPlayerList();
    }

    public void updatePlayerList(ArrayList<PlayerModel> updatedPlayers){
        Platform.runLater(() -> {
            MenuController.this.playerList.getItems().clear();
            for(PlayerModel player : updatedPlayers){
                MenuController.this.playerList.getItems().add(player);
            }
        });
    }

    public void refreshPlayerList(){
        ArrayList<PlayerModel> refreshedPlayers = clientObj.getPlayerList();
        Platform.runLater(new Runnable() {
            @Override
            public void run(){
                MenuController.this.playerList.getItems().clear();
                for(PlayerModel player : refreshedPlayers){
                    MenuController.this.playerList.getItems().add(player);
                }
            }
        });
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

    @FXML
    public void displayMsg(){
        JFXDialogLayout dialogContent = new JFXDialogLayout();
        dialogContent.setHeading(new Text("Error Message"));
        dialogContent.setBody(new Text("Room creation failed. Please try again."));
        JFXDialog dialog = new JFXDialog(dialogPane, dialogContent, JFXDialog.DialogTransition.CENTER);
        dialog.setOverlayClose(false);
        Button btnClose = new Button("Okay");
        btnClose.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                dialog.close();
                dialogPane.setVisible(false);
            }
        });
        dialogContent.setActions(btnClose);
        dialogPane.setVisible(true);
        dialog.show();
    }

    private void loadMainScence(){
        try{
            FXMLLoader loader = new FXMLLoader(getClass().getResource("Room.fxml"));
            Parent roomView = loader.load();
            Scene roomScene = new Scene(roomView);
            RoomController controller = loader.getController();
            controller.setClientObj(this.clientObj);
            this.clientObj.setRoomController(controller);
            controller.startup(true);
            Stage currentStage = (Stage) rootPane.getScene().getWindow();
            currentStage.setScene(roomScene);

        }catch(IOException e){
            System.out.println("Cannot find main scene fxml");
        }
    }

    public void setClientObj(GameClient clientObj){
        this.clientObj = clientObj;
    }
}
