package controllers;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextInputDialog;
import javafx.stage.Stage;
import model.Album;
import model.DataStore;
import model.User;

import java.time.format.DateTimeFormatter;
import java.util.Optional;

/**
 * Controller for the non-admin user main view. Lists albums and provides
 * create/delete/rename/open/search/logout actions for the active user.
 *
 * <p>Similar to {@link controllers.MainController} but used by the
 * renamed NonAdmin UI FXML.</p>
 *
 * @author Zach
 */
public class NonAdmin_Controller {
    /** FX-injected list view showing album names. */
    @FXML public ListView<String> albumListView;
    /** FX-injected button to create an album. */
    @FXML public Button createButton;
    /** FX-injected button to delete the selected album. */
    @FXML public Button deleteButton;
    /** FX-injected button to rename the selected album. */
    @FXML public Button renameButton;
    /** FX-injected button to open the selected album. */
    @FXML public Button openButton;
    /** FX-injected button to open the search view. */
    @FXML public Button searchButton;
    /** FX-injected logout button. */
    @FXML public Button logoutButton;

    /**
     * Open the search view and pass the active user to it.
     */
    @FXML
    public void handleSearch() {
        try {
            Stage stage = (Stage) searchButton.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/controllers/search.fxml"));
            Parent root = loader.load();
            SearchController controller = loader.getController();
            controller.setUser(user);
            stage.setScene(new Scene(root));
            stage.setTitle("Search Photos - " + user.getUsername());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private User user;

    /**
     * Set the active user for this view and refresh the album list.
     *
     * @param u active user
     */
    public void setUser(User u) {
        this.user = u;
        refresh();
    }

    /**
     * Initialize controller after FXML injection.
     */
    @FXML
    public void initialize() {}

    /**
     * Create a new album for the current user (prompts for name).
     */

    private void refresh() {
        if (user == null) return;
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        albumListView.setItems(FXCollections.observableArrayList(
                user.getAlbums().values().stream().map(a -> {
                    String range="";
                    if (a.getStartDate() != null && a.getEndDate()!=null) range = " ("+a.getStartDate().format(fmt)+" - "+a.getEndDate().format(fmt)+")";
                    return a.getName() + " [" + a.size() + "]" + range;
                }).toList()
        ));
    }
    

    /**
     * Prompt for and create a new album for the active user.
     */
    @FXML
    public void handleCreate() {
        TextInputDialog d = new TextInputDialog();
        d.setHeaderText("Create album - name");
        Optional<String> res = d.showAndWait();
        if (res.isPresent()) {
            String name = res.get().trim();
            if (name.isEmpty()) return;
            if (!user.createAlbum(name)) {
                new Alert(Alert.AlertType.ERROR, "Album exists").showAndWait();
                return;
            }
            try { DataStore.getInstance().save(); } catch (Exception ex) {}
            refresh();
        }
    }

    /**
     * Delete the selected album for the active user.
     */
    @FXML
    public void handleDelete() {
        String sel = albumListView.getSelectionModel().getSelectedItem();
        if (sel == null) return;
        String name = sel.split(" ")[0];
        if (!user.deleteAlbum(name)) {
            new Alert(Alert.AlertType.ERROR, "Failed to delete album").showAndWait();
        }
        try { DataStore.getInstance().save(); } catch (Exception ex) {}
        refresh();
    }

    /**
     * Rename the selected album. The special built-in "stock" album is not
     * renamable for the stock user; an error alert is shown in that case.
     */

    /**
     * Rename the selected album (blocks renaming built-in stock album for stock user).
     */
    @FXML
    public void handleRename() {
        String sel = albumListView.getSelectionModel().getSelectedItem();
        if (sel == null) return;
        String oldName = sel.split(" ")[0];
        // If logged-in user is the stock user and this is the stock album, block renaming with a clear message
        if (user != null && user.getUsername() != null && user.getUsername().equalsIgnoreCase("stock") && "stock".equals(oldName)) {
            new Alert(Alert.AlertType.ERROR, "You cannot rename this album.").showAndWait();
            return;
        }

        TextInputDialog d = new TextInputDialog(oldName);
        d.setHeaderText("Rename album");
        Optional<String> res = d.showAndWait();
        if (res.isPresent()) {
            String newName = res.get().trim();
            if (!user.renameAlbum(oldName, newName)) {
                new Alert(Alert.AlertType.ERROR, "Rename failed (duplicate?)").showAndWait();
            }
            try { DataStore.getInstance().save(); } catch (Exception ex) {}
            refresh();
        }
    }

    /**
     * Open the selected album view.
     */
    @FXML
    public void handleOpen() {
        String sel = albumListView.getSelectionModel().getSelectedItem();
        if (sel == null) return;
        String name = sel.split(" ")[0];
        try {
            Stage st = (Stage) openButton.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/controllers/album.fxml"));
            Parent p = loader.load();
            AlbumController ac = loader.getController();
            ac.setContext(user, user.getAlbums().get(name));
            st.setScene(new Scene(p));
            st.setTitle("Album: " + name + " - " + user.getUsername());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Save datastore and return to the login screen.
     */
    @FXML
    public void handleLogout() {
        try { DataStore.getInstance().save(); } catch (Exception ex) {}
        try {
            Stage st = (Stage) logoutButton.getScene().getWindow();
            Parent p = FXMLLoader.load(getClass().getResource("/controllers/Login_Controller.fxml"));
            st.setScene(new Scene(p));
            st.setTitle("Photos - Login");
        } catch (Exception ex) { ex.printStackTrace(); }
    }
}
