package org.example.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import org.example.entities.User;
import com.example.demo1.services.AuthService;
import java.io.IOException;

/** Transport Hub Controller for Users. */
public class UserMenuController {

    private User currentUser;

    // A root node injected from FXML so we can lookup the scene
    @FXML
    private VBox hubRoot;

    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    @FXML
    public void initialize() {
        loadCurrentUser();
    }

    private void loadCurrentUser() {
        AuthService auth = AuthService.getInstance();
        if (auth.getCurrentUser() != null) {
            this.currentUser = new User();
            try {
                this.currentUser.setIdUser(Integer.parseInt(auth.getCurrentUser().getId()));
            } catch (Exception e) {
            }
            this.currentUser.setUsername(auth.getCurrentUser().getUsername());
            this.currentUser.setEmail(auth.getCurrentUser().getEmail());
            this.currentUser.setRole(auth.getCurrentUser().getRole());
            this.currentUser.setTelephone(auth.getCurrentUser().getTelephone());
        }
    }

    private Pane findContentArea() {
        if (hubRoot == null || hubRoot.getScene() == null)
            return null;
        Pane p = (Pane) hubRoot.getScene().lookup("#contentContainer");
        if (p == null)
            p = (Pane) hubRoot.getScene().lookup("#contentArea");
        return p;
    }

    @FXML
    private void showTransportTypes() {
        loadSubView("/fxml/transport_type_selector.fxml");
    }

    @FXML
    private void showMyReservations() {
        loadSubView("/fxml/my_reservations.fxml");
    }

    @FXML
    private void showAIGuide() {
        loadSubView("/fxml/ai_guide.fxml");
    }

    private void loadSubView(String fxmlPath) {
        try {
            Pane contentArea = findContentArea();

            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            javafx.scene.Parent root = loader.load();

            // Pass user context if the target controller supports it
            Object controller = loader.getController();
            if (controller instanceof TransportTypeSelectorController) {
                ((TransportTypeSelectorController) controller).setCurrentUser(currentUser);
            } else if (controller instanceof MyReservationsController) {
                ((MyReservationsController) controller).setCurrentUser(currentUser);
            } else if (controller instanceof AIGuideController) {
                ((AIGuideController) controller).setCurrentUser(currentUser);
            }

            if (contentArea != null) {
                contentArea.getChildren().setAll(root);
            } else {
                // Fallback: Shell for standalone mode
                MainShellController shell = MainShellController.getInstance();
                if (shell != null) {
                    shell.getContentArea().getChildren().setAll(root);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
