package org.example.utils;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.draw.LineSeparator;
import org.example.entities.Paiement;
import org.example.entities.User;

import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class PDFService {

    public static void generatePaymentReceipt(Paiement p, User u, String filePath)
            throws DocumentException, IOException {
        Document document = new Document();
        PdfWriter.getInstance(document, new FileOutputStream(filePath));
        document.open();

        // Fonts
        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 22, BaseColor.DARK_GRAY);
        Font subTitleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, BaseColor.GRAY);
        Font labelFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, BaseColor.BLACK);
        Font valueFont = FontFactory.getFont(FontFactory.HELVETICA, 12, BaseColor.DARK_GRAY);
        Font footerFont = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 10, BaseColor.LIGHT_GRAY);

        // Header
        Paragraph title = new Paragraph("SMARTTRIP - REÇU DE PAIEMENT", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);

        Paragraph subTitle = new Paragraph("Confirmation officielle de transaction", subTitleFont);
        subTitle.setAlignment(Element.ALIGN_CENTER);
        subTitle.setSpacingAfter(20);
        document.add(subTitle);

        document.add(new LineSeparator());
        document.add(new Paragraph(" ")); // Spacer

        // Info Table
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setSpacingBefore(10f);
        table.setSpacingAfter(10f);

        addCell(table, "ID Paiement:", labelFont);
        addCell(table, String.valueOf(p.getIdPaiement()), valueFont);

        addCell(table, "Client:", labelFont);
        String name = (u != null && u.getName() != null) ? u.getName() : "Utilisateur #" + p.getUserId();
        addCell(table, name, valueFont);

        addCell(table, "Date:", labelFont);
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        String dateStr = p.getDatePaiement() != null ? sdf.format(p.getDatePaiement()) : sdf.format(new Date());
        addCell(table, dateStr, valueFont);

        addCell(table, "Montant Total:", labelFont);
        addCell(table, String.format("%.2f DT", p.getMontant()), valueFont);

        addCell(table, "Méthode:", labelFont);
        addCell(table, p.getMethodePaiement(), valueFont);

        addCell(table, "Statut:", labelFont);
        addCell(table, p.getStatut_paiement(), valueFont);

        if (p.getBookingId() != null) {
            addCell(table, "Réservation associée:", labelFont);
            addCell(table, "#" + p.getBookingId(), valueFont);
        }

        document.add(table);

        document.add(new Paragraph(" ")); // Spacer
        document.add(new LineSeparator());

        // Footer
        Paragraph footer = new Paragraph("Merci d'avoir choisi SmartTrip pour vos voyages.", footerFont);
        footer.setAlignment(Element.ALIGN_CENTER);
        footer.setSpacingBefore(30);
        document.add(footer);

        document.close();
    }

    private static void addCell(PdfPTable table, String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setPadding(8);
        table.addCell(cell);
    }
}
