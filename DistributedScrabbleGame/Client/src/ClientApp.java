/*
    COMP90015 Project 2 ClientApp.java
    Group Name: Distributed Otaku
    Tutor: Alisha Aneja
 */

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;



/* An application class to initiate a JavaFX program for Server*/
public class ClientApp extends Application{

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("Login.fxml"));
        Scene scene = new Scene(root, 600,400);

        primaryStage.setTitle("DS Scrabble");
        primaryStage.setResizable(false);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

}