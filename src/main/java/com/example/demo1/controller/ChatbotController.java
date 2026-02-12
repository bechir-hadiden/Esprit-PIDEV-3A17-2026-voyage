package com.example.demo1.controller;

import com.example.demo1.services.ChatbotService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class ChatbotController {

    @FXML
    private VBox messagesContainer;

    @FXML
    private ScrollPane scrollPane;

    @FXML
    private TextField messageInput;

    @FXML
    public void initialize() {
        // Message de bienvenue
        ajouterMessageBot("Bonjour ! 👋 Je suis votre assistant virtuel pour Voyages Excellence.\n\n" +
                "Comment puis-je vous aider aujourd'hui ?");
    }

    @FXML
    private void envoyerMessage() {
        String message = messageInput.getText().trim();

        if (message.isEmpty()) {
            return;
        }

        // Afficher le message de l'utilisateur
        ajouterMessageUtilisateur(message);
        messageInput.clear();

        // Afficher "En train d'écrire..."
        VBox indicateur = ajouterIndicateurEcriture(); // ← CHANGÉ : VBox au lieu de Label

        // Envoyer au chatbot en arrière-plan
        new Thread(() -> {
            String reponse = ChatbotService.envoyerMessage(message);

            Platform.runLater(() -> {
                // Retirer l'indicateur
                messagesContainer.getChildren().remove(indicateur);

                // Afficher la réponse
                ajouterMessageBot(reponse);
            });
        }).start();
    }

    private void ajouterMessageUtilisateur(String texte) {
        VBox messageBox = new VBox(5);
        messageBox.setAlignment(Pos.CENTER_RIGHT);
        messageBox.setMaxWidth(Double.MAX_VALUE);

        Label label = new Label(texte);
        label.setWrapText(true);
        label.getStyleClass().add("message-utilisateur");
        label.setMaxWidth(400);

        HBox container = new HBox(label);
        container.setAlignment(Pos.CENTER_RIGHT);

        messageBox.getChildren().add(container);
        messagesContainer.getChildren().add(messageBox);

        scrollToBottom();
    }

    private void ajouterMessageBot(String texte) {
        VBox messageBox = new VBox(5);
        messageBox.setAlignment(Pos.CENTER_LEFT);
        messageBox.setMaxWidth(Double.MAX_VALUE);

        Label label = new Label(texte);
        label.setWrapText(true);
        label.getStyleClass().add("message-bot");
        label.setMaxWidth(400);

        HBox container = new HBox(label);
        container.setAlignment(Pos.CENTER_LEFT);

        messageBox.getChildren().add(container);
        messagesContainer.getChildren().add(messageBox);

        scrollToBottom();
    }

    // ← MÉTHODE CORRIGÉE : Retourne VBox au lieu de Label
    private VBox ajouterIndicateurEcriture() {
        VBox messageBox = new VBox(5);
        messageBox.setAlignment(Pos.CENTER_LEFT);
        messageBox.setMaxWidth(Double.MAX_VALUE);

        Label indicateur = new Label("💭 En train d'écrire...");
        indicateur.getStyleClass().add("message-bot");
        indicateur.setStyle("-fx-font-style: italic; -fx-opacity: 0.7;");
        indicateur.setMaxWidth(400);

        HBox container = new HBox(indicateur);
        container.setAlignment(Pos.CENTER_LEFT);

        messageBox.getChildren().add(container);
        messagesContainer.getChildren().add(messageBox);
        scrollToBottom();

        return messageBox; // ← Retourne le VBox parent
    }

    private void scrollToBottom() {
        Platform.runLater(() ->
                scrollPane.setVvalue(scrollPane.getVmax())
        );
    }

    @FXML
    private void fermer() {
        Stage stage = (Stage) messageInput.getScene().getWindow();
        stage.close();
    }
}