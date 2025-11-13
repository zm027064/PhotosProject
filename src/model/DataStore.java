package model;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * DataStore manages users and serialization.
 *
 * <p>Provides a singleton access point to the persisted set of users and
 * handles loading/saving of the datastore to {@code data/users.dat}.
 * The datastore is responsible for ensuring the special built-in
 * "stock" user and album are present on startup.</p>
 *
 * @author Prayrit
 */
public class DataStore implements Serializable {
    private static final long serialVersionUID = 4L;

    private static final File DATA_DIR = new File("data");
    private static final File USERS_FILE = new File(DATA_DIR, "users.dat");

    private Map<String, User> users = new LinkedHashMap<>();

    private static DataStore instance;

    private DataStore() {}

    /**
     * Obtain the singleton DataStore instance, loading from disk if needed.
     *
     * @return the shared DataStore instance
     */
    public static synchronized DataStore getInstance() {
        if (instance == null) instance = loadOrCreate();
        return instance;
    }

    /**
     * Obtain the singleton DataStore instance, loading from disk if needed.
     *
     * @return the shared DataStore instance
     */

    private static DataStore loadOrCreate() {
        try {
            if (!DATA_DIR.exists()) DATA_DIR.mkdirs();
            if (USERS_FILE.exists()) {
                try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(USERS_FILE))) {
                    Object o = ois.readObject();
                    if (o instanceof DataStore) {
                        DataStore ds = (DataStore) o;
                        // Ensure stock user/album exist even when loading from an existing datastore
                        ds.ensureStock();
                        return ds;
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to load datastore, starting fresh: " + e.getMessage());
        }
        DataStore ds = new DataStore();
        ds.ensureStock();
        return ds;
    }

    /**
     * Ensure that the special built-in "stock" user and its "stock" album
     * exist and that images from the {@code data/stock} directory are loaded
     * into that album as relative paths.
     */
    private void ensureStock() {
        // Always ensure a 'stock' user and 'stock' album exist. Load images from data/stock if present.
        System.out.println("Checking for stock user...");
        User stock = users.get("stock"); // Case-sensitive lookup is fine here since we control the key
        if (stock == null) {
            System.out.println("Creating stock user...");
            stock = new User("stock", "stock");
            users.put("stock", stock);
            try {
                save(); // Save immediately after creating stock user
                System.out.println("Saved stock user to disk.");
            } catch (Exception e) {
                System.err.println("Failed to save after creating stock user: " + e.getMessage());
            }
        } else {
            System.out.println("Found existing stock user.");
        }
        boolean modified = false;
        if (!stock.getAlbums().containsKey("stock")) {
            System.out.println("Creating stock album...");
            stock.createAlbum("stock");
            modified = true;
        }
        Album a = stock.getAlbums().get("stock");

        File stockDir = new File(DATA_DIR, "stock");
        if (stockDir.exists() && stockDir.isDirectory()) {
            int photoCount = 0;
            try (DirectoryStream<Path> ds = Files.newDirectoryStream(stockDir.toPath())) {
                for (Path p : ds) {
                    String lower = p.toString().toLowerCase();
                    if (lower.endsWith(".jpg") || lower.endsWith(".jpeg") || lower.endsWith(".png") || lower.endsWith(".gif") || lower.endsWith(".bmp")) {
                        // Use relative path for stock photos: "data/stock/filename"
                        String relativePath = "data" + File.separator + "stock" + File.separator + p.getFileName().toString();
                        boolean exists = a.getPhotos().stream().anyMatch(ph -> ph.getFilePath().equals(relativePath));
                        if (!exists) {
                            Photo photo = new Photo(relativePath);
                            a.addPhoto(photo);
                            photoCount++;
                            modified = true;
                        }
                    }
                }
                // Validate stock photo count
                if (photoCount < 5 || photoCount > 10) {
                    System.err.println("Warning: Stock photos directory must contain between 5-10 photos (found " + photoCount + ")");
                }
                if (modified) {
                    try { save(); System.out.println("Saved datastore after adding stock photos/album."); } catch (Exception e) { System.err.println("Failed saving datastore after ensureStock: " + e.getMessage()); }
                }
            } catch (Exception ex) {
                System.err.println("Error loading stock photos: " + ex.getMessage());
            }
        }
    }

    public synchronized void save() throws Exception {
    /**
     * Persist the datastore to disk at {@code data/users.dat}.
     *
     * @throws Exception if an I/O error occurs during serialization
     */
        if (!DATA_DIR.exists()) DATA_DIR.mkdirs();
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(USERS_FILE))) {
            oos.writeObject(this);
        }
    }

    /**
     * Persist the datastore to disk at {@code data/users.dat}.
     *
     * @throws Exception if an I/O error occurs during serialization
     */

    /**
     * Return an unmodifiable view of the stored users map.
     *
     * @return unmodifiable map of username -> User
     */
    public synchronized Map<String, User> getUsers() { return Collections.unmodifiableMap(users); }

    /**
     * Lookup a user by username (case-insensitive).
     *
     * @param username username to lookup
     * @return User instance or null if not found
     */
    public synchronized User getUser(String username) { 
        if (username == null) return null;
        return users.get(username.toLowerCase());
    }

    /**
     * Add a user to the datastore. Username uniqueness is case-insensitive.
     *
     * @param u user to add
     * @return true if added; false if a user with the same username exists
     */
    public synchronized boolean addUser(User u) {
        String username = u.getUsername().toLowerCase(); // Store with lowercase key
        if (users.containsKey(username)) return false;
        users.put(username, u);
        return true;
    }

    /**
     * Delete a user by username (case-insensitive).
     *
     * @param username username to delete
     * @return true if user was present and removed; false otherwise
     */
    public synchronized boolean deleteUser(String username) {
        if (username == null) return false;
        if (!users.containsKey(username.toLowerCase())) return false;
        users.remove(username.toLowerCase());
        return true;
    }
}
