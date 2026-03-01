package org.example.entities;

/** Entity class for User data. */
public class User {
    private int idUser;
    private String username;
    private String password;
    private String role; // "USER" or "ADMIN"
    private int idProfession;
    private String telephone;
    private String email;

    public User() {
    }

    public User(int idUser, String username, String password, String role, int idProfession, String telephone,
            String email) {
        this.idUser = idUser;
        this.username = username;
        this.password = password;
        this.role = role;
        this.idProfession = idProfession;
        this.telephone = telephone;
        this.email = email;
    }

    public int getIdUser() {
        return idUser;
    }

    public void setIdUser(int idUser) {
        this.idUser = idUser;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public boolean isAdmin() {
        return "ADMIN".equals(role);
    }

    public int getIdProfession() {
        return idProfession;
    }

    public void setIdProfession(int idProfession) {
        this.idProfession = idProfession;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
