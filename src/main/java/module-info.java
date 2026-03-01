module com.example.demo1 {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;
    requires javafx.graphics;
    requires jdk.jsobject;
    requires java.sql;
    requires java.desktop;
    requires java.net.http; // <-- pour HttpClient
    requires com.google.gson; // ← Ajoute cette ligne
    requires com.google.zxing;
    requires com.google.zxing.javase;

    opens com.example.demo1.entity to javafx.base, com.fasterxml.jackson.databind; // Pour TableView et Jackson
    requires okhttp3;              // ← Ajoute cette ligne
    // Bibliothèques Externes
    requires com.fasterxml.jackson.databind; // Pour JSON
    requires com.google.gson;                // Pour JSON
    requires com.google.zxing;               // Pour QR Code (nécessite v3.5.0+)
    requires com.google.zxing.javase;        // Pour QR Code JavaSE
    requires okhttp3;                        // Pour requêtes HTTP
    requires com.twilio;                     // Pour WhatsApp
    requires java.mail;                      // Pour Email

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.datatype.jsr310;
    requires jakarta.mail;
    requires itextpdf;
    // Note: BCrypt is a non-modular JAR, requires --add-reads JVM argument

//    requires org.kordamp.ikonli.javafx;
//    requires org.kordamp.bootstrapfx.core;
//    requires eu.hansolo.tilesfx;
//    requires com.almasb.fxgl.all;

    // Permissions (Opens)
    // 1. Autoriser JavaFX à voir tes contrôleurs
    opens com.example.demo1 to javafx.fxml;
    opens com.example.demo1.controller to javafx.fxml;
    opens com.example.demo1.controller.authentification to javafx.fxml;
    opens com.example.demo1.controller.client to javafx.fxml;
    opens com.example.demo1.controller.admin to javafx.fxml;
    opens org.example.controllers to javafx.fxml;
    opens org.example.entities to javafx.base;
    // opens com.example.demo1.service to javafx.fxml; // si tu veux accéder aux
    // services via FXML

    // 2. Autoriser JavaFX (TableView) ET Jackson/Gson à voir tes Entités (Voyage, Client...)
    opens com.example.demo1.entity to javafx.base, com.fasterxml.jackson.databind, com.google.gson;

    exports com.example.demo1;
    exports com.example.demo1.services;
}