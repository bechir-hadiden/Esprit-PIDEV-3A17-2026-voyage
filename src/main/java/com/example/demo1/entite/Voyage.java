package com.example.demo1.entite;

import javafx.beans.property.*;

public class Voyage {   // ✅ majuscule

    private final StringProperty destination = new SimpleStringProperty();
    private final StringProperty dateDepart = new SimpleStringProperty();
    private final IntegerProperty duree = new SimpleIntegerProperty();
    private final DoubleProperty prix = new SimpleDoubleProperty();

    // ✅ Constructeur (même nom que la classe)
    public Voyage(String destination, String dateDepart, int duree, double prix) {
        this.destination.set(destination);
        this.dateDepart.set(dateDepart);
        this.duree.set(duree);
        this.prix.set(prix);
    }

    // ✅ Méthodes pour accéder aux propriétés
    public StringProperty destinationProperty() {
        return destination; }
    public StringProperty dateDepartProperty() {
        return dateDepart; }
    public IntegerProperty dureeProperty() {
        return duree; }
    public DoubleProperty prixProperty() {
        return prix; }
}
