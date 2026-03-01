module com.example.demo1 {
    requires javafx.base;
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;
    requires javafx.graphics;
    requires java.sql;
    requires java.net.http;
    requires com.google.gson;
    requires com.google.zxing;
    requires com.google.zxing.javase;
    requires okhttp3;
    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.datatype.jsr310;
    requires java.desktop;

    // --- LIGNE À AJOUTER POUR LE PDF ---
    requires itextpdf;

    opens com.example.demo1.entity to javafx.base, com.fasterxml.jackson.databind;

    opens com.example.demo1 to javafx.fxml;
    opens com.example.demo1.controller to javafx.fxml;
    opens com.example.demo1.controller.authentification to javafx.fxml;
    opens com.example.demo1.controller.client to javafx.fxml;
    opens com.example.demo1.controller.admin to javafx.fxml;

    exports com.example.demo1;
    exports com.example.demo1.services;
}