package model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Album holds photos in a list order.
 */
public class Album implements Serializable {
    private static final long serialVersionUID = 2L;

    private String name;
    private List<Photo> photos = new ArrayList<>();

    public Album(String name) { this.name = name; }

    public String getName() { return name; }
    public void setName(String n) { name = n; }

    public List<Photo> getPhotos() { return photos; }

    public boolean addPhoto(Photo p) {
        if (photos.contains(p)) return false;
        photos.add(p);
        return true;
    }

    public boolean removePhoto(Photo p) { return photos.remove(p); }

    public int size() { return photos.size(); }

    public LocalDateTime getStartDate() {
        return photos.stream().map(Photo::getDateTime).min(LocalDateTime::compareTo).orElse(null);
    }

    public LocalDateTime getEndDate() {
        return photos.stream().map(Photo::getDateTime).max(LocalDateTime::compareTo).orElse(null);
    }

    public List<Photo> getPhotosUnmodifiable() { return Collections.unmodifiableList(photos); }
}
