package org.example.demo10.service;

import org.example.demo10.dao.ReservationDAO;
import org.example.demo10.model.Reservation;
import java.time.LocalDate;
import java.util.List;

public class ReservationService {
    private ReservationDAO reservationDAO;

    public ReservationService() {
        this.reservationDAO = new ReservationDAO();
    }

    // Récupérer toutes les réservations
    public List<Reservation> getAllReservations() {
        return reservationDAO.getAllReservations();
    }

    // Récupérer une réservation par ID
    public Reservation getReservationById(int id) {
        return reservationDAO.getReservationById(id);
    }

    // Ajouter une réservation (client)
    public boolean ajouterReservation(String nom, String email, String tel, int voyageId,
                                      LocalDate date, int personnes, String commentaire) {
        Reservation reservation = new Reservation();
        reservation.setNomClient(nom);
        reservation.setEmailClient(email);
        reservation.setTelephone(tel);
        reservation.setVoyageId(voyageId);
        reservation.setDateReservation(date);
        reservation.setNombrePersonnes(personnes);
        reservation.setStatut("en_attente");  // Par défaut
        reservation.setCommentaire(commentaire);

        return reservationDAO.addReservation(reservation);
    }

    // Modifier une réservation (admin)
    public boolean modifierReservation(int id, String nom, String email, String tel,
                                       int voyageId, LocalDate date, int personnes,
                                       String statut, String commentaire) {
        Reservation reservation = new Reservation(id, nom, email, tel, voyageId, date, personnes, statut, commentaire);
        return reservationDAO.updateReservation(reservation);
    }

    // Supprimer une réservation
    public boolean supprimerReservation(int id) {
        return reservationDAO.deleteReservation(id);
    }

    // Changer le statut (admin)
    public boolean changerStatut(int id, String nouveauStatut) {
        return reservationDAO.updateStatut(id, nouveauStatut);
    }

    // Récupérer les réservations d'un client
    public List<Reservation> getReservationsByClient(String email) {
        return reservationDAO.getReservationsByClient(email);
    }

    // Récupérer les réservations par statut (admin)
    public List<Reservation> getReservationsByStatut(String statut) {
        return reservationDAO.getReservationsByStatut(statut);
    }

    // Récupérer les réservations par voyage
    public List<Reservation> getReservationsByVoyage(int voyageId) {
        return reservationDAO.getReservationsByVoyage(voyageId);
    }

    // Compter le nombre de réservations par statut
    public int countReservationsByStatut(String statut) {
        return getReservationsByStatut(statut).size();
    }

    // Calculer le nombre total de personnes pour un voyage
    public int totalPersonnesParVoyage(int voyageId) {
        return getReservationsByVoyage(voyageId).stream()
                .filter(r -> !r.getStatut().equals("annulée"))
                .mapToInt(Reservation::getNombrePersonnes)
                .sum();
    }
}