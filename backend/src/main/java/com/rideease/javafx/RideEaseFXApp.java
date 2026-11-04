package com.rideease.javafx;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

/**
 * RideEase JavaFX Desktop Administrative & Fleet Management Application
 * Features rich Java Event Handling:
 * - ActionEvent handlers on interactive controls (verification approval, rejection, filters)
 * - MouseEvent click/double-click handlers on TableView rows to inspect driver Cloudinary documents
 * - ChangeListener and Property bindings for dynamic real-time search & status filtering
 * - Background Task worker threads for network and database operations with setOnSucceeded / setOnFailed
 */
public class RideEaseFXApp extends Application {

    private static Stage primaryStage;

    @Override
    public void start(Stage stage) {
        primaryStage = stage;
        primaryStage.setTitle("RideEase Fleet & Verification Command Center (JavaFX Desktop)");

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/javafx/MainConsoleView.fxml"));
            Parent root = loader.load();

            Scene scene = new Scene(root, 1100, 720);
            primaryStage.setScene(scene);
            primaryStage.setMinWidth(950);
            primaryStage.setMinHeight(600);

            primaryStage.setOnCloseRequest(event -> {
                Platform.exit();
                System.exit(0);
            });

            primaryStage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
