package controllers;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import model.Album;
import model.DataStore;
import model.Photo;
import model.Tag;
import model.User;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Controller for searching photos by date range or tags. Produces a result
 * album from search results and allows navigation back to the main view.
 *
 * <p>Search results may be persisted into a new album for the active
 * user. Supports AND/OR semantics for tag searches.</p>
 *
 * @author Zach
 */
public class SearchController {
    /** FX-injected start date picker. */
    @FXML public DatePicker startDatePicker;
    /** FX-injected end date picker. */
    @FXML public DatePicker endDatePicker;
    /** FX-injected fields for primary tag name/value. */
    @FXML public TextField tag1NameField, tag1ValueField;
    /** FX-injected fields for secondary tag name/value. */
    @FXML public TextField tag2NameField, tag2ValueField;
    /** FX-injected radio buttons for AND/OR search semantics. */
    @FXML public RadioButton andRadio, orRadio;
    /** FX-injected list view of search results. */
    @FXML public ListView<Photo> resultsListView;
    /** FX-injected action buttons for searching and creating albums from results. */
    @FXML public Button searchDateButton, searchTagButton, createAlbumButton, backButton;

    private User user;
    private List<Photo> searchResults = new ArrayList<>();

    /**
     * Set the active user for search operations.
     *
     * @param u active user
     */
    public void setUser(User u) {
        this.user = u;
    }

    /**
     * Perform a date-range search across the user's albums and populate
     * the results list view.
     */
    @FXML
    public void handleSearchByDate() {
        LocalDate start = startDatePicker.getValue();
        LocalDate end = endDatePicker.getValue();
        if (start == null || end == null) {
            new Alert(Alert.AlertType.ERROR, "Please select both start and end dates").showAndWait();
            return;
        }
        if (start.isAfter(end)) {
            new Alert(Alert.AlertType.ERROR, "Start date must be before end date").showAndWait();
            return;
        }
        searchResults.clear();
        for (Album album : user.getAlbums().values()) {
            for (Photo photo : album.getPhotos()) {
                LocalDateTime photoDate = photo.getDateTime();
                if (!photoDate.toLocalDate().isBefore(start) && !photoDate.toLocalDate().isAfter(end)) {
                    if (!searchResults.contains(photo)) {
                        searchResults.add(photo);
                    }
                }
            }
        }
        updateResultsList();
    }

    /**
     * Search for photos by one or two tags (AND/OR semantics)
     * and populate the results list view.
     */
    @FXML
    public void handleSearchByTag() {
        String name1 = tag1NameField.getText().trim();
        String value1 = tag1ValueField.getText().trim();
        if (name1.isEmpty() || value1.isEmpty()) {
            new Alert(Alert.AlertType.ERROR, "At least one tag must be provided").showAndWait();
            return;
        }
        boolean isAnd = andRadio.isSelected();
        searchResults.clear();
        for (Album album : user.getAlbums().values()) {
            for (Photo photo : album.getPhotos()) {
                boolean matches = false;
                if (isAnd) {
                    boolean match1 = photo.getTags().stream()
                        .anyMatch(t -> t.getName().equalsIgnoreCase(name1) && t.getValue().equalsIgnoreCase(value1));
                    String name2 = tag2NameField.getText().trim();
                    String value2 = tag2ValueField.getText().trim();
                    boolean match2 = true;
                    if (!name2.isEmpty() && !value2.isEmpty()) {
                        match2 = photo.getTags().stream()
                            .anyMatch(t -> t.getName().equalsIgnoreCase(name2) && t.getValue().equalsIgnoreCase(value2));
                    }
                    matches = match1 && match2;
                } else {
                    boolean match1 = photo.getTags().stream()
                        .anyMatch(t -> t.getName().equalsIgnoreCase(name1) && t.getValue().equalsIgnoreCase(value1));
                    String name2 = tag2NameField.getText().trim();
                    String value2 = tag2ValueField.getText().trim();
                    boolean match2 = false;
                    if (!name2.isEmpty() && !value2.isEmpty()) {
                        match2 = photo.getTags().stream()
                            .anyMatch(t -> t.getName().equalsIgnoreCase(name2) && t.getValue().equalsIgnoreCase(value2));
                    }
                    matches = match1 || match2;
                }
                if (matches && !searchResults.contains(photo)) {
                    searchResults.add(photo);
                }
            }
        }
        updateResultsList();
    }


    /**
     * Create a new album from the current search results.
     */
    @FXML
    public void handleCreateAlbum() {
        if (searchResults.isEmpty()) {
            new Alert(Alert.AlertType.ERROR, "No search results to create album from").showAndWait();
            return;
        }
        TextInputDialog dialog = new TextInputDialog();
        dialog.setHeaderText("Enter album name for search results");
        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            String albumName = result.get().trim();
            if (albumName.isEmpty()) return;
            if (!user.createAlbum(albumName)) {
                new Alert(Alert.AlertType.ERROR, "Album name already exists").showAndWait();
                return;
            }
            Album newAlbum = user.getAlbums().get(albumName);
            for (Photo photo : searchResults) {
                newAlbum.addPhoto(photo);
            }
            try { DataStore.getInstance().save(); } catch (Exception ex) {}
            new Alert(Alert.AlertType.INFORMATION, "Album created with " + searchResults.size() + " photos").showAndWait();
        }
    }

    private void updateResultsList() {
        resultsListView.setItems(FXCollections.observableArrayList(searchResults));
        resultsListView.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Photo p, boolean empty) {
                super.updateItem(p, empty);
                if (empty || p == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(p.getCaption().isEmpty() ? p.getFilePath() : p.getCaption());
                }
            }
        });
    }

    /**
     * Navigate back to the main album view for the active user.
     */
    @FXML
    public void handleBack() {
        try {
            Stage stage = (Stage) backButton.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/controllers/NonAdminController.fxml"));
            Parent p = loader.load();
            NonAdmin_Controller controller = loader.getController();
            controller.setUser(user);
            stage.setScene(new Scene(p));
            stage.setTitle("Photos - " + user.getUsername());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
