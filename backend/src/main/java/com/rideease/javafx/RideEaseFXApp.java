package com.rideease.javafx;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * RideEase Unified JavaFX Desktop Dispatch & Operations Application
 * Features:
 * - Tab 1: Rider Booking Console (Port 8080 simulation)
 * - Tab 2: Driver Partner Terminal (Port 8081 simulation)
 * - Tab 3: Fleet Operations & Analytics (Port 8082 simulation)
 * - Tab 4: Compliance Dossier & KYC Document Inspection
 * - Responsive layout containers that adapt to all monitor aspect ratios.
 */
public class RideEaseFXApp extends Application {

    private static Stage primaryStage;

    @Override
    public void start(Stage stage) {
        primaryStage = stage;
        primaryStage.setTitle("RideEase Unified Command Console (JavaFX Desktop)");

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/javafx/MainConsoleView.fxml"));
            Parent root = loader.load();

            Scene scene = new Scene(root, 1200, 800);
            primaryStage.setScene(scene);
            primaryStage.setMinWidth(960);
            primaryStage.setMinHeight(650);

            primaryStage.setOnCloseRequest(event -> {
                Platform.exit();
                System.exit(0);
            });

            primaryStage.show();
        } catch (Exception ex) {
            System.err.println("CRITICAL: Failed to bootstrap JavaFX Dispatch Console: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
