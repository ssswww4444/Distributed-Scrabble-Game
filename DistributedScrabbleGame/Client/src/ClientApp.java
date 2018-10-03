import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;


/* Initiate a JavaFX program for Game client*/
public class ClientApp extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("Login.fxml"));
        Scene scene = new Scene(root, 600, 400);

        primaryStage.setTitle("DS Scrabble Game");
        primaryStage.setResizable(false);
        primaryStage.setScene(scene);
        primaryStage.show();

        primaryStage.setOnCloseRequest(t -> {
            System.out.println("Primary Stage is closing, process is killed! ");
            Platform.exit();
            System.exit(0);
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}