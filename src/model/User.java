package model;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * User holds albums and username/password (password optional).
 */
public class User implements Serializable {
    private static final long serialVersionUID = 3L;

    private String username;
    private String password; // optional
    private Map<String, Album> albums = new LinkedHashMap<>();

    public User(String username) { this(username, ""); }
    public User(String username, String password) {
        this.username = username;
        this.password = password == null ? "" : password;
    }

    public String getUsername() { return username; }
    public boolean checkPassword(String p) { return password.equals(p); }

    public Map<String, Album> getAlbums() { return albums; }

    public boolean createAlbum(String name) {
        if (albums.containsKey(name)) return false;
        albums.put(name, new Album(name));
        return true;
    }

    public boolean deleteAlbum(String name) {
        return albums.remove(name) != null;
    }

    public boolean renameAlbum(String oldName, String newName) {
        if (!albums.containsKey(oldName) || albums.containsKey(newName)) return false;
        Album a = albums.remove(oldName);
        a.setName(newName);
        albums.put(newName, a);
        return true;
    }
}
