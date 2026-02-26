module com.example.demo1 {
    requires javafx.base;
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;
    requires javafx.graphics;
    requires java.sql;
    requires java.net.http; // <-- pour HttpClient
    requires com.google.gson;  // ← Ajoute cette ligne
    requires com.google.zxing;
    requires com.google.zxing.javase;

    opens com.example.demo1.entity to javafx.base, com.fasterxml.jackson.databind; // Pour TableView et Jackson
    requires okhttp3;              // ← Ajoute cette ligne

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.datatype.jsr310;
    // Note: BCrypt is a non-modular JAR, requires --add-reads JVM argument

//    requires org.kordamp.ikonli.javafx;
//    requires org.kordamp.bootstrapfx.core;
//    requires eu.hansolo.tilesfx;
//    requires com.almasb.fxgl.all;

    opens com.example.demo1 to javafx.fxml;
    opens com.example.demo1.controller to javafx.fxml;
    opens com.example.demo1.controller.authentification to javafx.fxml;
    opens com.example.demo1.controller.client to javafx.fxml;
    opens com.example.demo1.controller.admin to javafx.fxml;
//    opens com.example.demo1.service to javafx.fxml; // si tu veux accéder aux services via FXML

    exports com.example.demo1;
    exports com.example.demo1.services;
}