import Controller.Controller;
import Model.Hardware;
import Model.MathFunction;
import com.fazecast.jSerialComm.SerialPort;
import com.sun.javafx.tk.Toolkit;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.Arrays;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        FXMLLoader fxmlLoader = new FXMLLoader();
        Parent root = fxmlLoader.load(getClass().getResource("View/sample.fxml"));
        primaryStage.setTitle("Test chlazení hotendu");
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
        primaryStage.setOnCloseRequest(e -> {
            Runtime.getRuntime().exit(0);
        });
    }


    public static void main(String[] args) {
        launch(args);
    }
}
