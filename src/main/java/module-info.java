module org.example.demo10 {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires java.mail;        // ✅ AJOUTÉ POUR JAVAMAIL
    requires java.desktop;     // ✅ AJOUTÉ POUR JAVAMAIL

    opens org.example.demo10 to javafx.fxml;
    opens org.example.demo10.controller to javafx.fxml;
    opens org.example.demo10.model to javafx.fxml;
    opens org.example.demo10.dao to javafx.fxml;
    opens org.example.demo10.service to javafx.fxml;
    opens org.example.demo10.util to javafx.fxml;  // ✅ AJOUTÉ

    exports org.example.demo10;
    exports org.example.demo10.controller;
    exports org.example.demo10.model;
    exports org.example.demo10.dao;
    exports org.example.demo10.service;
    exports org.example.demo10.util;  // ✅ AJOUTÉ
}