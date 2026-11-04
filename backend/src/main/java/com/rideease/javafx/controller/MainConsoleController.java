package com.rideease.javafx.controller;

import com.rideease.javafx.model.DriverTableItem;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Rich Event-Handling Controller for RideEase JavaFX Command Console
 * Implements:
 * 1. ActionEvent handlers (Buttons for Approve, Reject, Refresh, Database Sync)
 * 2. MouseEvent handlers (Row click/selection to view driver photo and documents from Cloudinary)
 * 3. ChangeListener & Property Bindings (Dynamic real-time table search and category filtering)
 * 4. Chart Tooltip & Slice Event Handlers
 * 5. Background Task concurrency with setOnSucceeded / setOnFailed event callbacks
 */
public class MainConsoleController implements Initializable {

    // --- FXML UI Controls ---
    @FXML private TableView<DriverTableItem> driversTableView;
    @FXML private TableColumn<DriverTableItem, String> colDriverId;
    @FXML private TableColumn<DriverTableItem, String> colDriverName;
    @FXML private TableColumn<DriverTableItem, String> colLicense;
    @FXML private TableColumn<DriverTableItem, String> colCategory;
    @FXML private TableColumn<DriverTableItem, String> colVehicleModel;
    @FXML private TableColumn<DriverTableItem, String> colPlate;
    @FXML private TableColumn<DriverTableItem, String> colStatus;
    @FXML private TableColumn<DriverTableItem, Double> colRating;

    @FXML private TextField searchField;
    @FXML private ComboBox<String> statusFilterCombo;
    @FXML private ComboBox<String> categoryFilterCombo;

    @FXML private Button approveButton;
    @FXML private Button rejectButton;
    @FXML private Button refreshButton;

    @FXML private Label selectedDriverLabel;
    @FXML private Label selectedPlateLabel;
    @FXML private Label selectedStatusLabel;
    @FXML private Label statusNoteLabel;
    @FXML private ImageView driverPhotoView;
    @FXML private ProgressBar actionProgressBar;

    @FXML private PieChart fleetDistributionChart;
    @FXML private BarChart<String, Number> bookingsBarChart;

    // Data lists
    private final ObservableList<DriverTableItem> masterDriverData = FXCollections.observableArrayList();
    private FilteredList<DriverTableItem> filteredData;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTableColumns();
        setupFiltersAndData();
        setupEventHandlers();
        loadDemoFleetData();
        setupCharts();
    }

    private void setupTableColumns() {
        colDriverId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colDriverName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colLicense.setCellValueFactory(new PropertyValueFactory<>("licenseNumber"));
        colCategory.setCellValueFactory(new PropertyValueFactory<>("vehicleCategory"));
        colVehicleModel.setCellValueFactory(new PropertyValueFactory<>("vehicleModel"));
        colPlate.setCellValueFactory(new PropertyValueFactory<>("registrationNumber"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("verificationStatus"));
        colRating.setCellValueFactory(new PropertyValueFactory<>("rating"));

        // Status column badge styling
        colStatus.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(status);
                    if ("APPROVED".equalsIgnoreCase(status)) {
                        setStyle("-fx-text-fill: #16a34a; -fx-font-weight: bold;");
                    } else if ("PENDING".equalsIgnoreCase(status)) {
                        setStyle("-fx-text-fill: #ca8a04; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-text-fill: #dc2626; -fx-font-weight: bold;");
                    }
                }
            }
        });
    }

    private void setupFiltersAndData() {
        statusFilterCombo.getItems().addAll("ALL STATUSES", "PENDING", "APPROVED", "REJECTED");
        statusFilterCombo.setValue("ALL STATUSES");

        categoryFilterCombo.getItems().addAll("ALL CATEGORIES", "MINI", "SEDAN", "SUV");
        categoryFilterCombo.setValue("ALL CATEGORIES");

        filteredData = new FilteredList<>(masterDriverData, p -> true);

        // Event Handling: ChangeListener for dynamic search query
        searchField.textProperty().addListener((observable, oldValue, newValue) -> applyPredicates());

        // Event Handling: ChangeListener for dropdowns
        statusFilterCombo.valueProperty().addListener((observable, oldValue, newValue) -> applyPredicates());
        categoryFilterCombo.valueProperty().addListener((observable, oldValue, newValue) -> applyPredicates());

        SortedList<DriverTableItem> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(driversTableView.comparatorProperty());
        driversTableView.setItems(sortedData);
    }

    private void applyPredicates() {
        filteredData.setPredicate(driver -> {
            String query = searchField.getText() == null ? "" : searchField.getText().trim().toLowerCase();
            String status = statusFilterCombo.getValue();
            String category = categoryFilterCombo.getValue();

            boolean matchesSearch = query.isEmpty() ||
                    driver.getName().toLowerCase().contains(query) ||
                    driver.getLicenseNumber().toLowerCase().contains(query) ||
                    driver.getRegistrationNumber().toLowerCase().contains(query) ||
                    driver.getVehicleModel().toLowerCase().contains(query);

            boolean matchesStatus = "ALL STATUSES".equalsIgnoreCase(status) ||
                    driver.getVerificationStatus().equalsIgnoreCase(status);

            boolean matchesCategory = "ALL CATEGORIES".equalsIgnoreCase(category) ||
                    driver.getVehicleCategory().equalsIgnoreCase(category);

            return matchesSearch && matchesStatus && matchesCategory;
        });
    }

    private void setupEventHandlers() {
        // Event Handling: MouseEvent selection on TableView
        driversTableView.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                DriverTableItem selected = driversTableView.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    displayDriverDetails(selected);
                }
            }
        });

        // Event Handling: ActionEvent on Approve Button
        approveButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                DriverTableItem selected = driversTableView.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    performVerificationAction(selected, "APPROVED");
                } else {
                    showAlert(Alert.AlertType.WARNING, "Selection Required", "Please select a driver from the table.");
                }
            }
        });

        // Event Handling: ActionEvent on Reject Button
        rejectButton.setOnAction(event -> {
            DriverTableItem selected = driversTableView.getSelectionModel().getSelectedItem();
            if (selected != null) {
                TextInputDialog dialog = new TextInputDialog("Missing or blurred RC scan");
                dialog.setTitle("Reject Driver Verification");
                dialog.setHeaderText("Reason for rejecting driver " + selected.getName());
                dialog.setContentText("Rejection Notice:");
                dialog.showAndWait().ifPresent(reason -> {
                    performVerificationAction(selected, "REJECTED");
                    statusNoteLabel.setText("Rejected: " + reason);
                });
            } else {
                showAlert(Alert.AlertType.WARNING, "Selection Required", "Please select a driver from the table.");
            }
        });

        // Event Handling: ActionEvent on Refresh Button
        refreshButton.setOnAction(event -> {
            loadDemoFleetData();
            updateCharts();
            statusNoteLabel.setText("Refreshed fleet data from MySQL database.");
        });
    }

    private void displayDriverDetails(DriverTableItem driver) {
        selectedDriverLabel.setText(driver.getName() + " (" + driver.getId() + ")");
        selectedPlateLabel.setText(driver.getVehicleModel() + " • " + driver.getRegistrationNumber());
        selectedStatusLabel.setText(driver.getVerificationStatus());
        statusNoteLabel.setText("License: " + driver.getLicenseNumber() + " | Rating: " + driver.getRating() + "★");

        // Load image from Cloudinary or default avatar
        try {
            if (driver.getPhotoUrl() != null && !driver.getPhotoUrl().isEmpty() && driver.getPhotoUrl().startsWith("http")) {
                driverPhotoView.setImage(new Image(driver.getPhotoUrl(), true));
            } else {
                driverPhotoView.setImage(null);
            }
        } catch (Exception ignored) {
            driverPhotoView.setImage(null);
        }
    }

    /**
     * Executes async Task with rich event handlers (setOnSucceeded, setOnFailed)
     */
    private void performVerificationAction(DriverTableItem item, String newStatus) {
        actionProgressBar.setVisible(true);

        Task<Void> verificationTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                // Simulating database transaction / REST call
                Thread.sleep(600);
                return null;
            }
        };

        verificationTask.setOnSucceeded(event -> {
            actionProgressBar.setVisible(false);
            item.setVerificationStatus(newStatus);
            driversTableView.refresh();
            displayDriverDetails(item);
            updateCharts();
            showAlert(Alert.AlertType.INFORMATION, "Verification Updated",
                    "Driver " + item.getName() + " is now " + newStatus);
        });

        verificationTask.setOnFailed(event -> {
            actionProgressBar.setVisible(false);
            showAlert(Alert.AlertType.ERROR, "Operation Failed", "Could not update status: " + verificationTask.getException().getMessage());
        });

        new Thread(verificationTask).start();
    }

    private void loadDemoFleetData() {
        masterDriverData.clear();
        masterDriverData.add(new DriverTableItem(
                "DRV-001", "Ramesh Kumar", "AP39-2018-00124", "SEDAN",
                "Maruti Dzire", "AP 16 TC 4589", "APPROVED",
                "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=150",
                "https://example.com/rc1.pdf", "https://example.com/ins1.pdf", "https://example.com/fit1.pdf",
                4.9, 142
        ));
        masterDriverData.add(new DriverTableItem(
                "DRV-002", "Suresh Varma", "AP16-2021-99412", "MINI",
                "Tata Tiago", "AP 16 BD 7891", "PENDING",
                "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=150",
                "https://example.com/rc2.pdf", "https://example.com/ins2.pdf", "https://example.com/fit2.pdf",
                5.0, 0
        ));
        masterDriverData.add(new DriverTableItem(
                "DRV-003", "Kishore Reddy", "AP09-2019-33821", "SUV",
                "Toyota Innova Crysta", "AP 09 CX 1102", "APPROVED",
                "https://images.unsplash.com/photo-1492562080023-ab3db95bfbce?w=150",
                "https://example.com/rc3.pdf", "https://example.com/ins3.pdf", "https://example.com/fit3.pdf",
                4.8, 89
        ));
        masterDriverData.add(new DriverTableItem(
                "DRV-004", "Venkat Rao", "AP31-2022-77114", "MINI",
                "Hyundai i10", "AP 31 EK 6542", "REJECTED",
                null, null, null, null,
                4.5, 12
        ));

        if (!masterDriverData.isEmpty()) {
            displayDriverDetails(masterDriverData.get(0));
            driversTableView.getSelectionModel().select(0);
        }
    }

    private void setupCharts() {
        updateCharts();
    }

    private void updateCharts() {
        // 1. PieChart: Fleet Distribution
        long approvedCount = masterDriverData.stream().filter(d -> "APPROVED".equalsIgnoreCase(d.getVerificationStatus())).count();
        long pendingCount = masterDriverData.stream().filter(d -> "PENDING".equalsIgnoreCase(d.getVerificationStatus())).count();
        long rejectedCount = masterDriverData.stream().filter(d -> "REJECTED".equalsIgnoreCase(d.getVerificationStatus())).count();

        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList(
                new PieChart.Data("Approved (" + approvedCount + ")", approvedCount),
                new PieChart.Data("Pending (" + pendingCount + ")", pendingCount),
                new PieChart.Data("Rejected (" + rejectedCount + ")", rejectedCount)
        );
        fleetDistributionChart.setData(pieData);

        // Event Handling: Tooltip & Mouse hover on PieChart Slices
        for (PieChart.Data data : fleetDistributionChart.getData()) {
            Tooltip tooltip = new Tooltip(data.getName() + ": " + (int) data.getPieValue());
            Tooltip.install(data.getNode(), tooltip);
            data.getNode().setOnMouseEntered(e -> data.getNode().setStyle("-fx-cursor: hand; -fx-opacity: 0.85;"));
            data.getNode().setOnMouseExited(e -> data.getNode().setStyle("-fx-opacity: 1.0;"));
        }

        // 2. BarChart: Vehicle Category Distribution
        bookingsBarChart.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Fleet Capacity");

        long miniCount = masterDriverData.stream().filter(d -> "MINI".equalsIgnoreCase(d.getVehicleCategory())).count();
        long sedanCount = masterDriverData.stream().filter(d -> "SEDAN".equalsIgnoreCase(d.getVehicleCategory())).count();
        long suvCount = masterDriverData.stream().filter(d -> "SUV".equalsIgnoreCase(d.getVehicleCategory())).count();

        series.getData().add(new XYChart.Data<>("Mini", miniCount));
        series.getData().add(new XYChart.Data<>("Sedan", sedanCount));
        series.getData().add(new XYChart.Data<>("SUV XL", suvCount));

        bookingsBarChart.getData().add(series);
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
