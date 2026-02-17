package com.example.demo1;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class HelloApplication extends Application {

    @Override
    public void start(Stage stage) throws IOException {

        FXMLLoader loader = new FXMLLoader(
                Objects.requireNonNull(
//                        getClass().getResource("/fxml/gestion-reclamation.fxml"),
//                        "home.fxml introuvable"

                                                getClass().getResource("/fxml/home.fxml"),
                        "home.fxml introuvable"
                )
        );

        Scene scene = new Scene(loader.load(), 1200, 800);

        stage.setTitle("Voyages Excellence - Agence de Voyage");
        stage.setScene(scene);
        stage.setMaximized(true);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}