package controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import model.DataStore;
import model.User;

import javafx.scene.control.Alert;

/**
 * Legacy login controller (kept for compatibility). Handles simple login
 * interactions from the original UI.
 *
 * <p>Routes successful logins to the non-admin view and preserves a
 * backward-compatible entry point for older FXML screens.</p>
 *
 * @author Zach
 */
public class LoginController {
    @FXML public TextField usernameField;
    @FXML public PasswordField passwordField;
    @FXML public Button loginButton;

    /**
     * Controller initialization after FXML injection (legacy login view).
     */
    @FXML
    public void initialize() {}

    /**
     * Handle login action for the legacy login view.
     *
     * @param e action event from the login button
     */
    @FXML
    public void handleLogin(ActionEvent e) {
        String username = usernameField.getText().trim().toLowerCase(); // Make case-insensitive
        String password = passwordField.getText();
        if (username.isEmpty()) {
            new Alert(Alert.AlertType.ERROR, "Please enter a username").showAndWait();
            return;
        }

        if (username.equals("admin")) {
            // show admin portal scene
            try {
                Stage st = (Stage) loginButton.getScene().getWindow();
                Parent p = FXMLLoader.load(getClass().getResource("/controllers/Login_Controller.fxml"));
                st.setScene(new Scene(p));
                st.setTitle("Admin Portal");
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            return;
        }

        DataStore ds = DataStore.getInstance();
        User u = ds.getUser(username);
        if (u == null) {
            new Alert(Alert.AlertType.ERROR, "User not found. Please ask admin to create user.").showAndWait();
            return;
        }

        // show main scene for user
        try {
            Stage st = (Stage) loginButton.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/controllers/NonAdminController.fxml"));
            Parent p = loader.load();
            // pass user
            NonAdmin_Controller mc = loader.getController();
            mc.setUser(u);
            st.setScene(new Scene(p));
            st.setTitle("Photos - " + u.getUsername());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Persist datastore and close the application window.
     *
     * @param e action event from the quit button
     */
    @FXML
    public void handleQuit(ActionEvent e) {
        Stage st = (Stage) loginButton.getScene().getWindow();
        try { DataStore.getInstance().save(); } catch (Exception ex) { ex.printStackTrace(); }
        st.close();
    }
}
