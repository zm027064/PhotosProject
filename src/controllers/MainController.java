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

public class MainController {
    @FXML public ListView<String> albumListView;
    @FXML public Button createButton;
    @FXML public Button deleteButton;
    @FXML public Button renameButton;
    @FXML public Button openButton;
    @FXML public Button searchButton;
    @FXML public Button logoutButton;
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

    public void setUser(User u) {
        this.user = u;
        refresh();
    }

    @FXML
    public void initialize() {}

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

    @FXML
    public void handleRename() {
        String sel = albumListView.getSelectionModel().getSelectedItem();
        if (sel == null) return;
        String oldName = sel.split(" ")[0];
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

    @FXML
    public void handleLogout() {
        try { DataStore.getInstance().save(); } catch (Exception ex) {}
        try {
            Stage st = (Stage) logoutButton.getScene().getWindow();
            Parent p = FXMLLoader.load(getClass().getResource("/controllers/login.fxml"));
            st.setScene(new Scene(p));
            st.setTitle("Photos - Login");
        } catch (Exception ex) { ex.printStackTrace(); }
    }
}
