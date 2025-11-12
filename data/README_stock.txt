Place 5-10 stock images into the `data/stock` directory (create it if missing).
Supported image formats: .jpg, .jpeg, .png, .gif, .bmp

The application will read these files on startup and create a user named `stock` with an album named `stock`.

Run (example PowerShell commands):
# Compile
javac --module-path "path\to\javafx-sdk-21\lib" --add-modules javafx.controls,javafx.fxml -d bin @sources.txt
# Run
java --module-path "path\to\javafx-sdk-21\lib" --add-modules javafx.controls,javafx.fxml -cp bin Photos

Replace path\to\javafx-sdk-21 with your JavaFX SDK path. You can also run from your IDE (configure VM options to add JavaFX modules).
