import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import model.DataStore;

/**
 * Photos application launcher required by the assignment.
 * <p>Starts the JavaFX application and ensures the datastore is
 * initialized and persisted on shutdown.</p>
 *
 * @author Zach
 */
public class Photos extends Application {

    /**
     * Application entry point (JavaFX start method).
     *
     * @param primaryStage primary stage provided by JavaFX
     * @throws Exception if loading the UI fails
     */
    @Override
    public void start(Stage primaryStage) throws Exception {
        // Ensure datastore initialized
        DataStore.getInstance();

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/controllers/Login_Controller.fxml"));
        Parent root = loader.load();
        primaryStage.setTitle("Photos - Login");
        primaryStage.setScene(new Scene(root));
        primaryStage.show();

        // save data on close
        primaryStage.setOnCloseRequest(ev -> {
            try { DataStore.getInstance().save(); } catch (Exception e) { e.printStackTrace(); }
        });
    }

    /**
     * Launch the JavaFX application.
     *
     * @param args command-line args
     */
    public static void main(String[] args) {
        launch(args);
    }
}
