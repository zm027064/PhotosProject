package controllers;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.FileChooser;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;
import model.Album;
import model.DataStore;
import model.Photo;
import model.Tag;
import model.User;

import java.io.File;
import java.util.List;
import java.util.Optional;

/**
 * Controller for the Album view. Manages displaying photos in an album and
 * performing operations such as add, remove, copy, move, tag, and navigation.
 *
 * <p>Handles photo selection, preview, tagging and operations that move or
 * copy photos between albums for the active user.</p>
 *
 * @author Zach
 */
public class AlbumController {
    /** FX-injected Back button. */
    @FXML public Button backButton;
    /** FX-injected label showing album name. */
    @FXML public Label albumNameLabel;
    /** FX-injected list view of photos in the album. */
    @FXML public ListView<Photo> photoListView;
    /** FX-injected ImageView displaying the selected photo. */
    @FXML public ImageView photoView;
    /** FX-injected label showing caption. */
    @FXML public Label captionLabel;
    /** FX-injected label showing photo date. */
    @FXML public Label dateLabel;
    /** FX-injected list view of tags for selected photo. */
    @FXML public ListView<String> tagsListView;
    /** FX-injected Prev/Next navigation buttons. */
    @FXML public Button prevButton, nextButton;
    /** FX-injected album action buttons (add/remove/copy/move/recaption/tag). */
    @FXML public Button addButton, removeButton, copyButton, moveButton, recapButton, addTagButton, removeTagButton;

    private User user;
    private Album album;
    private int currentIndex = -1;

    /**
     * Initialize controller context with the active user and album.
     *
     * @param u active user
     * @param a album to display
     */
    public void setContext(User u, Album a) {
        this.user = u; this.album = a;
        albumNameLabel.setText(a.getName());
        refreshPhotos();
    }

    private void refreshPhotos() {
        photoListView.setItems(FXCollections.observableArrayList(album.getPhotos()));
        photoListView.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Photo p, boolean empty) {
                super.updateItem(p, empty);
                if (empty || p==null) { setText(null); setGraphic(null); }
                else { setText(p.getCaption().isEmpty() ? new File(p.getFilePath()).getName() : p.getCaption()); }
            }
        });

        photoListView.getSelectionModel().selectedIndexProperty().addListener((obs,ov,nv)->{
            if (nv != null) showPhoto(nv.intValue());
        });
        if (!album.getPhotos().isEmpty()) {
            photoListView.getSelectionModel().select(0);
            showPhoto(0);
        } else {
            clearDisplay();
        }
    }

    private void showPhoto(int idx) {
        if (idx < 0 || idx >= album.getPhotos().size()) return;
        currentIndex = idx;
        Photo p = album.getPhotos().get(idx);
        try {
            Image img = new Image(new File(p.getFilePath()).toURI().toString());
            photoView.setImage(img);
        } catch (Exception e) {
            photoView.setImage(null);
        }
        captionLabel.setText("Caption: " + p.getCaption());
        dateLabel.setText("Date: " + p.getDateTime().toString());
        tagsListView.setItems(FXCollections.observableArrayList(p.getTags().stream().map(Tag::toString).toList()));
    }

    private void clearDisplay() {
        photoView.setImage(null);
        captionLabel.setText("Caption:");
        dateLabel.setText("Date:");
        tagsListView.setItems(FXCollections.emptyObservableList());
    }

    @FXML
    public void handleAdd() {
        FileChooser fc = new FileChooser();
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Images", "*.jpg","*.jpeg","*.png","*.gif","*.bmp"));
        File f = fc.showOpenDialog(addButton.getScene().getWindow());
        if (f == null) return;
        // Reuse existing Photo instance if it already exists in any album for this user
        String abs = f.getAbsolutePath();
        Photo existing = null;
        for (Album a : user.getAlbums().values()) {
            for (Photo ph : a.getPhotos()) {
                if (ph.getFilePath().equals(abs)) { existing = ph; break; }
            }
            if (existing != null) break;
        }
        Photo p = existing != null ? existing : new Photo(abs);
        if (!album.addPhoto(p)) {
            new Alert(Alert.AlertType.INFORMATION, "Photo already exists in album").showAndWait();
            return;
        }
        try { DataStore.getInstance().save(); } catch (Exception ex) {}
        refreshPhotos();
    }


    /**
     * Copy the currently selected photo to another album.
     */
    @FXML
    public void handleCopy() {
        int sel = photoListView.getSelectionModel().getSelectedIndex();
        if (sel < 0) return;
        Photo photo = album.getPhotos().get(sel);
        showAlbumSelector(photo, false);
    }


    /**
     * Move the currently selected photo to another album.
     */
    @FXML
    public void handleMove() {
        int sel = photoListView.getSelectionModel().getSelectedIndex();
        if (sel < 0) return;
        Photo photo = album.getPhotos().get(sel);
        showAlbumSelector(photo, true);
    }

    private void showAlbumSelector(Photo photo, boolean isMove) {
        List<String> albumNames = user.getAlbums().values().stream()
            .filter(a -> !a.equals(album))
            .map(Album::getName)
            .toList();
        if (albumNames.isEmpty()) {
            new Alert(Alert.AlertType.INFORMATION, "No other albums available").showAndWait();
            return;
        }
        ChoiceDialog<String> dialog = new ChoiceDialog<>(albumNames.get(0), albumNames);
        dialog.setHeaderText("Select target album to " + (isMove ? "move" : "copy") + " photo to");
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(targetAlbumName -> {
            Album targetAlbum = user.getAlbums().get(targetAlbumName);
            if (targetAlbum != null) {
                if (isMove) {
                    album.removePhoto(photo);
                }
                targetAlbum.addPhoto(photo);
                try { DataStore.getInstance().save(); } catch (Exception ex) {}
                refreshPhotos();
            }
        });
    }

    @FXML
    public void handleRemove() {
        int sel = photoListView.getSelectionModel().getSelectedIndex();
        if (sel < 0) return;
        Photo p = album.getPhotos().get(sel);
        album.removePhoto(p);
        try { DataStore.getInstance().save(); } catch (Exception ex) {}
        refreshPhotos();
    }


    /**
     * Set or update the caption for the selected photo.
     */
    @FXML
    public void handleRecaption() {
        int sel = photoListView.getSelectionModel().getSelectedIndex();
        if (sel < 0) return;
        Photo p = album.getPhotos().get(sel);
        TextInputDialog d = new TextInputDialog(p.getCaption());
        d.setHeaderText("Set caption");
        Optional<String> res = d.showAndWait();
        res.ifPresent(s -> {
            p.setCaption(s);
            try { DataStore.getInstance().save(); } catch (Exception ex) {}
            refreshPhotos();
        });
    }


    /**
     * Prompt for and add a tag to the selected photo.
     */
    @FXML
    public void handleAddTag() {
        int sel = photoListView.getSelectionModel().getSelectedIndex();
        if (sel < 0) return;
        Photo p = album.getPhotos().get(sel);
        TextInputDialog d = new TextInputDialog();
        d.setHeaderText("Add tag (format: name:value)\nExample: person:Alice or location:New Brunswick");
        Optional<String> res = d.showAndWait();
        res.ifPresent(s -> {
            String[] parts = s.split(":",2);
            if (parts.length < 2 || parts[0].trim().isEmpty() || parts[1].trim().isEmpty()) {
                new Alert(Alert.AlertType.ERROR, "Invalid format. Enter as name:value (e.g. person:Alice)").showAndWait();
                return;
            }
            String name = parts[0].trim();
            String value = parts[1].trim();
            // Only allow one value for location, but allow multiple for person
            if (name.equalsIgnoreCase("location")) {
                // Remove any existing location tag
                p.getTags().removeIf(t -> t.getName().equalsIgnoreCase("location"));
            }
            Tag t = new Tag(name, value);
            if (p.getTags().contains(t)) {
                new Alert(Alert.AlertType.INFORMATION, "Tag already present").showAndWait();
                return;
            }
            p.addTag(t);
            try { DataStore.getInstance().save(); } catch (Exception ex) {}
            showPhoto(sel);
        });
    }


    /**
     * Remove the selected tag from the selected photo.
     */
    @FXML
    public void handleRemoveTag() {
        int sel = photoListView.getSelectionModel().getSelectedIndex();
        int tsel = tagsListView.getSelectionModel().getSelectedIndex();
        if (sel < 0 || tsel < 0) return;
        Photo p = album.getPhotos().get(sel);
        List<String> tags = tagsListView.getItems();
        String chosen = tags.get(tsel);
        String[] parts = chosen.split(":",2);
        Tag t = new Tag(parts[0], parts.length>1?parts[1]:"");
        p.removeTag(t);
        try { DataStore.getInstance().save(); } catch (Exception ex) {}
        showPhoto(sel);
    }


    /**
     * Show previous photo in the album.
     */
    @FXML
    public void handlePrev() { if (currentIndex > 0) { photoListView.getSelectionModel().select(--currentIndex); showPhoto(currentIndex); } }

    /**
     * Show next photo in the album.
     */
    @FXML
    public void handleNext() { if (currentIndex+1 < album.getPhotos().size()) { photoListView.getSelectionModel().select(++currentIndex); showPhoto(currentIndex); } }

    /**
     * Navigate back to the user's main album view.
     */
    @FXML
    public void handleBack() {
        try {
            Stage st = (Stage) backButton.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/controllers/NonAdminController.fxml"));
            Parent p = loader.load();
            NonAdmin_Controller mc = loader.getController();
            mc.setUser(user);
            st.setScene(new Scene(p));
            st.setTitle("Photos - " + user.getUsername());
        } catch (Exception ex) { ex.printStackTrace(); }
    }
}
