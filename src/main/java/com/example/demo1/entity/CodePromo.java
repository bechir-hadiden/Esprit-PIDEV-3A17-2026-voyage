package com.example.demo1.entity;

import java.sql.Date;

public class CodePromo {
    private int id_code;
    private String code_texte;
    private Date date_expiration;
    private int id_offre;

    // Constructeurs
    public CodePromo() {}

    public CodePromo(int id_code, String code_texte, Date date_expiration, int id_offre) {
        this.id_code = id_code;
        this.code_texte = code_texte;
        this.date_expiration = date_expiration;
        this.id_offre = id_offre;
    }

    // Constructeur sans ID (pour l'ajout)
    public CodePromo(String code_texte, Date date_expiration, int id_offre) {
        this.code_texte = code_texte;
        this.date_expiration = date_expiration;
        this.id_offre = id_offre;
    }

    // Getters et Setters
    public int getId_code() { return id_code; }
    public void setId_code(int id_code) { this.id_code = id_code; }
    public String getCode_texte() { return code_texte; }
    public void setCode_texte(String code_texte) { this.code_texte = code_texte; }
    public Date getDate_expiration() { return date_expiration; }
    public void setDate_expiration(Date date_expiration) { this.date_expiration = date_expiration; }
    public int getId_offre() { return id_offre; }
    public void setId_offre(int id_offre) { this.id_offre = id_offre; }
}
