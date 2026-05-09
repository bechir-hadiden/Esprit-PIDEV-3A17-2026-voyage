package com.example.demo1;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import com.example.demo1.services.SessionManager;
import java.io.IOException;
import java.net.URI;
import javafx.scene.Parent;
import java.awt.Desktop;
public class HelloApplication extends Application {

    private static HelloApplication instance;
    private static Stage primaryStage;

    @Override
    public void start(Stage stage) throws IOException {
        instance = this;
        primaryStage = stage;

        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/fxml/home.fxml")
        );

        Scene scene = new Scene(loader.load(), 1200, 800);

        stage.setTitle("Voyages Excellence - Agence de Voyage");
        stage.setScene(scene);
        stage.setMaximized(true);
        stage.show();
    }

    public static boolean openExternalUrl(String url) {
        if (url == null || url.isBlank()) {
            return false;
        }

        try {
            if (instance != null) {
                instance.getHostServices().showDocument(url);
                return true;
            }
        } catch (Exception ignored) {
        }

        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(new URI(url));
                return true;
            }
        } catch (Exception ignored) {
        }

        return false;
    }

    public static void showView(SessionManager.View view) {
        try {
            // Update session view state first
            SessionManager.getInstance().setCurrentView(view);

            String fxmlFile = getFxmlFile(view);
            FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource(fxmlFile));
            Parent root = loader.load();

            Scene scene = primaryStage.getScene();
            if (scene == null) {
                scene = new Scene(root);
                scene.getStylesheets()
                        .add(HelloApplication.class.getResource("/com/smarttrip/css/main.css").toExternalForm());
                primaryStage.setScene(scene);
            } else {
                scene.setRoot(root);
            }

            // Update stage size based on view
            if (view == SessionManager.View.SIGN_IN || view == SessionManager.View.SIGN_UP || view == SessionManager.View.FORGOT_PASSWORD) {
                primaryStage.setMaximized(false);
                primaryStage.setWidth(1200);
                primaryStage.setHeight(800);
                primaryStage.centerOnScreen();
            } else {
                primaryStage.setMaximized(true);
            }

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error loading view: " + view + " - " + e.getMessage());
        }
    }

    public static void showAdminView() {
        try {
            String fxmlFile = "/fxml/admin/AdminMain.fxml";
            FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource(fxmlFile));
            Parent root = loader.load();

            Scene scene = primaryStage.getScene();
            if (scene == null) {
                scene = new Scene(root);
                scene.getStylesheets()
                        .add(HelloApplication.class.getResource("/css/main.css").toExternalForm());
                scene.getStylesheets()
                        .add(HelloApplication.class.getResource("/css/admin-styles.css").toExternalForm());
                primaryStage.setScene(scene);
            } else {
                // Make sure admin styles are loaded
                if (!scene.getStylesheets().contains(
                        HelloApplication.class.getResource("/css/admin-styles.css").toExternalForm())) {
                    scene.getStylesheets().add(HelloApplication.class.getResource("/css/admin-styles.css").toExternalForm());
                }
                scene.setRoot(root);
            }

            primaryStage.setMaximized(true);

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error loading admin view: " + e.getMessage());
        }
    }

    private static String getFxmlFile(SessionManager.View view) {
        switch (view) {
            case HOME:
                return "/fxml/home.fxml";
            case SIGN_IN:
                return "/fxml/authentification/SignIn.fxml";
            case SIGN_UP:
                return "/fxml/authentification/SignUp.fxml";
            case FORGOT_PASSWORD:
                return "/fxml/authentification/ForgetPassword.fxml";
            case DASHBOARD:
            case MY_WALLET:
            case HOTELS:
            case HOTEL_DETAILS:
            case BOOKING:
            case TRANSPORT:
            case OFFERS:
            case TICKET_PLANS:
            case MY_BOOKINGS:
            case SETTINGS:
                return "/fxml/client/MainLayout.fxml";
            default:
                return "/fxml/authentification/SignIn.fxml";
        }
    }

    public static void main(String[] args) {
        launch();
    }
}
