module com.example.demo1 {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;
    requires javafx.graphics;
    requires java.sql;
    requires java.net.http; // <-- pour HttpClient
    requires com.google.gson;  // ← Ajoute cette ligne

    opens com.example.demo1.entity to javafx.base; // Pour les TableView
    requires okhttp3;              // ← Ajoute cette ligne

    requires java.mail;
    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
//    requires org.kordamp.ikonli.javafx;
//    requires org.kordamp.bootstrapfx.core;
//    requires eu.hansolo.tilesfx;
//    requires com.almasb.fxgl.all;

    opens com.example.demo1 to javafx.fxml;
    opens com.example.demo1.controller to javafx.fxml;
//    opens com.example.demo1.service to javafx.fxml; // si tu veux accéder aux services via FXML

    exports com.example.demo1;
}