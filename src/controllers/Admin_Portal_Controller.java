package controllers;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.Alert;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import model.DataStore;
import model.User;

/**
 * Controller for the admin portal UI. Allows adding and deleting users
 * and selecting a user from the list.
 *
 * <p>The admin portal is used by the administrator to create and remove
 * regular user accounts. Special users such as {@code admin} and
 * {@code stock} are protected.</p>
 *
 * @author Zach
 */
public class Admin_Portal_Controller {

    /** FX resource bundle injected by FXML. */
    @FXML private ResourceBundle resources;
    /** FXML location injected. */
    @FXML private URL location;
    /** Button to add a user. */
    @FXML private Button Add_User_Button;
    /** Button to delete a user. */
    @FXML private Button Delete_User_Button;
    /** Button to logout admin and return to login. */
    @FXML private Button logout_admin_button;
    /** List view showing configured users. */
    @FXML private ListView<String> userListView;
    
    private String selectedUser = null;

    @FXML
    /**
     * Prompt for and create a new non-special user.
     *
     * @param event action event from the UI
     */
    void Add_User_Admin(ActionEvent event) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Add New User");
        dialog.setHeaderText("Create a new user");
        dialog.setContentText("Enter username:");
        Optional<String> result = dialog.showAndWait();
        
        if (result.isPresent()) {
            String username = result.get().trim().toLowerCase();
            if (username.isEmpty()) {
                new Alert(Alert.AlertType.ERROR, "Username cannot be empty").showAndWait();
                return;
            }
            
            if (username.equals("admin") || username.equals("stock")) {
                new Alert(Alert.AlertType.ERROR, "Cannot create special users").showAndWait();
                return;
            }
            
            if (DataStore.getInstance().getUser(username) != null) {
                new Alert(Alert.AlertType.ERROR, "User already exists").showAndWait();
                return;
            }
            
            DataStore.getInstance().addUser(new User(username));
            try {
                DataStore.getInstance().save();
                new Alert(Alert.AlertType.INFORMATION, "User created successfully").showAndWait();
                refreshUserList();
            } catch (Exception e) {
                new Alert(Alert.AlertType.ERROR, "Failed to save user").showAndWait();
                e.printStackTrace();
            }
        }
    }

    @FXML
    /**
     * Delete the selected user after confirmation. Special users are protected.
     *
     * @param event action event from the UI
     */
    void Delete_User_Action(ActionEvent event) {
        if (selectedUser == null) {
            new Alert(Alert.AlertType.WARNING, "Please select a user to delete").showAndWait();
            return;
        }
        
        if (selectedUser.equals("admin") || selectedUser.equals("stock")) {
            new Alert(Alert.AlertType.ERROR, "Cannot delete special users").showAndWait();
            return;
        }
        
        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION, 
            "Are you sure you want to delete user '" + selectedUser + "'?");
        Optional<javafx.scene.control.ButtonType> result = confirmDialog.showAndWait();
        
        if (result.isPresent() && result.get() == javafx.scene.control.ButtonType.OK) {
            DataStore.getInstance().deleteUser(selectedUser);
            try {
                DataStore.getInstance().save();
                new Alert(Alert.AlertType.INFORMATION, "User deleted successfully").showAndWait();
                selectedUser = null;
                refreshUserList();
            } catch (Exception e) {
                new Alert(Alert.AlertType.ERROR, "Failed to delete user").showAndWait();
                e.printStackTrace();
            }
        }
    }

    @FXML
    /**
     * Save data and return to the login screen.
     *
     * @param event action event from the UI
     */
    void logout_Admin(ActionEvent event) {
        try {
            DataStore.getInstance().save();
            Stage st = (Stage) logout_admin_button.getScene().getWindow();
            Parent p = FXMLLoader.load(getClass().getResource("/controllers/Login_Controller.fxml"));
            st.setScene(new Scene(p));
            st.setTitle("Photos - Login");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    /**
     * Record the currently-selected user in the list view.
     *
     * @param event mouse event from the list view
     */
    void select_user(MouseEvent event) {
        selectedUser = userListView.getSelectionModel().getSelectedItem();
    }

    @FXML
    /**
     * Initialize the admin portal UI and populate the user list.
     */
    void initialize() {
        assert Add_User_Button != null : "fx:id=\"Add_User_Button\" was not injected: check your FXML file 'Admin_Portal.fxml'.";
        assert Delete_User_Button != null : "fx:id=\"Delete_User_Button\" was not injected: check your FXML file 'Admin_Portal.fxml'.";
        assert logout_admin_button != null : "fx:id=\"logout_admin_button\" was not injected: check your FXML file 'Admin_Portal.fxml'.";
        
        refreshUserList();
    }
    
    private void refreshUserList() {
        userListView.setItems(FXCollections.observableArrayList(DataStore.getInstance().getUsers().keySet()));
    }
}
