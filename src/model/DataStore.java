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
 */
public class DataStore implements Serializable {
    private static final long serialVersionUID = 4L;

    private static final File DATA_DIR = new File("data");
    private static final File USERS_FILE = new File(DATA_DIR, "users.dat");

    private Map<String, User> users = new LinkedHashMap<>();

    private static DataStore instance;

    private DataStore() {}

    public static synchronized DataStore getInstance() {
        if (instance == null) instance = loadOrCreate();
        return instance;
    }

    private static DataStore loadOrCreate() {
        try {
            if (!DATA_DIR.exists()) DATA_DIR.mkdirs();
            if (USERS_FILE.exists()) {
                try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(USERS_FILE))) {
                    Object o = ois.readObject();
                    if (o instanceof DataStore) return (DataStore)o;
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to load datastore, starting fresh: " + e.getMessage());
        }
        DataStore ds = new DataStore();
        ds.ensureStock();
        return ds;
    }

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
        if (!stock.getAlbums().containsKey("stock")) {
            System.out.println("Creating stock album...");
            stock.createAlbum("stock");
        }
        Album a = stock.getAlbums().get("stock");

        File stockDir = new File(DATA_DIR, "stock");
        if (stockDir.exists() && stockDir.isDirectory()) {
            int photoCount = 0;
            try (DirectoryStream<Path> ds = Files.newDirectoryStream(stockDir.toPath())) {
                for (Path p : ds) {
                    String s = p.toAbsolutePath().toString();
                    String lower = s.toLowerCase();
                    if (lower.endsWith(".jpg") || lower.endsWith(".jpeg") || lower.endsWith(".png") || lower.endsWith(".gif") || lower.endsWith(".bmp")) {
                        boolean exists = a.getPhotos().stream().anyMatch(ph -> ph.getFilePath().equals(s));
                        if (!exists) {
                            Photo photo = new Photo(s);
                            a.addPhoto(photo);
                            photoCount++;
                        }
                    }
                }
                // Validate stock photo count
                if (photoCount < 5 || photoCount > 10) {
                    System.err.println("Warning: Stock photos directory must contain between 5-10 photos (found " + photoCount + ")");
                }
            } catch (Exception ex) {
                System.err.println("Error loading stock photos: " + ex.getMessage());
            }
        }
    }

    public synchronized void save() throws Exception {
        if (!DATA_DIR.exists()) DATA_DIR.mkdirs();
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(USERS_FILE))) {
            oos.writeObject(this);
        }
    }

    public synchronized Map<String, User> getUsers() { return Collections.unmodifiableMap(users); }

    public synchronized User getUser(String username) { 
        // Case-insensitive lookup
        return users.get(username.toLowerCase());
    }

    public synchronized boolean addUser(User u) {
        String username = u.getUsername().toLowerCase(); // Store with lowercase key
        if (users.containsKey(username)) return false;
        users.put(username, u);
        return true;
    }

    public synchronized boolean deleteUser(String username) {
        if (!users.containsKey(username)) return false;
        users.remove(username);
        return true;
    }
}
