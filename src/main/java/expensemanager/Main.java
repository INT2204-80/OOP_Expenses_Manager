package expensemanager;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.net.URL;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        URL fxmlUrl = getClass().getResource("/fxml/Dashboard.fxml");
        if (fxmlUrl == null) {
            System.err.println("Cannot find /fxml/Dashboard.fxml");
            System.exit(1);
        }
        
        Parent root = FXMLLoader.load(fxmlUrl);
        primaryStage.setTitle("OOP Expenses Manager - Dashboard");
        primaryStage.setScene(new Scene(root, 1000, 700));
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}