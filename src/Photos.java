import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import model.DataStore;

/**
 * Photos application launcher required by the assignment.
 */
public class Photos extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Ensure datastore initialized
        DataStore.getInstance();

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/controllers/login.fxml"));
        Parent root = loader.load();
        primaryStage.setTitle("Photos - Login");
        primaryStage.setScene(new Scene(root));
        primaryStage.show();

        // save data on close
        primaryStage.setOnCloseRequest(ev -> {
            try { DataStore.getInstance().save(); } catch (Exception e) { e.printStackTrace(); }
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}
