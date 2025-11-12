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

public class LoginController {
    @FXML public TextField usernameField;
    @FXML public PasswordField passwordField;
    @FXML public Button loginButton;

    @FXML
    public void initialize() {}

    @FXML
    public void handleLogin(ActionEvent e) {
        String username = usernameField.getText().trim().toLowerCase(); // Make case-insensitive
        String password = passwordField.getText();
        if (username.isEmpty()) {
            new Alert(Alert.AlertType.ERROR, "Please enter a username").showAndWait();
            return;
        }

        if (username.equals("admin")) {
            // show admin scene
            try {
                Stage st = (Stage) loginButton.getScene().getWindow();
                Parent p = FXMLLoader.load(getClass().getResource("/controllers/admin.fxml"));
                st.setScene(new Scene(p));
                st.setTitle("Admin - Users");
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/controllers/main.fxml"));
            Parent p = loader.load();
            // pass user
            MainController mc = loader.getController();
            mc.setUser(u);
            st.setScene(new Scene(p));
            st.setTitle("Photos - " + u.getUsername());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @FXML
    public void handleQuit(ActionEvent e) {
        Stage st = (Stage) loginButton.getScene().getWindow();
        try { DataStore.getInstance().save(); } catch (Exception ex) { ex.printStackTrace(); }
        st.close();
    }
}
