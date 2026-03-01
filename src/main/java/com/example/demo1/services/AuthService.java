package com.example.demo1.services;
import com.example.demo1.entity.User;
import com.example.demo1.controller.dao.UserDAO;
import com.example.demo1.*;

import java.util.List;

/**
 * Authentication Service for SmartTrip application.
 * Handles user authentication using database-backed storage.
 */
public class AuthService {

    private static AuthService instance;
    private User currentUser;
    private final UserDAO userDAO;

    private AuthService() {
        this.userDAO = new UserDAO();
    }

    public static synchronized AuthService getInstance() {
        if (instance == null) {
            instance = new AuthService();
        }
        return instance;
    }

    /**
     * Login with username and password.
     * Checks if user is blocked before allowing login.
     *
     * @param username User's username
     * @param password User's plain text password
     * @return true if authentication successful and user not blocked, false otherwise
     */
    public boolean login(String username, String password) {
        User user = userDAO.authenticateUser(username, password);
        if (user != null) {
            // Check if user is blocked
            if (user.isBlocked()) {
                System.out.println("Blocked user attempted login: " + user.getUsername());
                return false; // User is blocked, cannot login
            }
            currentUser = user;
            BookingService.getInstance().loadHotelBookingsFromDb();
            System.out.println("User logged in: " + user.getUsername() + " (Role: " + user.getRole() + ")");
            return true;
        }
        return false;
    }

    /**
     * Register a new user (CLIENT role by default).
     *
     * @param fullName User's full name
     * @param email    User's email
     * @param username User's username
     * @param password User's plain text password
     * @return true if registration successful, false otherwise
     */
    public boolean register(String fullName, String email, String username, String password) {
        // Check if username already exists
        if (userDAO.getUserByUsername(username) != null) {
            return false; // Username already taken
        }

        User newUser = new User();
        newUser.setFullName(fullName);
        newUser.setEmail(email);
        newUser.setUsername(username);
        newUser.setRole("CLIENT"); // Default role
        newUser.setAvatar("https://ui-avatars.com/api/?name=" +
                fullName.replace(" ", "+") + "&background=2563EB&color=fff");

        if (userDAO.createUser(newUser, password)) {
            currentUser = newUser;
            return true;
        }

        return false;
    }

    /**
     * Logout current user.
     */
    public void logout() {
        if (currentUser != null) {
            System.out.println("User logged out: " + currentUser.getUsername());
        }
        currentUser = null;
    }

    /**
     * Get currently logged in user.
     *
     * @return Current user or null if not logged in
     */
    public User getCurrentUser() {
        return currentUser;
    }

    /**
     * Check if a user is currently logged in.
     *
     * @return true if user is logged in, false otherwise
     */
    public boolean isLoggedIn() {
        return currentUser != null;
    }

    /**
     * Check if current user is an admin.
     *
     * @return true if current user is admin, false otherwise
     */
    public boolean isAdmin() {
        return currentUser != null && currentUser.isAdmin();
    }

    /**
     * Update current user information.
     *
     * @param updatedUser Updated user object
     * @return true if update successful, false otherwise
     */
    public boolean updateUser(User updatedUser) {
        if (currentUser != null && currentUser.getId().equals(updatedUser.getId())) {
            if (userDAO.updateUser(updatedUser)) {
                currentUser = updatedUser;
                return true;
            }
        }
        return false;
    }

    /**
     * Update current user password.
     *
     * @param newPassword New plaintext password
     * @return true if update successful, false otherwise
     */
    public boolean updatePassword(String newPassword) {
        if (currentUser != null) {
            return userDAO.updatePassword(Integer.parseInt(currentUser.getId()), newPassword);
        }
        return false;
    }

    /**
     * Get all users from database.
     *
     * @return List of all users
     */
    public List<User> getAllUsers() {
        return userDAO.getAllUsers();
    }

    /**
     * Update user's blocked status.
     *
     * @param userId User ID
     * @param blocked true to block, false to unblock
     * @return true if successful, false otherwise
     */
    public boolean updateUserBlockedStatus(String userId, boolean blocked) {
        return userDAO.updateBlockedStatus(userId, blocked);
    }

    /**
     * Delete a user from the database.
     *
     * @param userId User ID to delete
     * @return true if successful, false otherwise
     */
    public boolean deleteUser(String userId) {
        return userDAO.deleteUser(userId);
    }
}

