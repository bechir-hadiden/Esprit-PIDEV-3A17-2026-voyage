package com.example.demo1.controller;

import javafx.fxml.FXML;

public class HomeAccueilController {

    @FXML
    public void initialize() {
        System.out.println("✅ Page d'accueil chargée");
    }

    @FXML
    public void navigateToVoyages() {
        System.out.println("Navigation vers Voyages");
        // Cette méthode sera appelée par le parent HomeController
    }

    @FXML
    public void showContact() {
        System.out.println("Affichage du formulaire de contact");
        // TODO: Implémenter le formulaire de contact
    }
}