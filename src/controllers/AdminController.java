package controllers;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.Alert;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import model.DataStore;
import model.User;

import java.util.Optional;

public class AdminController {
    @FXML public ListView<String> userListView;
    @FXML public Button createButton;
    @FXML public Button deleteButton;
    @FXML public Button backButton;

    @FXML
    public void initialize() {
        refresh();
    }

    private void refresh() {
        userListView.setItems(FXCollections.observableArrayList(DataStore.getInstance().getUsers().keySet()));
    }

    @FXML
    public void handleCreate(ActionEvent e) {
        TextInputDialog d = new TextInputDialog();
        d.setHeaderText("Create new user (username)");
        Optional<String> res = d.showAndWait();
        if (res.isPresent()) {
            String username = res.get().trim();
            if (username.isEmpty()) return;
            if (DataStore.getInstance().getUser(username) != null) {
                new Alert(Alert.AlertType.ERROR, "User exists").showAndWait();
                return;
            }
            DataStore.getInstance().addUser(new User(username));
            try { DataStore.getInstance().save(); } catch (Exception ex) {}
            refresh();
        }
    }

    @FXML
    public void handleDelete(ActionEvent e) {
        String sel = userListView.getSelectionModel().getSelectedItem();
        if (sel == null) return;
        if (sel.equals("admin") || sel.equals("stock")) {
            new Alert(Alert.AlertType.ERROR, "Cannot delete special users").showAndWait();
            return;
        }
        DataStore.getInstance().deleteUser(sel);
        try { DataStore.getInstance().save(); } catch (Exception ex) {}
        refresh();
    }

    @FXML
    public void handleBack(ActionEvent e) {
        try {
            Stage st = (Stage) backButton.getScene().getWindow();
            Parent p = FXMLLoader.load(getClass().getResource("/controllers/login.fxml"));
            st.setScene(new Scene(p));
            st.setTitle("Photos - Login");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
