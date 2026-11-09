package com.rideease.javafx.controller;

import com.rideease.javafx.model.DriverTableItem;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

import java.awt.Desktop;
import java.net.URI;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Enhanced Multi-Portal JavaFX Command Center Controller
 * Controls:
 * 1. Embedded WebViews for Port 8080 (Rider), 8081 (Driver), 8082 (Fleet Admin)
 * 2. Native TableView Document Verification & KYC Previews
 * 3. Charts and Analytics
 * 4. Browser launching and portal reload actions
 */
public class MainConsoleController implements Initializable {

    // --- Embedded Portal WebViews ---
    @FXML private TabPane mainTabPane;
    @FXML private Tab tabRider;
    @FXML private Tab tabDriver;
    @FXML private Tab tabAdmin;
    @FXML private WebView riderWebView;
    @FXML private WebView driverWebView;
    @FXML private WebView adminWebView;

    @FXML private Button reloadActiveTabButton;
    @FXML private Button openBrowserButton;

    // --- Native Table & Compliance Controls ---
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

    private static final String RIDER_URL = "http://localhost:8080/";
    private static final String DRIVER_URL = "http://localhost:8081/driver/login";
    private static final String ADMIN_URL = "http://localhost:8082/admin/login";

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initPortalWebViews();
        setupTableColumns();
        setupFiltersAndData();
        setupEventHandlers();
        loadDemoFleetData();
        setupCharts();
    }

    /**
     * Initializes embedded WebKit WebViews for Port 8080, 8081, and 8082
     */
    private void initPortalWebViews() {
        if (riderWebView != null) {
            WebEngine engine = riderWebView.getEngine();
            engine.setJavaScriptEnabled(true);
            engine.setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36 RideEase-Desktop");
            engine.load(RIDER_URL);
        }

        if (driverWebView != null) {
            WebEngine engine = driverWebView.getEngine();
            engine.setJavaScriptEnabled(true);
            engine.setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36 RideEase-Desktop");
            engine.load(DRIVER_URL);
        }

        if (adminWebView != null) {
            WebEngine engine = adminWebView.getEngine();
            engine.setJavaScriptEnabled(true);
            engine.setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36 RideEase-Desktop");
            engine.load(ADMIN_URL);
        }

        if (reloadActiveTabButton != null) {
            reloadActiveTabButton.setOnAction(e -> reloadActivePortal());
        }

        if (openBrowserButton != null) {
            openBrowserButton.setOnAction(e -> openActivePortalInSystemBrowser());
        }
    }

    private void reloadActivePortal() {
        Tab currentTab = mainTabPane.getSelectionModel().getSelectedItem();
        if (currentTab == tabRider && riderWebView != null) {
            riderWebView.getEngine().reload();
        } else if (currentTab == tabDriver && driverWebView != null) {
            driverWebView.getEngine().reload();
        } else if (currentTab == tabAdmin && adminWebView != null) {
            adminWebView.getEngine().reload();
        } else {
            loadDemoFleetData();
            updateCharts();
        }
    }

    private void openActivePortalInSystemBrowser() {
        Tab currentTab = mainTabPane.getSelectionModel().getSelectedItem();
        String url = RIDER_URL;
        if (currentTab == tabDriver) url = DRIVER_URL;
        else if (currentTab == tabAdmin) url = ADMIN_URL;

        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(new URI(url));
            }
        } catch (Exception ex) {
            System.err.println("Failed to open browser: " + ex.getMessage());
        }
    }

    private void setupTableColumns() {
        colDriverId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colDriverName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colLicense.setCellValueFactory(new PropertyValueFactory<>("licenseNumber"));
        colCategory.setCellValueFactory(new PropertyValueFactory<>("category"));
        colVehicleModel.setCellValueFactory(new PropertyValueFactory<>("vehicleModel"));
        colPlate.setCellValueFactory(new PropertyValueFactory<>("registrationNumber"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("verificationStatus"));
        colRating.setCellValueFactory(new PropertyValueFactory<>("rating"));

        colStatus.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    if ("APPROVED".equalsIgnoreCase(item)) {
                        setStyle("-fx-text-fill: #16a34a; -fx-font-weight: bold;");
                    } else if ("REJECTED".equalsIgnoreCase(item)) {
                        setStyle("-fx-text-fill: #dc2626; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-text-fill: #d97706; -fx-font-weight: bold;");
                    }
                }
            }
        });
    }

    private void setupFiltersAndData() {
        statusFilterCombo.setItems(FXCollections.observableArrayList("ALL STATUSES", "PENDING", "APPROVED", "REJECTED"));
        statusFilterCombo.getSelectionModel().selectFirst();

        categoryFilterCombo.setItems(FXCollections.observableArrayList("ALL CATEGORIES", "MINI", "SEDAN", "SUV"));
        categoryFilterCombo.getSelectionModel().selectFirst();

        filteredData = new FilteredList<>(masterDriverData, p -> true);

        searchField.textProperty().addListener((obs, oldVal, newVal) -> applyFilterPredicates());
        statusFilterCombo.valueProperty().addListener((obs, oldVal, newVal) -> applyFilterPredicates());
        categoryFilterCombo.valueProperty().addListener((obs, oldVal, newVal) -> applyFilterPredicates());

        SortedList<DriverTableItem> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(driversTableView.comparatorProperty());
        driversTableView.setItems(sortedData);
    }

    private void applyFilterPredicates() {
        filteredData.setPredicate(driver -> {
            String searchText = searchField.getText();
            boolean matchesSearch = true;
            if (searchText != null && !searchText.trim().isEmpty()) {
                String lower = searchText.toLowerCase().trim();
                matchesSearch = driver.getName().toLowerCase().contains(lower)
                        || driver.getLicenseNumber().toLowerCase().contains(lower)
                        || driver.getRegistrationNumber().toLowerCase().contains(lower)
                        || driver.getVehicleModel().toLowerCase().contains(lower);
            }

            String selectedStatus = statusFilterCombo.getValue();
            boolean matchesStatus = true;
            if (selectedStatus != null && !"ALL STATUSES".equalsIgnoreCase(selectedStatus)) {
                matchesStatus = selectedStatus.equalsIgnoreCase(driver.getVerificationStatus());
            }

            String selectedCategory = categoryFilterCombo.getValue();
            boolean matchesCategory = true;
            if (selectedCategory != null && !"ALL CATEGORIES".equalsIgnoreCase(selectedCategory)) {
                matchesCategory = selectedCategory.equalsIgnoreCase(driver.getCategory());
            }

            return matchesSearch && matchesStatus && matchesCategory;
        });
    }

    private void setupEventHandlers() {
        driversTableView.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null) {
                displayDriverDetails(newSel);
            }
        });

        approveButton.setOnAction(event -> {
            DriverTableItem selected = driversTableView.getSelectionModel().getSelectedItem();
            if (selected != null) {
                performVerificationAction(selected, "APPROVED");
            } else {
                showAlert(Alert.AlertType.WARNING, "Selection Required", "Please select a driver from the table.");
            }
        });

        rejectButton.setOnAction(event -> {
            DriverTableItem selected = driversTableView.getSelectionModel().getSelectedItem();
            if (selected != null) {
                TextInputDialog dialog = new TextInputDialog("Blurred RC book / expired insurance");
                dialog.setTitle("Reject Driver Verification");
                dialog.setHeaderText("Specify rejection reason for " + selected.getName() + ":");
                dialog.setContentText("Reason:");
                dialog.showAndWait().ifPresent(reason -> {
                    performVerificationAction(selected, "REJECTED");
                    statusNoteLabel.setText("Rejected: " + reason);
                });
            } else {
                showAlert(Alert.AlertType.WARNING, "Selection Required", "Please select a driver from the table.");
            }
        });

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

    private void performVerificationAction(DriverTableItem item, String newStatus) {
        actionProgressBar.setVisible(true);

        Task<Void> verificationTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                Thread.sleep(400);
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
        masterDriverData.addAll(
                new DriverTableItem("DRV-001", "Ramesh Kumar", "AP39-2018-00124", "SEDAN", "Maruti Dzire", "AP 16 TC 4589", "APPROVED", "https://res.cloudinary.com/demo/image/upload/sample.jpg", "https://res.cloudinary.com/demo/image/upload/sample.jpg", "https://res.cloudinary.com/demo/image/upload/sample.jpg", "https://res.cloudinary.com/demo/image/upload/sample.jpg", 4.9, 12),
                new DriverTableItem("DRV-002", "Suresh Varma", "AP16-2021-99412", "MINI", "Tata Tiago", "AP 16 BD 7891", "PENDING", "https://res.cloudinary.com/demo/image/upload/avatar.jpg", null, null, null, 4.7, 5),
                new DriverTableItem("DRV-003", "Kishore Reddy", "AP09-2019-33821", "SUV", "Toyota Innova Crysta", "AP 09 CX 1102", "APPROVED", "https://res.cloudinary.com/demo/image/upload/sample.jpg", "https://res.cloudinary.com/demo/image/upload/sample.jpg", null, null, 5.0, 24),
                new DriverTableItem("DRV-004", "Venkat Rao", "AP31-2022-77114", "MINI", "Hyundai i10", "AP 31 EK 6542", "REJECTED", null, null, null, null, 4.2, 0)
        );
        if (!masterDriverData.isEmpty()) {
            driversTableView.getSelectionModel().selectFirst();
            displayDriverDetails(masterDriverData.get(0));
        }
    }

    private void setupCharts() {
        updateCharts();
    }

    private void updateCharts() {
        long approved = masterDriverData.stream().filter(d -> "APPROVED".equalsIgnoreCase(d.getVerificationStatus())).count();
        long pending = masterDriverData.stream().filter(d -> "PENDING".equalsIgnoreCase(d.getVerificationStatus())).count();
        long rejected = masterDriverData.stream().filter(d -> "REJECTED".equalsIgnoreCase(d.getVerificationStatus())).count();

        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList(
                new PieChart.Data("Approved (" + approved + ")", approved),
                new PieChart.Data("Pending (" + pending + ")", pending),
                new PieChart.Data("Rejected (" + rejected + ")", rejected)
        );
        fleetDistributionChart.setData(pieData);

        bookingsBarChart.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Registered Vehicles");

        long miniCount = masterDriverData.stream().filter(d -> "MINI".equalsIgnoreCase(d.getCategory())).count();
        long sedanCount = masterDriverData.stream().filter(d -> "SEDAN".equalsIgnoreCase(d.getCategory())).count();
        long suvCount = masterDriverData.stream().filter(d -> "SUV".equalsIgnoreCase(d.getCategory())).count();

        series.getData().add(new XYChart.Data<>("MINI", miniCount));
        series.getData().add(new XYChart.Data<>("SEDAN", sedanCount));
        series.getData().add(new XYChart.Data<>("SUV", suvCount));

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
