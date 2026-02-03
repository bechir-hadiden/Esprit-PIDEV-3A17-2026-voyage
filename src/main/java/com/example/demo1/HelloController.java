package com.example.demo1;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import com.example.demo1.entite.Voyage ;
public class HelloController {
    @FXML
    private Label welcomeText;

    @FXML
    protected void onHelloButtonClick() {
        welcomeText.setText("Welcome to JavaFX Application!");
    }

    @FXML
    private TableView<Voyage> tableVoyages;

    @FXML
    private TableColumn<Voyage, String> colDestination;

    @FXML
    private TableColumn<Voyage, String> colDateDepart;

    @FXML
    private TableColumn<Voyage, Integer> colDuree;

    @FXML
    private TableColumn<Voyage, Double> colPrix;

    @FXML
    private TextField tfDestination;

    @FXML
    private TextField tfDateDepart;

    @FXML
    private TextField tfDuree;

    @FXML
    private TextField tfPrix;

    private final ObservableList<Voyage> voyages = FXCollections.observableArrayList();


    @FXML
    public void initialize() {
        colDestination.setCellValueFactory(cellData -> cellData.getValue().destinationProperty());
        colDateDepart.setCellValueFactory(cellData -> cellData.getValue().dateDepartProperty());
        colDuree.setCellValueFactory(cellData -> cellData.getValue().dureeProperty().asObject());
        colPrix.setCellValueFactory(cellData -> cellData.getValue().prixProperty().asObject());

        tableVoyages.setItems(voyages);
    }

    @FXML
    private void ajouterVoyage() {
        Voyage v = new Voyage(
                tfDestination.getText(),
                tfDateDepart.getText(),
                Integer.parseInt(tfDuree.getText()),
                Double.parseDouble(tfPrix.getText())
        );
        voyages.add(v);
        tfDestination.clear();
        tfDateDepart.clear();
        tfDuree.clear();
        tfPrix.clear();
    }

    @FXML
    private void supprimerVoyage() {
        Voyage selected = tableVoyages.getSelectionModel().getSelectedItem();
        if (selected != null) {
            voyages.remove(selected);
        }
    }
}