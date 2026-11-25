package main;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/view/Login.fxml"));
        util.ResponsiveFont.bind(root);
        util.ResponsiveForm.fit(root);
        primaryStage.setScene(new Scene(root));
        primaryStage.setMinWidth(320);
        primaryStage.setMinHeight(480);
        primaryStage.setTitle("Manuel B. Guiñez System - Login");
        primaryStage.show();
    }
}
