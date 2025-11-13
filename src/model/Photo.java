package model;

import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Serializable photo model.
 *
 * <p>Wraps a file path, caption, capture date (read from the file's
 * last-modified time) and a set of tags. Instances are serialized by
 * the application to persist user albums and photos.</p>
 *
 * @author Prayrit
 */
public class Photo implements Serializable {
    private static final long serialVersionUID = 2L;

    private String filePath; // absolute path or relative path for stock photos
    private String caption;
    private LocalDateTime dateTime;
    private Set<Tag> tags = new LinkedHashSet<>();

    /**
     * Construct a Photo for the given file path.
     *
     * @param filePath path to the image file (absolute or project-relative)
     */
    public Photo(String filePath) {
        this.filePath = filePath;
        this.caption = "";
        this.dateTime = readFileDate(filePath);
    }

    private LocalDateTime readFileDate(String path) {
        try {
            Path p = Path.of(path);
            if (!p.toFile().exists()) p = Path.of(path).toAbsolutePath();
            Instant instant = Files.getLastModifiedTime(p).toInstant();
            return LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
        } catch (IOException e) {
            return LocalDateTime.ofInstant(Instant.EPOCH, ZoneId.systemDefault());
        }
    }

    /**
     * @return stored file path
     */
    public String getFilePath() { return filePath; }

    /**
     * @return caption text
     */
    public String getCaption() { return caption; }

    /**
     * Set the photo caption.
     *
     * @param c caption
     */
    public void setCaption(String c) { caption = c; }

    /**
     * @return date/time associated with the photo (derived from file)
     */
    public LocalDateTime getDateTime() { return dateTime; }

    /**
     * @return an unmodifiable view of tags attached to the photo
     */
    public Set<Tag> getTags() { return tags; }

    /**
     * Add a tag to this photo. Duplicate tags (same type/value) are ignored.
     *
     * @param t tag to add
     * @return true if the tag was added
     */
    public boolean addTag(Tag t) { return tags.add(t); }

    /**
     * Remove a tag from this photo.
     *
     * @param t tag to remove
     * @return true if removed
     */
    public boolean removeTag(Tag t) { return tags.remove(t); }

    /**
     * Equality is based on the photo's file path.
     *
     * @param o other object
     * @return true if the other object is a Photo with the same file path
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Photo)) return false;
        Photo p = (Photo) o;
        return filePath.equals(p.filePath);
    }

    /**
     * Hash code consistent with {@link #equals(Object)}.
     *
     * @return hash code
     */
    @Override
    public int hashCode() { return filePath.hashCode(); }
}
