module com.example.demo1 {
    // JavaFX
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;
    requires javafx.graphics;

    // Java Standard
    requires java.sql;
    requires java.net.http;

    // Bibliothèques Externes
    requires com.fasterxml.jackson.databind; // Pour JSON
    requires com.google.gson;                // Pour JSON
    requires com.google.zxing;               // Pour QR Code (nécessite v3.5.0+)
    requires com.google.zxing.javase;        // Pour QR Code JavaSE
    requires okhttp3;                        // Pour requêtes HTTP
    requires com.twilio;                     // Pour WhatsApp
    requires java.mail;                      // Pour Email

    // UI Libraries
    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;

    // Permissions (Opens)
    // 1. Autoriser JavaFX à voir tes contrôleurs
    opens com.example.demo1 to javafx.fxml;
    opens com.example.demo1.controller to javafx.fxml;

    // 2. Autoriser JavaFX (TableView) ET Jackson/Gson à voir tes Entités (Voyage, Client...)
    opens com.example.demo1.entity to javafx.base, com.fasterxml.jackson.databind, com.google.gson;

    exports com.example.demo1;
}