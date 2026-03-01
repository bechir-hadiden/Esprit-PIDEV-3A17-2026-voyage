package org.example.entities;

import java.time.LocalDateTime;

/** Entity class for Reservation data. */
public class Reservation {
    private int idReservation;
    private int idUser;
    private int idTransport;
    private LocalDateTime dateReservation;
    private String statut;

    // Include transport details for display
    private String transportType;
    private String transportCompagnie;
    private String transportNumero;

    // New fields for 2-level architecture
    private String typeTransport; // Bus, Taxi, or Voiture
    private int idVehicule; // ID in respective table

    public Reservation() {
    }

    public Reservation(int idReservation, int idUser, int idTransport, LocalDateTime dateReservation, String statut) {
        this.idReservation = idReservation;
        this.idUser = idUser;
        this.idTransport = idTransport;
        this.dateReservation = dateReservation;
        this.statut = statut;
    }

    public int getIdReservation() {
        return idReservation;
    }

    public void setIdReservation(int idReservation) {
        this.idReservation = idReservation;
    }

    public int getIdUser() {
        return idUser;
    }

    public void setIdUser(int idUser) {
        this.idUser = idUser;
    }

    public int getIdTransport() {
        return idTransport;
    }

    public void setIdTransport(int idTransport) {
        this.idTransport = idTransport;
    }

    public LocalDateTime getDateReservation() {
        return dateReservation;
    }

    public void setDateReservation(LocalDateTime dateReservation) {
        this.dateReservation = dateReservation;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public String getTransportType() {
        return transportType;
    }

    public void setTransportType(String transportType) {
        this.transportType = transportType;
    }

    public String getTransportCompagnie() {
        return transportCompagnie;
    }

    public void setTransportCompagnie(String transportCompagnie) {
        this.transportCompagnie = transportCompagnie;
    }

    public String getTransportNumero() {
        return transportNumero;
    }

    public void setTransportNumero(String transportNumero) {
        this.transportNumero = transportNumero;
    }

    private int transportCapacite;
    private double transportPrix;

    public int getTransportCapacite() {
        return transportCapacite;
    }

    public void setTransportCapacite(int transportCapacite) {
        this.transportCapacite = transportCapacite;
    }

    public double getTransportPrix() {
        return transportPrix;
    }

    public void setTransportPrix(double transportPrix) {
        this.transportPrix = transportPrix;
    }

    public String getTypeTransport() {
        return typeTransport;
    }

    public void setTypeTransport(String typeTransport) {
        this.typeTransport = typeTransport;
    }

    public int getIdVehicule() {
        return idVehicule;
    }

    public void setIdVehicule(int idVehicule) {
        this.idVehicule = idVehicule;
    }
}
