package org.example.entities;

/** Entity class for Profession data. */
public class Profession {
    private int idProfession;
    private String titre;
    private String description;

    public Profession() {
    }

    public Profession(int idProfession, String titre, String description) {
        this.idProfession = idProfession;
        this.titre = titre;
        this.description = description;
    }

    public int getIdProfession() {
        return idProfession;
    }

    public void setIdProfession(int idProfession) {
        this.idProfession = idProfession;
    }

    public String getTitre() {
        return titre;
    }

    public void setTitre(String titre) {
        this.titre = titre;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return titre;
    }
}
