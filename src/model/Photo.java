package model;

import java.io.File;
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
 */
public class Photo implements Serializable {
    private static final long serialVersionUID = 2L;

    private String filePath; // absolute path or relative path for stock photos
    private String caption;
    private LocalDateTime dateTime;
    private Set<Tag> tags = new LinkedHashSet<>();

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

    public String getFilePath() { return filePath; }
    public String getCaption() { return caption; }
    public void setCaption(String c) { caption = c; }
    public LocalDateTime getDateTime() { return dateTime; }

    public Set<Tag> getTags() { return tags; }

    public boolean addTag(Tag t) { return tags.add(t); }
    public boolean removeTag(Tag t) { return tags.remove(t); }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Photo)) return false;
        Photo p = (Photo) o;
        return filePath.equals(p.filePath);
    }

    @Override
    public int hashCode() { return filePath.hashCode(); }
}
