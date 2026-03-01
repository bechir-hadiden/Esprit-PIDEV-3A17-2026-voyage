package org.example.services;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import org.example.entities.Reservation;
import org.example.entities.User;

import java.io.FileOutputStream;
import java.time.format.DateTimeFormatter;

/** Service for generating PDF documents for reservations. */
public class PdfService {

    public boolean generateReservationPdf(Reservation reservation, User user, String outputPath) {
        Document document = new Document();
        try {
            PdfWriter.getInstance(document, new FileOutputStream(outputPath));
            document.open();

            // Add Title
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20, BaseColor.BLUE);
            Paragraph title = new Paragraph("Récapitulatif de Réservation", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(30);
            document.add(title);

            // Add User Info
            Font boldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
            document.add(new Paragraph("Client: " + user.getUsername(), boldFont));
            document.add(new Paragraph("Email: " + user.getEmail()));
            document.add(new Paragraph("Téléphone: " + user.getTelephone()));
            document.add(new Chunk("\n"));

            // Add Table for Reservation Details
            PdfPTable table = new PdfPTable(2);
            table.setWidthPercentage(100);
            table.setSpacingBefore(10);
            table.setSpacingAfter(10);

            addTableCell(table, "ID Réservation", String.valueOf(reservation.getIdReservation()));
            addTableCell(table, "Type de Transport", reservation.getTransportType());
            addTableCell(table, "Compagnie", reservation.getTransportCompagnie());
            addTableCell(table, "Numéro du véhicule", reservation.getTransportNumero());
            addTableCell(table, "Capacité", String.valueOf(reservation.getTransportCapacite()));

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            addTableCell(table, "Date et Heure", reservation.getDateReservation().format(formatter));

            addTableCell(table, "Statut", reservation.getStatut());
            addTableCell(table, "Prix", String.format("%.2f DT", reservation.getTransportPrix()));

            document.add(table);

            // Add Footer
            document.add(new Chunk("\n"));
            Paragraph footer = new Paragraph("Merci d'utiliser notre service de transport !",
                    FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 10));
            footer.setAlignment(Element.ALIGN_CENTER);
            document.add(footer);

            document.close();
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private void addTableCell(PdfPTable table, String label, String value) {
        PdfPCell cell1 = new PdfPCell(new Phrase(label, FontFactory.getFont(FontFactory.HELVETICA_BOLD)));
        PdfPCell cell2 = new PdfPCell(new Phrase(value));

        cell1.setPadding(8);
        cell2.setPadding(8);

        table.addCell(cell1);
        table.addCell(cell2);
    }
}
