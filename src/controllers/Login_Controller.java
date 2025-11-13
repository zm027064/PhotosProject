/**
 * Sample Skeleton for 'Login_Controller.fxml' Controller Class
 */

package controllers;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import model.DataStore;
import model.User;

/**
 * New login controller used by the updated login UI. Routes admin logins
 * to the Admin Portal and regular users to the NonAdmin view.
 *
 * <p>Validates the entered username and either shows an error alert or
 * navigates to the appropriate scene. Persists datastore on quit.</p>
 *
 * @author Zach
 */
public class Login_Controller {

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private Button loginButton;

    @FXML
    private Button quitButton;

    @FXML
    private TextField username;

    @FXML
    private TextField password;
    
    /** FX-injected login button. */
    // (fields above are injected via FXML)

    /**
     * Authenticate the entered username and route to the appropriate view
     * (admin portal or non-admin view). Shows error alerts for missing
     * or unknown users.
     *
     * @param event action event triggered by the login button
     */
    @FXML
    void auth(ActionEvent event) {
        String user = username.getText().trim().toLowerCase();
        
        if (user.isEmpty()) {
            new Alert(Alert.AlertType.ERROR, "Please enter a username").showAndWait();
            return;
        }
        
        // Check if admin
        if (user.equals("admin")) {
            try {
                Stage st = (Stage) loginButton.getScene().getWindow();
                Parent p = FXMLLoader.load(getClass().getResource("/controllers/Admin_Portal.fxml"));
                st.setScene(new Scene(p));
                st.setTitle("Admin Portal");
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            return;
        }
        
        // Check if user exists
        User u = DataStore.getInstance().getUser(user);
        if (u == null) {
            new Alert(Alert.AlertType.ERROR, "User not found").showAndWait();
            return;
        }
        
        // Load NonAdmin view
        try {
            Stage st = (Stage) loginButton.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/controllers/NonAdminController.fxml"));
            Parent p = loader.load();
            controllers.NonAdmin_Controller mc = loader.getController();
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
     * @param event action event triggered by the quit button
     */
    @FXML
    void quit(ActionEvent event) {
        try {
            DataStore.getInstance().save();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        Stage st = (Stage) quitButton.getScene().getWindow();
        st.close();
    }

    /**
     * Controller initialization performed after FXML injection.
     */
    @FXML
    void initialize() {
        assert loginButton != null : "fx:id=\"loginButton\" was not injected: check your FXML file 'Login_Controller.fxml'.";
        assert quitButton != null : "fx:id=\"quitButton\" was not injected: check your FXML file 'Login_Controller.fxml'.";
        assert username != null : "fx:id=\"username\" was not injected: check your FXML file 'Login_Controller.fxml'.";
        assert password != null : "fx:id=\"password\" was not injected: check your FXML file 'Login_Controller.fxml'.";
    }

}
