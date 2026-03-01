module com.example.demo1 {

    // ================================================
    // JAVAFX
    // ================================================
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;
    requires javafx.graphics;
    requires javafx.swing;
    requires javafx.media;
    requires jdk.jsobject;
    requires jbcrypt;

    // ================================================
    // JAVA STANDARD
    // ================================================
    requires java.sql;
    requires java.desktop;
    requires java.net.http;
    requires java.mail;
    requires itextpdf;
//    requires com.itextpdf ;
    // ================================================
    // TWILIO ✅ — module name correct pour 10.1.0
    // ================================================
//    requires twilio;
    requires transitive twilio;

    // ================================================
    // JSON & HTTP
    // ================================================
    requires com.google.gson;
    requires okhttp3;
    requires org.json;

    // ================================================
    // QR CODE
    // ================================================
    requires com.google.zxing;
    requires com.google.zxing.javase;

    // ================================================
    // JACKSON (pour CurrencyService si utilisé)
    // ================================================
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.datatype.jsr310;

    // ================================================
    // UI EXTRAS
    // ================================================
    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;

    // ================================================
    // OPENS — JavaFX FXML accès aux controllers
    // ================================================
    opens com.example.demo1 to javafx.fxml;
    opens com.example.demo1.controller to javafx.fxml;
    opens com.example.demo1.controller.authentification to javafx.fxml;
    opens com.example.demo1.controller.client to javafx.fxml;
    opens com.example.demo1.controller.admin to javafx.fxml;

    // ================================================
    // OPENS — Entités pour TableView + Jackson + Gson
    // ================================================
    opens com.example.demo1.entity to
            javafx.base,
            com.fasterxml.jackson.databind,
            com.google.gson;

    // ================================================
    // OPENS — autres packages si nécessaire
    // ================================================
    opens org.example.controllers to javafx.fxml;
    opens org.example.entities to javafx.base;

    // ================================================
    // EXPORTS
    // ================================================
    exports com.example.demo1;
    exports com.example.demo1.services;
}