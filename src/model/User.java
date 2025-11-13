package model;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * User holds albums and username/password (password optional).
 *
 * <p>Provides operations to create, delete and rename albums owned by the user.</p>
 *
 * @author Prayrit
 */
public class User implements Serializable {
    private static final long serialVersionUID = 3L;

    private String username;
    private String password; // optional
    private Map<String, Album> albums = new LinkedHashMap<>();

    /**
     * Create a user with no password.
     *
     * @param username username
     */
    public User(String username) { this(username, ""); }

    /**
     * Create a user with a password.
     *
     * @param username username
     * @param password password (optional)
     */
    public User(String username, String password) {
        this.username = username;
        this.password = password == null ? "" : password;
    }

    /**
     * Get the user's username.
     *
     * @return username string
     */
    public String getUsername() { return username; }

    /**
     * Check whether the provided password matches the user's password.
     *
     * @param p candidate password
     * @return true if the password matches
     */
    public boolean checkPassword(String p) { return password.equals(p); }

    /**
     * @return map of album name to Album instances owned by this user
     */
    public Map<String, Album> getAlbums() { return albums; }

    /**
     * Create a new album for this user.
     *
     * @param name album name
     * @return true if created, false if album already exists
     */
    public boolean createAlbum(String name) {
        if (albums.containsKey(name)) return false;
        albums.put(name, new Album(name));
        return true;
    }

    /**
     * Delete an album owned by this user. Deleting the built-in stock album
     * is blocked when performed on the special "stock" user.
     *
     * @param name album name
     * @return true if deleted
     */
    public boolean deleteAlbum(String name) {
        // Prevent deletion of the built-in stock album for the stock user
        if (this.username != null && this.username.equalsIgnoreCase("stock") && "stock".equals(name)) {
            return false;
        }
        return albums.remove(name) != null;
    }

    /**
     * Rename an album for this user. Renaming the built-in stock album is
     * blocked for the special "stock" user.
     *
     * @param oldName old album name
     * @param newName new album name
     * @return true if rename succeeded
     */
    public boolean renameAlbum(String oldName, String newName) {
        // Prevent renaming the built-in stock album for the stock user
        if (this.username != null && this.username.equalsIgnoreCase("stock") && "stock".equals(oldName)) {
            return false;
        }
        if (!albums.containsKey(oldName) || albums.containsKey(newName)) return false;
        Album a = albums.remove(oldName);
        a.setName(newName);
        albums.put(newName, a);
        return true;
    }
}
