package com.example.demo1;

import com.example.demo1.entity.Voyage;
import com.example.demo1.services.VoyageServices;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.time.LocalDate;

public class HelloController {
    @FXML
    private Label welcomeText;

    @FXML
    private TableView<Voyage> tableView;

    @FXML
    private TableColumn<Voyage, Integer> colId;

    @FXML
    private TableColumn<Voyage, String> colDestination;

    @FXML
    private TableColumn<Voyage, LocalDate> colDateDebut;

    @FXML
    private TableColumn<Voyage, LocalDate> colDateFin;

    @FXML
    private TableColumn<Voyage, Double> colPrix;

    @FXML
    private TableColumn<Voyage, String> colDescription;

    private VoyageServices voyageService;

    @FXML
    public void initialize() {
        voyageService = new VoyageServices();

        // Configurer les colonnes du TableView
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colDestination.setCellValueFactory(new PropertyValueFactory<>("destination"));
        colDateDebut.setCellValueFactory(new PropertyValueFactory<>("dateDebut"));
        colDateFin.setCellValueFactory(new PropertyValueFactory<>("dateFin"));
        colPrix.setCellValueFactory(new PropertyValueFactory<>("prix"));
        colDescription.setCellValueFactory(new PropertyValueFactory<>("description"));

        // Charger les données
        loadVoyages();
    }

    private void loadVoyages() {
        tableView.getItems().clear();
        tableView.getItems().addAll(voyageService.getAllVoyages());
    }

    @FXML
    protected void onHelloButtonClick() {
        welcomeText.setText("Bienvenue dans l'agence de voyage!");
    }

    @FXML
    protected void onRefreshButtonClick() {
        loadVoyages();
        welcomeText.setText("Données rafraîchies!");
    }
}