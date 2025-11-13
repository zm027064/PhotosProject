package model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Album holds photos in a list order.
 *
 * <p>Provides methods to add/remove photos and to query date ranges.</p>
 *
    * Album model representing a named collection of photos.
    *
    * <p>Provides operations to add/remove photos and to query album
    * metadata such as size and date range.</p>
    *
    * @author Prayrit
    */
public class Album implements Serializable {
    private static final long serialVersionUID = 2L;

    private String name;
    private List<Photo> photos = new ArrayList<>();

    /**
     * Create a new album with the given name.
     *
     * @param name album name
     */
    /**
     * Construct an album with the provided name.
     *
     * @param name album name
     */
    public Album(String name) { this.name = name; }

    /**
     * @return album name
     */
    public String getName() { return name; }

    /**
     * Set the album name (caller should ensure uniqueness at the User level).
     *
     * @param n new name
     */
    public void setName(String n) { name = n; }

    /**
     * Direct (modifiable) list of photos in the album. Prefer {@link #getPhotosUnmodifiable()} when exposing externally.
     *
     * @return list of photos
     */
    public List<Photo> getPhotos() { return photos; }

    /**
     * Add a photo to the album if it is not already present.
     *
     * @param p photo to add
     * @return true if added, false if already present
     */
    public boolean addPhoto(Photo p) {
        if (photos.contains(p)) return false;
        photos.add(p);
        return true;
    }

    /**
     * Remove a photo from the album.
     *
     * @param p photo to remove
     * @return true if removed
     */
    public boolean removePhoto(Photo p) { return photos.remove(p); }

    /**
     * Number of photos in the album.
     *
     * @return count of photos
     */
    public int size() { return photos.size(); }

    /**
     * Earliest photo date in the album, or null if none.
     *
     * @return earliest LocalDateTime or null
     */
    public LocalDateTime getStartDate() {
        return photos.stream().map(Photo::getDateTime).min(LocalDateTime::compareTo).orElse(null);
    }

    /**
     * Latest photo date in the album, or null if none.
     *
     * @return latest LocalDateTime or null
     */
    public LocalDateTime getEndDate() {
        return photos.stream().map(Photo::getDateTime).max(LocalDateTime::compareTo).orElse(null);
    }

    /**
     * Unmodifiable view of the photos list.
     *
     * @return unmodifiable list of photos
     */
    public List<Photo> getPhotosUnmodifiable() { return Collections.unmodifiableList(photos); }
}
