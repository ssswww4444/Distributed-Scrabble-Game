/*
    COMP90015 Project 1 ClientAppStartController.java
    Student Name: Haowen Tang
    Student Number: 706892
    Tutor: Alisha Aneja
 */

import com.jfoenix.controls.JFXDialog;
import com.jfoenix.controls.JFXDialogLayout;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.TilePane;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.lang.reflect.Array;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.ResourceBundle;

/* Controller class for the start scene of the Client App */
public class GameController implements Initializable {

    /* UI elements */
    @FXML
    private AnchorPane rootPane;
    @FXML
    private Button btnVote;
    @FXML
    private Button btnNoWord;
    @FXML
    private Label roomNumber;
    @FXML
    private Label stateLabel;
    @FXML
    private Label turnLabel;
    @FXML
    private TilePane gameBoard;
    @FXML
    private TilePane letterBoard;
    @FXML
    private Button btnPass;
    @FXML
    private StackPane dialogPane;
    @FXML
    private TableView<ScoreModel> scoreList;
    @FXML
    private TableColumn username;
    @FXML
    private TableColumn score;
    @FXML
    private Button btnLeave;

    private ArrayList<TextField> occupiedCells;

    private ArrayList<TextField> NonOccupiedCells;

    private TextField chosenCell;

    private ArrayList<ToggleButton> letterBtns;

    private ToggleButton btnSelected;

    private String state;

    private TextField head;

    private TextField tail;

    private GameClient clientObj;

    private String chosenWord;


    @FXML
    private void passBtnClick(ActionEvent event){
        this.clientObj.pass();
        this.btnPass.setDisable(true);
    }

    @FXML
    private void noWordBtnClick(ActionEvent event){
        this.clientObj.noWord();
        this.btnNoWord.setDisable(true);
        this.enterStateNotMyTurn();
    }


    @FXML
    public void voteBtnClick(ActionEvent event){
        this.enterStateWait();
        this.btnVote.setDisable(true);
        System.out.println(chosenWord);
        this.clientObj.sendVoteRequest(getRow(head), getCol(head), this.chosenWord, isHorizontal(head, tail));

        dialogPane.getChildren().clear();
        JFXDialogLayout dialogContent = new JFXDialogLayout();
        dialogContent.setHeading(new Text("Waiting for vote result..."));
        ProgressIndicator loadingIcon = new ProgressIndicator();
        loadingIcon.setProgress(ProgressIndicator.INDETERMINATE_PROGRESS);
        dialogContent.setBody(loadingIcon);
        JFXDialog dialog = new JFXDialog(dialogPane, dialogContent, JFXDialog.DialogTransition.CENTER);
        dialog.setOverlayClose(false);
        dialogPane.setVisible(true);
        dialog.show();
    }

    private boolean isHorizontal(TextField start, TextField end){
        if(getRow(start)==getRow(end)){
            return true;
        }else{
            return false;
        }
    }

    private String getWord(TextField head, TextField tail){
        int rowHead = getRow(head);
        int colHead = getCol(head);
        int rowTail = getRow(tail);
        int colTail = getCol(tail);

        StringBuilder result = new StringBuilder();

        boolean included = false;
        if((rowHead<rowTail) && (colHead==colTail)){
            for(int row=rowHead; row<=rowTail; row++){
                TilePane rowPane = (TilePane)this.gameBoard.getChildren().get(row);
                TextField cell = (TextField)rowPane.getChildren().get(colHead);
                if(cell==this.chosenCell){
                    included = true;
                }
                if(!cell.getText().isEmpty()){
                    result.append(cell.getText());
                }else{
                    return null;
                }
            }
            if(included){
                for(int row=rowHead; row<=rowTail; row++){
                    TilePane rowPane = (TilePane)this.gameBoard.getChildren().get(row);
                    TextField cell = (TextField)rowPane.getChildren().get(colHead);
                    cell.setStyle("-fx-background-color: #d71f5a;");
                }
                return result.toString();
            }else{
                return null;
            }
        }else if((colHead<colTail) && (rowHead==rowTail)){
            TilePane rowPane = (TilePane)this.gameBoard.getChildren().get(rowHead);
            for(int col=colHead; col<=colTail; col++){
                TextField cell = (TextField)rowPane.getChildren().get(col);
                if(cell==this.chosenCell){
                    included = true;
                }
                if(!cell.getText().isEmpty()){
                    result.append(cell.getText());
                }else{
                    return null;
                }
            }
            if(included){
                for(int col=colHead; col<=colTail; col++){
                    TextField cell = (TextField)rowPane.getChildren().get(col);
                    cell.setStyle("-fx-background-color: #d71f5a;");
                }
                return result.toString();
            }else{
                return null;
            }
        }else{
            return null;
        }

    }

    private int getRow(TextField cell){
        String[] strs = cell.getId().split(",");  //Make "Cell#,#" to "Cell#" and "#"
        String row = strs[0].replaceAll("\\D+",""); //Get rid of "Cell" from "Cell#"
        return Integer.parseInt(row);
    }

    private int getCol(TextField cell){
        String[] strs = cell.getId().split(",");
        return Integer.parseInt(strs[1]);
    }

    /* ******************************************State Controls*************************************** */

    public void enterStatePlace(){

        for(Node rowNode: this.gameBoard.getChildren()){
            TilePane row = (TilePane)rowNode;
            for(Node cellNode : row.getChildren()){
                TextField cell = (TextField)cellNode;
                cell.setStyle("-fx-border-color : #68a429;");
            }
        }
        this.btnPass.setDisable(true);
        this.state = "place";
        this.stateLabel.setText("place");
    }

    private void enterStateChoose(){
        this.btnSelected.setSelected(false);

        for(TextField NonOccupiedCell : this.NonOccupiedCells) {
            NonOccupiedCell.setDisable(true);
        }
        for(TextField occupiedCell : this.occupiedCells){
            occupiedCell.setDisable(false);
        }
        for(ToggleButton letterBtn : this.letterBtns){
            letterBtn.setDisable(true);
        }

        for(Node rowNode: this.gameBoard.getChildren()){
            TilePane row = (TilePane)rowNode;
            for(Node cellNode : row.getChildren()){
                TextField cell = (TextField)cellNode;
                if(cell==this.chosenCell){
                    cell.setStyle("-fx-background-color: #68a429;");
                }else{
                    cell.setStyle("");
                }
            }
        }

        this.btnNoWord.setDisable(false);

        this.state = "choose";
        this.stateLabel.setText("choose head");
    }

    public void enterStateWait(){

        this.state = "wait";
        this.stateLabel.setText("wait");
    }


    private void enterStateNotMyTurn(){
        for(Node rowNode: this.gameBoard.getChildren()){
            TilePane row = (TilePane)rowNode;
            for(Node cellNode : row.getChildren()){
                TextField cell = (TextField)cellNode;
                cell.setDisable(true);
                cell.setStyle("");
            }
        }
        for(ToggleButton letterBtn : this.letterBtns){
            letterBtn.setDisable(true);
        }

        this.head = null;
        this.tail = null;
        this.chosenCell = null;
        this.btnNoWord.setDisable(true);
        this.btnVote.setDisable(true);
        this.btnSelected = null;
        this.btnPass.setDisable(true);
        this.state = "NotMyTurn";
        this.stateLabel.setText("NotMyTurn");
    }

    private void enterStateSelect(){
        Platform.runLater(()->{
            for(TextField occupiedCell : this.occupiedCells) {
                occupiedCell.setDisable(true);
                occupiedCell.setStyle("");
            }
            for(TextField NonOccupiedCell : this.NonOccupiedCells) {
                NonOccupiedCell.setDisable(false);
                NonOccupiedCell.setStyle("");
            }
            for(ToggleButton letterBtn : this.letterBtns){
                letterBtn.setDisable(false);
            }


            this.head = null;
            this.tail = null;
            this.chosenCell = null;
            this.btnNoWord.setDisable(true);
            this.btnVote.setDisable(true);
            this.btnSelected = null;
            this.btnPass.setDisable(false);
            this.state = "select";
            this.stateLabel.setText("select");
        });
    }
    /* ********************************************************************************************** */

    public void voteResponse(boolean correct){
        if(correct){

        }else{

        }
        this.enterStateNotMyTurn();
    }

    @FXML
    public void leaveBtnClick(ActionEvent event){
        this.clientObj.leaveRoom();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources){
        /* Initialize the association of columns in TableView elements */
        username.setCellValueFactory(new PropertyValueFactory<>("username"));
        score.setCellValueFactory(new PropertyValueFactory<>("score"));
    }

    public void startup(){
        HashMap<String, ArrayList<Integer>> roomPlayerInfoMap = clientObj.getRoomInfoMap();  // copy reference from client.obj
        for(String player : roomPlayerInfoMap.keySet()){
            this.scoreList.getItems().add(new ScoreModel(player, Integer.toString(0)));
        }

        this.NonOccupiedCells = new ArrayList<>(400);
        this.occupiedCells = new ArrayList<>(400);
        this.letterBtns = new ArrayList<>(26);
        roomNumber.setText(Integer.toString(this.clientObj.getRoomNumber()));

        this.setUpGameBoard();
        this.setUpLetterBoard();
        if(this.clientObj.isMyTurn()){
            this.enterStateSelect();
        }else{
            this.enterStateNotMyTurn();
        }
        turnLabel.setText(clientObj.getCurrTurnPlayer());
    }

    private void setUpGameBoard(){
        for(int i=0; i<20; i++){
            TilePane row = new TilePane();
            row.setId("Row" + i);
            row.setPrefColumns(20);
            for(int j=0; j<20; j++){
                TextField cell = new TextField();
                cell.setId("Cell" + i + "," + j);
                cell.setEditable(false);
                cell.setMinSize(20,20);
                cell.setPrefSize(20,20);
                cell.setMaxSize(20,20);
                cell.setFont(Font.font("System", FontWeight.BOLD, 8));

                cell.setOnMouseClicked(event -> {
                    if(state.equals("select")){

                    }else if(state.equals("place")){
                        TextField cell1 = (TextField)event.getSource();
                        confirmPlacement(cell1);
                    }else if(state.equals("choose")){
                        if(head == null){
                            head = (TextField)event.getSource();
                            head.setStyle("-fx-background-color: #d71f5a;");
                            stateLabel.setText("choose tail");
                        }else if(tail == null){
                            tail = (TextField)event.getSource();
                            chosenWord = getWord(head, tail);
                            if(chosenWord==null) {
                                dialogPane.getChildren().clear();
                                JFXDialogLayout dialogContent = new JFXDialogLayout();
                                dialogContent.setHeading(new javafx.scene.text.Text("Invalid Choice:"));
                                dialogContent.setBody(new javafx.scene.text.Text("You must choose from left \nto right, or from top to "
                                        + "bottm, \nwithout any spaces in between.And \n" +
                                        "your newly inserted letter must be included"));
                                JFXDialog dialog = new JFXDialog(dialogPane, dialogContent, JFXDialog.DialogTransition.CENTER);
                                dialog.setOverlayClose(false);
                                Button btnClose = new Button("Okay");
                                btnClose.setOnAction(new EventHandler<ActionEvent>() {
                                    @Override
                                    public void handle(ActionEvent event) {
                                        head.setStyle("");
                                        tail.setStyle("");
                                        head = null;
                                        tail = null;
                                        chosenCell.setStyle("-fx-background-color: #68a429;");
                                        btnVote.setDisable(true);
                                        dialog.close();
                                        dialogPane.setVisible(false);
                                    }
                                });
                                dialogContent.setActions(btnClose);
                                dialogPane.setVisible(true);
                                dialog.show();
                            }else {
                                btnVote.setDisable(false);
                            }
                        }else {

                        }
                    }
                });

                cell.setOnMouseEntered(event -> cell.setStyle(cell.getStyle() + "-fx-border-color : #d71f5a;"));

                cell.setOnMouseExited(event -> {
                    if(state.equals("select")){
                        cell.setStyle("");
                    }else if(state.equals("place")){
                        cell.setStyle("-fx-border-color : #68a429;");
                    }else if(state.equals("choose")){
                        cell.setStyle(cell.getStyle()+"-fx-border-color : none;");
                    }
                });

                row.getChildren().add(cell);
                NonOccupiedCells.add(cell);
            }
            this.gameBoard.getChildren().add(row);
        }
    }

    private void setUpLetterBoard(){
        for(int i=0; i<26; i++){
            ToggleButton letterBtn = new ToggleButton();
            letterBtn.setId("btnLetter" + Character.toString((char)('A'+i)));
            letterBtn.setText(Character.toString((char)('A'+i)));
            letterBtn.setPrefSize(36, 36);

            letterBtn.setOnMouseClicked(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    if(state.equals("select")){
                        btnSelected = (ToggleButton)event.getSource();
                        System.out.println("Btn: " + btnSelected.getText());
                        enterStatePlace();
                    }else if(state.equals("place")){
                        btnSelected.setSelected(false);
                        if(btnSelected==(ToggleButton)event.getSource()){
                            btnSelected=null;
                            enterStateSelect();
                        }else{
                            btnSelected = (ToggleButton)event.getSource();
                            System.out.println("Btn: " + btnSelected.getText());
                        }
                    }else if(state.equals("choose")){

                    }
                }
            });
            this.letterBoard.getChildren().add(letterBtn);
            this.letterBtns.add(letterBtn);
        }
    }

    /**
     * Create a dialog to confirm letter placement. Players are not allowed to change placement afterwards.
     */
    private void confirmPlacement(TextField cell){
        dialogPane.getChildren().clear();
        JFXDialogLayout dialogContent = new JFXDialogLayout();
        dialogContent.setHeading(new javafx.scene.text.Text("Please Confirm Placement:"));
        dialogContent.setBody(new javafx.scene.text.Text("Are you sure to place letter \'" + btnSelected.getText() + "\' ?"));
        JFXDialog dialog = new JFXDialog(dialogPane, dialogContent, JFXDialog.DialogTransition.CENTER);
        dialog.setOverlayClose(false);
        Button btnYes = new Button("Yes");
        Button btnNo = new Button("No");
        btnYes.setOnAction(event -> {
            dialog.close();
            dialogPane.setVisible(false);
            cell.setText(btnSelected.getText());

            occupiedCells.add(cell);
            NonOccupiedCells.remove(cell);
            chosenCell = cell;
            if(NonOccupiedCells.isEmpty()){
                this.clientObj.sendPlacedLetter(getRow(chosenCell), getCol(chosenCell), btnSelected.getText());
            }else{
                this.clientObj.sendPlacedLetter(getRow(chosenCell), getCol(chosenCell), btnSelected.getText());
            }


            enterStateChoose();
        });
        btnNo.setOnAction(event -> {
            dialog.close();
            dialogPane.setVisible(false);
            btnSelected.setSelected(false);
            enterStateSelect();

        });
        dialogContent.setActions(btnYes, btnNo);
        dialogPane.setVisible(true);
        dialog.show();
    }

    @FXML
    public void voteMsg(String word) {
        Platform.runLater(() -> {
            dialogPane.getChildren().clear();
            JFXDialogLayout dialogContent = new JFXDialogLayout();
            dialogContent.setHeading(new Text("Vote"));
            dialogContent.setBody(new Text("Do you think " + word + " is a word?"));
            JFXDialog dialog = new JFXDialog(dialogPane, dialogContent, JFXDialog.DialogTransition.CENTER);
            dialog.setOverlayClose(false);
            Button btnYes = new Button("Yes");
            btnYes.setOnAction(event -> {
                dialog.close();
                dialogPane.setVisible(false);
                clientObj.yesVote(word);
            });

            Button btnNo = new Button("No");
            btnNo.setOnAction(event -> {
                dialog.close();
                dialogPane.setVisible(false);
                clientObj.noVote();
            });

            dialogContent.setActions(btnYes, btnNo);
            dialogPane.setVisible(true);
            dialog.show();
        });
    }


    /* This method is used to provide a smoother transition between scences */
    public void fadeOut(String scene){
        FadeTransition fadeTransition = new FadeTransition();
        fadeTransition.setDuration(Duration.millis(500));
        fadeTransition.setNode(rootPane);
        fadeTransition.setFromValue(1);
        fadeTransition.setToValue(0);

        switch(scene) {
            case "Room":
                fadeTransition.setOnFinished(event -> loadRoomScene());
                break;
            case "Menu":
                fadeTransition.setOnFinished(event -> loadMenuScene());
                break;
        }

        fadeTransition.play();
    }

    private void loadMainScene(){
        try{
            FXMLLoader loader = new FXMLLoader(getClass().getResource("Login.fxml"));
            Parent mainView = loader.load();
            Scene mainScene = new Scene(mainView);
            Stage currentStage = (Stage) rootPane.getScene().getWindow();
            currentStage.setScene(mainScene);

        }catch(IOException e){
            System.out.println("Cannot find main scene fxml");
        }
    }

    public void setClientObj(GameClient clientObj){
        this.clientObj = clientObj;
    }

    /**
     * Display voting result
     */
    public void voteResultMsg(boolean isWord, int score){
        Platform.runLater(() -> {
            dialogPane.getChildren().clear();
            JFXDialogLayout dialogContent = new JFXDialogLayout();
            dialogContent.setHeading(new Text("Vote result"));
            if (isWord) {
                dialogContent.setBody(new Text("Vote PASSED!!"));

            } else {
                dialogContent.setBody(new Text("Vote FAILED!!"));
            }
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
        });
    }

    /**
     * Display message when the player either pass or can't select a word
     */
    public void passMsg(boolean wordSelected){
        Platform.runLater(()->{
            dialogPane.getChildren().clear();
            JFXDialogLayout dialogContent = new JFXDialogLayout();
            dialogContent.setHeading(new Text("Vote result"));
            if(wordSelected){
                dialogContent.setBody(new Text("The player can't find a word"));
            }else{
                dialogContent.setBody(new Text("The player chooses to pass"));
            }
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
        });
    }

    public void renderNext(){
        Platform.runLater(()->{
            if(clientObj.isMyTurn()){
                this.enterStateSelect();
            }else{
                this.enterStateNotMyTurn();
            }
            turnLabel.setText(clientObj.getCurrTurnPlayer());
        });
    }

    public void updateScore(String username, int score){
        for(ScoreModel userScore : scoreList.getItems()){
            if(userScore.getUsername().equals(username)){
                userScore.setScore(Integer.toString(score));
                break;
            }
        }
        scoreList.refresh();
    }

    /**
     * Display the end-game result dialog
     */
    public void renderResultPage(String username, boolean dismissed){
        Platform.runLater(() -> {
            dialogPane.getChildren().clear();
            JFXDialogLayout dialogContent = new JFXDialogLayout();
            if(username == null && !this.NonOccupiedCells.isEmpty()){
                dialogContent.setHeading(new Text("Game Over! Everybody has chosen to pass!"));
            }else if(username == null){
                dialogContent.setHeading(new Text("Game Over! No empty cell left!"));
            }else if(!dismissed){
                dialogContent.setHeading(new Text("Game Over! " + username + " left the room!"));
            }else{
                dialogContent.setHeading(new Text("Game Over! The host left the room!"));
            }

            ArrayList<String> winners = new ArrayList<>();
            int highScore = 0;
            int myScore = 0;

            for (ScoreModel playerScore : scoreList.getItems()) {
                int score = Integer.parseInt(playerScore.getScore());

                if (playerScore.getUsername().equals(clientObj.getUsername())) {
                    myScore = score;
                }

                if (score > highScore) {
                    highScore = score;
                    winners.clear();
                    winners.add(playerScore.getUsername());
                } else if (score == highScore) {
                    winners.add(playerScore.getUsername());
                }
            }
            StringBuilder msg = new StringBuilder();
            msg.append("Highest score is " + highScore + "\n");

            boolean iAmWinner = false;

            if (winners.size() == 1) {
                String winner = winners.get(0);
                msg.append("The Winner is: " + winner);
                if (winner.equals(clientObj.getUsername())) {
                    iAmWinner = true;
                }
            } else {
                msg.append("The Winners are: " + "\n");
                for (String winner : winners) {
                    msg.append(winner + "\n");
                    if (winner.equals(clientObj.getUsername())) {
                        iAmWinner = true;
                    }
                }
            }

            if (iAmWinner) {
                msg.append("\n" + "You are the winner!!!");
            } else {
                msg.append("\nYour score is: " + myScore);
            }

            dialogContent.setBody(new Text(msg.toString()));

            JFXDialog dialog = new JFXDialog(dialogPane, dialogContent, JFXDialog.DialogTransition.CENTER);
            dialog.setOverlayClose(false);
            Button btnClose = new Button("Okay");
            if(!dismissed) {
                System.out.println("111");
                btnClose.setOnAction(event -> {
                    dialog.close();
                    dialogPane.setVisible(false);
                    roomFadeOut();
                });
            }else{
                System.out.println("222");
                btnClose.setOnAction(event -> {
                    dialog.close();
                    dialogPane.setVisible(false);
                    fadeOut("Menu");
                });
            }

            dialogContent.setActions(btnClose);
            dialogPane.setVisible(true);
            dialog.show();
        });
    }

    /**
     * The fading-out animation for changing to room scene
     */
    private void roomFadeOut(){

        // update player list before fadeout
        clientObj.updateRoomInfoMap();

        FadeTransition fadeTransition = new FadeTransition();
        fadeTransition.setDuration(Duration.millis(500));
        fadeTransition.setNode(rootPane);
        fadeTransition.setFromValue(1);
        fadeTransition.setToValue(0);

        fadeTransition.setOnFinished(event -> loadRoomScene());
        fadeTransition.play();
    }

    /**
     * Load the room scene after game successfully ended.
     */
    private void loadRoomScene() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("Room.fxml"));
            Parent roomView = loader.load();
            Scene roomScene = new Scene(roomView);
            RoomController controller = loader.getController();
            controller.setClientObj(this.clientObj);
            this.clientObj.setRoomController(controller);
            this.clientObj.removeGameController();
            controller.startup(this.clientObj.isHost(), clientObj.getRoomInfoMap());
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
            /* Need to do a pop-up dialog instead of printing in terminal here! */
            System.out.println("Cannot find room scene fxml");
        }
    }

    /**
     * Load the menu scene after game being interrupted(e.g. when somebody left the room).
     */
    private void loadMenuScene() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("Menu.fxml"));
            Parent menuView = loader.load();
            Scene menuScene = new Scene(menuView);
            MenuController controller = loader.getController();
            controller.setClientObj(this.clientObj);
            this.clientObj.setMenuController(controller);
            this.clientObj.removeGameController();
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

    /**
     * highlight a placed letter in the specified cell
     */
    public void renderPlacedLetter(int insertedRow, int insertedCol, String insertedLetter){
        Platform.runLater(()->{
            TilePane row = (TilePane)this.gameBoard.getChildren().get(insertedRow);
            TextField cell = (TextField)row.getChildren().get(insertedCol);
            cell.setText(insertedLetter);
            cell.setDisable(true);
            cell.setStyle("-fx-background-color : #68a429;");
            this.occupiedCells.add(cell);
            this.NonOccupiedCells.remove(cell);
        });

    }

    /**
     * highlight a chosen word
     */
    public void highlightChosenWord(int startRow, int startCol, int length, boolean horizontal){
        if(horizontal){
            TilePane row = (TilePane)this.gameBoard.getChildren().get(startRow);
            for(int i=startCol; i<startCol+length; i++){
                TextField cell = (TextField)row.getChildren().get(i);
                cell.setStyle("-fx-background-color : #fcdb62;");
            }
        }else{
            for(int i=startRow; i<startRow+length; i++){
                TilePane row = (TilePane)this.gameBoard.getChildren().get(i);
                TextField cell = (TextField)row.getChildren().get(startCol);
                cell.setStyle("-fx-border-color : #fcdb62;");
            }
        }
    }

    /**
     *
     */
    public boolean fullCells(){
        return this.NonOccupiedCells.isEmpty();
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
}
