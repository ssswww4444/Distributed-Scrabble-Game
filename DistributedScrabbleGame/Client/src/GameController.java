/*
    COMP90015 Project 1 ClientAppStartController.java
    Student Name: Haowen Tang
    Student Number: 706892
    Tutor: Alisha Aneja
 */

import com.jfoenix.controls.JFXDialog;
import com.jfoenix.controls.JFXDialogLayout;
import javafx.animation.FadeTransition;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.TilePane;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.util.Duration;

import javax.swing.*;
import javax.xml.soap.Text;
import java.io.IOException;
import java.lang.reflect.Array;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

/* Controller class for the start scene of the Client App */
public class GameController implements Initializable {

    @FXML
    private AnchorPane rootPane;
    /* UI elements */
    @FXML
    private Button btnVote;
    @FXML
    private Button btnNoWord;

    @FXML
    private Label roomNumber;

    @FXML
    private Label stateLabel;

    private ToggleButton btnSelected;

    private String state;

    private TextField head;

    private TextField tail;

    private GameClient clientObj;

    private String chosenWord;
    @FXML
    private TilePane gameBoard;
    @FXML
    private TilePane letterBoard;

    @FXML
    private Button btnPass;

    @FXML
    private StackPane dialogPane;

    private ArrayList<TextField> occupiedCells;

    private ArrayList<TextField> NonOccupiedCells;

    private TextField chosenCell;

    private ArrayList<ToggleButton> letterBtns;


    @FXML
    private void passBtnClick(ActionEvent event){
        this.clientObj.pass();
        this.btnPass.setDisable(true);
        this.enterStateNotMyTurn();
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
        this.clientObj.sendVoteRequest(this.chosenWord);
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
        this.state = "NotMyTurn";
        this.stateLabel.setText("NotMyTurn");

        try {
            Thread.sleep(3000);
        }catch(Exception e){

        }

        this.enterStateSelect();
    }

    private void enterStateSelect(){
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
    }
    /* ********************************************************************************************** */

    public void voteResponse(boolean correct){
        if(correct){

        }else{

        }
        this.enterStateNotMyTurn();
    }

    @FXML
    public void connectBtnClick(ActionEvent event){
        fadeOut();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources){

    }

    public void startup(){
        this.NonOccupiedCells = new ArrayList<>(400);
        this.occupiedCells = new ArrayList<>(400);
        this.letterBtns = new ArrayList<>(26);
        roomNumber.setText(Integer.toString(this.clientObj.getRoomNumber()));

        this.setUpGameBoard();
        this.setUpLetterBoard();
        this.state = "select";
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

                cell.setOnMouseClicked(new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent event) {
                        if(state.equals("select")){

                        }else if(state.equals("place")){
                            TextField cell = (TextField)event.getSource();
                            confirmPlacement(cell);
                        }else if(state.equals("choose")){
                            if(head == null){
                                head = (TextField)event.getSource();
                                head.setStyle("-fx-background-color: #d71f5a;");
                                stateLabel.setText("choose tail");
                            }else if(tail == null){
                                tail = (TextField)event.getSource();
                                chosenWord = getWord(head, tail);
                                if(chosenWord==null) {
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
                    }
                });

                cell.setOnMouseEntered(new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent event) {
                        cell.setStyle(cell.getStyle() + "-fx-border-color : #d71f5a;");
                    }
                });

                cell.setOnMouseExited(new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent event) {
                        if(state.equals("select")){
                            cell.setStyle("");
                        }else if(state.equals("place")){
                            cell.setStyle("-fx-border-color : #68a429;");
                        }else if(state.equals("choose")){

                        }
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
                        btnSelected = (ToggleButton)event.getSource();
                        System.out.println("Btn: " + btnSelected.getText());
                    }else if(state.equals("choose")){

                    }
                }
            });
            this.letterBoard.getChildren().add(letterBtn);
            this.letterBtns.add(letterBtn);
        }
    }

    private void confirmPlacement(TextField cell){
        JFXDialogLayout dialogContent = new JFXDialogLayout();
        dialogContent.setHeading(new javafx.scene.text.Text("Please Confirm Placement:"));
        dialogContent.setBody(new javafx.scene.text.Text("Are you sure to place letter \'" + btnSelected.getText() + "\' ?"));
        JFXDialog dialog = new JFXDialog(dialogPane, dialogContent, JFXDialog.DialogTransition.CENTER);
        dialog.setOverlayClose(false);
        Button btnYes = new Button("Yes");
        Button btnNo = new Button("No");
        btnYes.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                dialog.close();
                dialogPane.setVisible(false);
                cell.setText(btnSelected.getText());

                occupiedCells.add(cell);
                NonOccupiedCells.remove(cell);
                chosenCell = cell;

                enterStateChoose();
            }
        });
        btnNo.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                dialog.close();
                dialogPane.setVisible(false);
                btnSelected.setSelected(false);
                enterStateSelect();

            }
        });
        dialogContent.setActions(btnYes, btnNo);
        dialogPane.setVisible(true);
        dialog.show();
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
}
