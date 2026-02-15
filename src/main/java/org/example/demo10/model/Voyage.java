package org.example.demo10.model;

import javafx.beans.property.*;
import java.time.LocalDate;

public class Voyage {
    private IntegerProperty id = new SimpleIntegerProperty();
    private StringProperty destination = new SimpleStringProperty();
    private StringProperty description = new SimpleStringProperty();
    private DoubleProperty prix = new SimpleDoubleProperty();
    private IntegerProperty duree = new SimpleIntegerProperty(); // en jours
    private StringProperty imageUrl = new SimpleStringProperty();
    private ObjectProperty<LocalDate> dateDepart = new SimpleObjectProperty<>();
    private ObjectProperty<LocalDate> dateRetour = new SimpleObjectProperty<>();
    private IntegerProperty placesDisponibles = new SimpleIntegerProperty();

    // Constructeurs
    public Voyage() {}

    public Voyage(int id, String destination, String description, double prix,
                  int duree, String imageUrl, LocalDate dateDepart,
                  LocalDate dateRetour, int placesDisponibles) {
        this.id.set(id);
        this.destination.set(destination);
        this.description.set(description);
        this.prix.set(prix);
        this.duree.set(duree);
        this.imageUrl.set(imageUrl);
        this.dateDepart.set(dateDepart);
        this.dateRetour.set(dateRetour);
        this.placesDisponibles.set(placesDisponibles);
    }

    // Getters et Setters
    public int getId() { return id.get(); }
    public void setId(int id) { this.id.set(id); }
    public IntegerProperty idProperty() { return id; }

    public String getDestination() { return destination.get(); }
    public void setDestination(String destination) { this.destination.set(destination); }
    public StringProperty destinationProperty() { return destination; }

    public String getDescription() { return description.get(); }
    public void setDescription(String description) { this.description.set(description); }
    public StringProperty descriptionProperty() { return description; }

    public double getPrix() { return prix.get(); }
    public void setPrix(double prix) { this.prix.set(prix); }
    public DoubleProperty prixProperty() { return prix; }

    public int getDuree() { return duree.get(); }
    public void setDuree(int duree) { this.duree.set(duree); }
    public IntegerProperty dureeProperty() { return duree; }

    public String getImageUrl() { return imageUrl.get(); }
    public void setImageUrl(String imageUrl) { this.imageUrl.set(imageUrl); }
    public StringProperty imageUrlProperty() { return imageUrl; }

    public LocalDate getDateDepart() { return dateDepart.get(); }
    public void setDateDepart(LocalDate dateDepart) { this.dateDepart.set(dateDepart); }
    public ObjectProperty<LocalDate> dateDepartProperty() { return dateDepart; }

    public LocalDate getDateRetour() { return dateRetour.get(); }
    public void setDateRetour(LocalDate dateRetour) { this.dateRetour.set(dateRetour); }
    public ObjectProperty<LocalDate> dateRetourProperty() { return dateRetour; }

    public int getPlacesDisponibles() { return placesDisponibles.get(); }
    public void setPlacesDisponibles(int placesDisponibles) { this.placesDisponibles.set(placesDisponibles); }
    public IntegerProperty placesDisponiblesProperty() { return placesDisponibles; }

    // Méthodes utilitaires
    public String getPrixFormate() {
        return String.format("%,.0f €", prix.get());
    }

    public String getDureeFormate() {
        return duree.get() + " jours";
    }

    public String getPeriode() {
        if (dateDepart.get() != null && dateRetour.get() != null) {
            return dateDepart.get().toString() + " → " + dateRetour.get().toString();
        }
        return "Dates non définies";
    }

    public String getStatutPlaces() {
        if (placesDisponibles.get() > 5) {
            return "✓ Disponible";
        } else if (placesDisponibles.get() > 0) {
            return "⚠️ Plus que " + placesDisponibles.get() + " places";
        } else {
            return "❌ Complet";
        }
    }

    public String getCouleurStatut() {
        if (placesDisponibles.get() > 5) return "#28a745"; // Vert
        if (placesDisponibles.get() > 0) return "#ffc107"; // Jaune
        return "#dc3545"; // Rouge
    }
}