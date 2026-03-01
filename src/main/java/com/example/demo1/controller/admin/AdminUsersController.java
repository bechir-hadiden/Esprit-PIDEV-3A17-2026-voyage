package com.example.demo1.controller.admin;

import com.example.demo1.entity.User;
import com.example.demo1.services.AuthService;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public class AdminUsersController {

    @FXML
    private TableView<User> usersTable;
    @FXML
    private TableColumn<User, String> avatarColumn;
    @FXML
    private TableColumn<User, String> nameColumn;
    @FXML
    private TableColumn<User, String> emailColumn;
    @FXML
    private TableColumn<User, String> roleColumn;
    @FXML
    private TableColumn<User, String> statusColumn;
    @FXML
    private TableColumn<User, Void> actionsColumn;

    @FXML
    private TextField searchField;
    @FXML
    private ComboBox<String> filterComboBox;

    @FXML
    private Label totalUsersLabel;
    @FXML
    private Label activeUsersLabel;
    @FXML
    private Label blockedUsersLabel;
    @FXML
    private Label adminsLabel;
    @FXML
    private Label messageLabel;

    private final AuthService authService = AuthService.getInstance();
    private ObservableList<User> usersList = FXCollections.observableArrayList();
    private ObservableList<User> filteredUsers = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        setupTable();
        setupFilterComboBox();
        setupSearch();
        loadUsers();
    }

    private void setupTable() {
        // Avatar column with initials
        avatarColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    User user = getTableRow().getItem();
                    Label avatar = new Label(user.getInitials());
                    avatar.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; " +
                            "-fx-font-weight: bold; -fx-background-radius: 50%; " +
                            "-fx-min-width: 40; -fx-min-height: 40; " +
                            "-fx-alignment: center; -fx-font-size: 14;");
                    setGraphic(avatar);
                    setText(null);
                }
            }
        });

        nameColumn.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        roleColumn.setCellValueFactory(new PropertyValueFactory<>("role"));

        // Status column with styling
        statusColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    User user = getTableRow().getItem();
                    boolean isBlocked = user.isBlocked();
                    Label status = new Label(isBlocked ? "🔴 Blocked" : "🟢 Active");
                    status.setStyle(isBlocked
                            ? "-fx-text-fill: #ef4444; -fx-font-weight: bold;"
                            : "-fx-text-fill: #22c55e; -fx-font-weight: bold;");
                    setGraphic(status);
                    setText(null);
                }
            }
        });

        // Actions column with buttons
        actionsColumn.setCellFactory(column -> new TableCell<>() {
            private final Button blockBtn = new Button();
            private final Button editBtn = new Button("✏️");
            private final Button deleteBtn = new Button("🗑️");
            private final HBox container = new HBox(5, blockBtn, editBtn, deleteBtn);

            {
                blockBtn.setStyle("-fx-background-color: #f59e0b; -fx-text-fill: white; " +
                        "-fx-font-size: 11; -fx-padding: 5 10; -fx-background-radius: 4;");
                editBtn.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; " +
                        "-fx-font-size: 11; -fx-padding: 5 10; -fx-background-radius: 4;");
                deleteBtn.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; " +
                        "-fx-font-size: 11; -fx-padding: 5 10; -fx-background-radius: 4;");

                blockBtn.setOnAction(e -> handleBlockToggle(getTableRow().getItem()));
                editBtn.setOnAction(e -> handleEditUser(getTableRow().getItem()));
                deleteBtn.setOnAction(e -> handleDeleteUser(getTableRow().getItem()));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                } else {
                    User user = getTableRow().getItem();
                    boolean isBlocked = user.isBlocked();
                    blockBtn.setText(isBlocked ? "🔓 Unblock" : "🔒 Block");
                    blockBtn.setStyle(isBlocked
                            ? "-fx-background-color: #22c55e; -fx-text-fill: white; -fx-font-size: 11; -fx-padding: 5 10; -fx-background-radius: 4;"
                            : "-fx-background-color: #f59e0b; -fx-text-fill: white; -fx-font-size: 11; -fx-padding: 5 10; -fx-background-radius: 4;");
                    setGraphic(container);
                }
            }
        });

        usersTable.setItems(filteredUsers);
    }

    private void setupFilterComboBox() {
        filterComboBox.setItems(FXCollections.observableArrayList(
                "All Users", "Active Only", "Blocked Only", "Admins Only"
        ));
        filterComboBox.getSelectionModel().selectFirst();
        filterComboBox.setOnAction(e -> applyFilter());
    }

    private void setupSearch() {
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            applyFilter();
        });
    }

    @FXML
    private void handleRefresh() {
        loadUsers();
        showMessage("Users refreshed successfully", false);
    }

    @FXML
    private void handleAddUser() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/admin/UserDialog.fxml"));
            DialogPane dialogPane = loader.load();
            
            UserDialogController controller = loader.getController();
            controller.initForAdd();
            
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane(dialogPane);
            dialog.setTitle("Add New User");
            
            // Set save button action
            Button saveButton = (Button) dialogPane.lookupButton(ButtonType.OK);
            saveButton.setText("Save User");
            saveButton.setOnAction(e -> {
                controller.handleSave();
                e.consume();
            });
            
            dialog.showAndWait().ifPresent(result -> {
                if (result == ButtonType.OK && controller.isSaved()) {
                    loadUsers();
                    showMessage("✅ User added successfully!", false);
                }
            });
            
        } catch (IOException e) {
            showMessage("❌ Error opening add user dialog: " + e.getMessage(), true);
            e.printStackTrace();
        }
    }

    private void handleBlockToggle(User user) {
        if (user == null) return;

        // Prevent blocking yourself
        User currentUser = authService.getCurrentUser();
        if (currentUser != null && currentUser.getId().equals(user.getId())) {
            showMessage("❌ You cannot block yourself!", true);
            return;
        }

        boolean newBlockedState = !user.isBlocked();
        String action = newBlockedState ? "block" : "unblock";

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm " + action);
        confirm.setHeaderText("Are you sure you want to " + action + " this user?");
        confirm.setContentText("User: " + user.getFullName() + "\nEmail: " + user.getEmail());

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            Task<Boolean> task = new Task<>() {
                @Override
                protected Boolean call() {
                    return authService.updateUserBlockedStatus(user.getId(), newBlockedState);
                }
            };
            task.setOnSucceeded(e -> {
                if (task.getValue()) {
                    user.setBlocked(newBlockedState);
                    usersTable.refresh();
                    updateStats();
                    showMessage("✅ User " + action + "ed successfully!", false);
                } else {
                    showMessage("❌ Failed to " + action + " user", true);
                }
            });
            task.setOnFailed(e -> {
                showMessage("❌ Error: " + task.getException().getMessage(), true);
            });
            new Thread(task).start();
        }
    }

    private void handleEditUser(User user) {
        if (user == null) return;
        
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/admin/UserDialog.fxml"));
            DialogPane dialogPane = loader.load();
            
            UserDialogController controller = loader.getController();
            controller.initForEdit(user);
            
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane(dialogPane);
            dialog.setTitle("Edit User");
            
            // Set save button action
            Button saveButton = (Button) dialogPane.lookupButton(ButtonType.OK);
            saveButton.setText("Save Changes");
            saveButton.setOnAction(e -> {
                controller.handleSave();
                e.consume();
            });
            
            dialog.showAndWait().ifPresent(result -> {
                if (result == ButtonType.OK) {
                    usersTable.refresh();
                    updateStats();
                    showMessage("✅ User updated successfully!", false);
                }
            });
            
        } catch (IOException e) {
            showMessage("❌ Error opening edit user dialog: " + e.getMessage(), true);
            e.printStackTrace();
        }
    }

    private void handleDeleteUser(User user) {
        if (user == null) return;

        // Prevent deleting yourself
        User currentUser = authService.getCurrentUser();
        if (currentUser != null && currentUser.getId().equals(user.getId())) {
            showMessage("❌ You cannot delete yourself!", true);
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Delete");
        confirm.setHeaderText("Are you sure you want to delete this user?");
        confirm.setContentText("User: " + user.getFullName() + "\nEmail: " + user.getEmail() + "\n\n⚠️ This action cannot be undone!");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            Task<Boolean> task = new Task<>() {
                @Override
                protected Boolean call() {
                    return authService.deleteUser(user.getId());
                }
            };
            task.setOnSucceeded(e -> {
                if (task.getValue()) {
                    usersList.remove(user);
                    filteredUsers.remove(user);
                    updateStats();
                    showMessage("✅ User deleted successfully!", false);
                } else {
                    showMessage("❌ Failed to delete user", true);
                }
            });
            task.setOnFailed(e -> {
                showMessage("❌ Error: " + task.getException().getMessage(), true);
            });
            new Thread(task).start();
        }
    }

    private void loadUsers() {
        Task<List<User>> task = new Task<>() {
            @Override
            protected List<User> call() {
                return authService.getAllUsers();
            }
        };
        task.setOnSucceeded(e -> {
            Platform.runLater(() -> {
                usersList.clear();
                usersList.addAll(task.getValue());
                applyFilter();
                updateStats();
            });
        });
        task.setOnFailed(e -> {
            showMessage("❌ Error loading users: " + task.getException().getMessage(), true);
        });
        new Thread(task).start();
    }

    private void applyFilter() {
        String searchText = searchField.getText().toLowerCase().trim();
        String filter = filterComboBox.getValue();

        filteredUsers.clear();

        for (User user : usersList) {
            // Search filter
            boolean matchesSearch = searchText.isEmpty()
                    || user.getFullName().toLowerCase().contains(searchText)
                    || user.getEmail().toLowerCase().contains(searchText);

            // Status filter
            boolean matchesFilter = true;
            if ("Active Only".equals(filter)) {
                matchesFilter = !user.isBlocked();
            } else if ("Blocked Only".equals(filter)) {
                matchesFilter = user.isBlocked();
            } else if ("Admins Only".equals(filter)) {
                matchesFilter = "ADMIN".equals(user.getRole());
            }

            if (matchesSearch && matchesFilter) {
                filteredUsers.add(user);
            }
        }
    }

    private void updateStats() {
        long total = usersList.size();
        long active = usersList.stream().filter(u -> !u.isBlocked()).count();
        long blocked = usersList.stream().filter(User::isBlocked).count();
        long admins = usersList.stream().filter(u -> "ADMIN".equals(u.getRole())).count();

        totalUsersLabel.setText(String.valueOf(total));
        activeUsersLabel.setText(String.valueOf(active));
        blockedUsersLabel.setText(String.valueOf(blocked));
        adminsLabel.setText(String.valueOf(admins));
    }

    private void showMessage(String message, boolean isError) {
        messageLabel.setText(message);
        messageLabel.setVisible(true);
        messageLabel.setStyle(isError
                ? "-fx-text-fill: #ef4444; -fx-background-color: #fef2f2; -fx-padding: 10; -fx-background-radius: 6;"
                : "-fx-text-fill: #22c55e; -fx-background-color: #f0fdf4; -fx-padding: 10; -fx-background-radius: 6;");

        // Auto-hide after 5 seconds
        new Thread(() -> {
            try {
                Thread.sleep(5000);
                Platform.runLater(() -> messageLabel.setVisible(false));
            } catch (InterruptedException ignored) {}
        }).start();
    }
}
