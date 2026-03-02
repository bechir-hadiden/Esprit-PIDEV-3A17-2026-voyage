package org.example;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.example.services.ProfessionService;
import org.example.services.UserService;

import java.io.IOException;

public class TransportApp extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(TransportApp.class.getResource("/fxml/login.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1000, 600);
        stage.setTitle("Transport Application");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        new ProfessionService().seedProfessions();
        new UserService().ensureSchemaConsistency();
        launch();
    }
}
