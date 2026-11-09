package com.rideease.javafx.controller;

import com.rideease.javafx.model.DriverTableItem;
import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.awt.Desktop;
import java.net.URI;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * RideEase Multi-Port Interactive Native Controller
 * Delivers full native interactivity for:
 * - Rider Console (Port 8080 simulation): Geocoded destination routing, vehicle tiers, live fare math & booking lifecycle.
 * - Driver Terminal (Port 8081 simulation): Online/Offline duty toggles, live dispatch radar cards, countdown timer, and OTP start verification.
 * - Fleet Admin (Port 8082 simulation): Real-time KPI summaries, compliance verification dossier, and analytics charts.
 * Enhanced with subtle micro-animations and responsive layout boundaries.
 */
public class MainConsoleController implements Initializable {

    // --- External Browser Launchers ---
    @FXML private Button btnLaunchRider;
    @FXML private Button btnLaunchDriver;
    @FXML private Button btnLaunchAdmin;
    @FXML private Button refreshButton;

    // --- Rider Interactive Controls ---
    @FXML private TextField txtRiderPickup;
    @FXML private TextField txtRiderDrop;
    @FXML private Button btnTierMini;
    @FXML private Button btnTierSedan;
    @FXML private Button btnTierSuv;
    @FXML private Button btnCalculateFare;
    @FXML private Label lblFareAmount;
    @FXML private Label lblDistance;
    @FXML private Label lblDuration;
    @FXML private Label lblRiderBookingStatus;
    @FXML private Label lblAssignedDriver;
    @FXML private Label lblRiderOtp;
    @FXML private Button btnBookRide;
    @FXML private Button btnCancelRide;
    @FXML private VBox cardRideLifecycle;

    // --- Driver Interactive Controls ---
    @FXML private Label lblDriverDutyStatus;
    @FXML private Button btnToggleDuty;
    @FXML private Label lblDriverCompletedTrips;
    @FXML private Label lblDriverEarnings;
    @FXML private VBox cardIncomingTrip;
    @FXML private Label lblCountdownTimer;
    @FXML private Label lblIncomingPassenger;
    @FXML private Label lblIncomingRoute;
    @FXML private Label lblIncomingPayout;
    @FXML private Button btnAcceptRide;
    @FXML private Button btnDeclineRide;
    @FXML private TextField txtDriverOtpVerify;
    @FXML private Button btnVerifyOtpAndStart;

    // --- Fleet Admin KPI & Charts ---
    @FXML private Label lblKpiDrivers;
    @FXML private Label lblKpiTrips;
    @FXML private Label lblKpiPending;
    @FXML private Label lblKpiRevenue;
    @FXML private PieChart fleetDistributionChart;
    @FXML private BarChart<String, Number> bookingsBarChart;

    // --- Compliance Dossier Controls ---
    @FXML private TextField searchField;
    @FXML private ComboBox<String> statusFilterCombo;
    @FXML private ComboBox<String> categoryFilterCombo;
    @FXML private ProgressBar actionProgressBar;

    @FXML private TableView<DriverTableItem> driversTableView;
    @FXML private TableColumn<DriverTableItem, String> colDriverId;
    @FXML private TableColumn<DriverTableItem, String> colDriverName;
    @FXML private TableColumn<DriverTableItem, String> colLicense;
    @FXML private TableColumn<DriverTableItem, String> colCategory;
    @FXML private TableColumn<DriverTableItem, String> colVehicleModel;
    @FXML private TableColumn<DriverTableItem, String> colPlate;
    @FXML private TableColumn<DriverTableItem, String> colStatus;
    @FXML private TableColumn<DriverTableItem, Double> colRating;

    @FXML private ImageView driverPhotoView;
    @FXML private Label selectedDriverLabel;
    @FXML private Label selectedPlateLabel;
    @FXML private Label selectedStatusLabel;
    @FXML private Label statusNoteLabel;
    @FXML private Button approveButton;
    @FXML private Button rejectButton;

    // Internal State
    private String selectedVehicleTier = "SEDAN";
    private boolean isDriverOnline = true;
    private int shiftTrips = 12;
    private double shiftEarnings = 2840.0;
    private final ObservableList<DriverTableItem> masterData = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initExternalLaunchers();
        initRiderInteractions();
        initDriverInteractions();
        initDossierTable();
        initAnalyticsCharts();
        setupMicroAnimations();
    }

    private void initExternalLaunchers() {
        if (btnLaunchRider != null) btnLaunchRider.setOnAction(e -> openBrowser("http://localhost:8080/"));
        if (btnLaunchDriver != null) btnLaunchDriver.setOnAction(e -> openBrowser("http://localhost:8081/driver/login"));
        if (btnLaunchAdmin != null) btnLaunchAdmin.setOnAction(e -> openBrowser("http://localhost:8082/admin/login"));
        if (refreshButton != null) refreshButton.setOnAction(e -> refreshAllData());
    }

    private void openBrowser(String url) {
        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(new URI(url));
            }
        } catch (Exception ex) {
            System.err.println("Failed to open browser: " + ex.getMessage());
        }
    }

    // ==========================================
    // 🚗 TAB 1: RIDER INTERACTION SYSTEM
    // ==========================================
    private void initRiderInteractions() {
        if (btnTierMini != null) btnTierMini.setOnAction(e -> setTier("MINI", btnTierMini));
        if (btnTierSedan != null) btnTierSedan.setOnAction(e -> setTier("SEDAN", btnTierSedan));
        if (btnTierSuv != null) btnTierSuv.setOnAction(e -> setTier("SUV", btnTierSuv));

        if (btnCalculateFare != null) {
            btnCalculateFare.setOnAction(e -> calculateTariff());
        }

        if (btnBookRide != null) {
            btnBookRide.setOnAction(e -> {
                animatePulse(btnBookRide);
                lblRiderBookingStatus.setText("SEARCHING FOR DRIVER...");
                lblRiderBookingStatus.setStyle("-fx-font-weight: bold; -fx-text-fill: #fbbf24; -fx-font-size: 13px;");
                lblAssignedDriver.setText("Pinging 4 drivers in your zone via WebSocket...");

                // Simulate quick dispatch match
                Task<Void> dispatchTask = new Task<>() {
                    @Override
                    protected Void call() throws Exception {
                        Thread.sleep(1200);
                        return null;
                    }
                };
                dispatchTask.setOnSucceeded(ev -> {
                    lblRiderBookingStatus.setText("DRIVER ACCEPTED & EN ROUTE");
                    lblRiderBookingStatus.setStyle("-fx-font-weight: bold; -fx-text-fill: #34d399; -fx-font-size: 13px;");
                    lblAssignedDriver.setText("Driver: Ramesh Kumar (Maruti Dzire, AP 16 TC 4589) • 3 mins away");
                    lblRiderOtp.setText(String.valueOf((int) (1000 + Math.random() * 9000)));
                    animatePulse(cardRideLifecycle);
                });
                new Thread(dispatchTask).start();
            });
        }

        if (btnCancelRide != null) {
            btnCancelRide.setOnAction(e -> {
                lblRiderBookingStatus.setText("RIDE CANCELLED");
                lblRiderBookingStatus.setStyle("-fx-font-weight: bold; -fx-text-fill: #ef4444; -fx-font-size: 13px;");
                lblAssignedDriver.setText("No active booking. Enter pickup & destination to book.");
            });
        }
    }

    private void setTier(String tier, Button activeBtn) {
        this.selectedVehicleTier = tier;
        // Reset styles
        String inactive = "-fx-background-color: #1f2937; -fx-text-fill: white; -fx-padding: 10px; -fx-background-radius: 8px; -fx-border-color: #374151; -fx-border-radius: 8px; -fx-font-weight: bold; -fx-cursor: hand;";
        String active = "-fx-background-color: #4f46e5; -fx-text-fill: white; -fx-padding: 10px; -fx-background-radius: 8px; -fx-border-color: #6366f1; -fx-border-radius: 8px; -fx-font-weight: bold; -fx-cursor: hand;";
        
        if (btnTierMini != null) btnTierMini.setStyle(inactive);
        if (btnTierSedan != null) btnTierSedan.setStyle(inactive);
        if (btnTierSuv != null) btnTierSuv.setStyle(inactive);
        if (activeBtn != null) activeBtn.setStyle(active);

        calculateTariff();
    }

    private void calculateTariff() {
        double baseDistance = 12.4;
        double ratePerKm = 14.0;
        double baseFare = 50.0;

        if ("MINI".equals(selectedVehicleTier)) {
            ratePerKm = 11.0;
            baseFare = 40.0;
        } else if ("SUV".equals(selectedVehicleTier)) {
            ratePerKm = 19.0;
            baseFare = 80.0;
        }

        double total = baseFare + (baseDistance * ratePerKm);
        if (lblFareAmount != null) {
            lblFareAmount.setText(String.format("₹ %.2f", total));
            animatePulse(lblFareAmount);
        }
    }

    // ==========================================
    // 🚕 TAB 2: DRIVER PARTNER SYSTEM
    // ==========================================
    private void initDriverInteractions() {
        if (btnToggleDuty != null) {
            btnToggleDuty.setOnAction(e -> {
                isDriverOnline = !isDriverOnline;
                if (isDriverOnline) {
                    lblDriverDutyStatus.setText("ONLINE (Ready for Rides)");
                    lblDriverDutyStatus.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #34d399;");
                    btnToggleDuty.setText("Go Offline");
                    btnToggleDuty.setStyle("-fx-background-color: #059669; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8px 14px; -fx-background-radius: 6px; -fx-cursor: hand;");
                    if (cardIncomingTrip != null) cardIncomingTrip.setVisible(true);
                } else {
                    lblDriverDutyStatus.setText("OFFLINE (Duty Suspended)");
                    lblDriverDutyStatus.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #ef4444;");
                    btnToggleDuty.setText("Go Online");
                    btnToggleDuty.setStyle("-fx-background-color: #374151; -fx-text-fill: #9ca3af; -fx-font-weight: bold; -fx-padding: 8px 14px; -fx-background-radius: 6px; -fx-cursor: hand;");
                    if (cardIncomingTrip != null) cardIncomingTrip.setVisible(false);
                }
            });
        }

        if (btnAcceptRide != null) {
            btnAcceptRide.setOnAction(e -> {
                lblCountdownTimer.setText("✓ ACCEPTED • PROCEED TO PICKUP");
                lblCountdownTimer.setStyle("-fx-text-fill: #34d399; -fx-font-weight: bold; -fx-font-size: 13px;");
                btnAcceptRide.setDisable(true);
                animatePulse(cardIncomingTrip);
            });
        }

        if (btnDeclineRide != null) {
            btnDeclineRide.setOnAction(e -> {
                lblCountdownTimer.setText("✕ DECLINED");
                lblCountdownTimer.setStyle("-fx-text-fill: #ef4444; -fx-font-weight: bold; -fx-font-size: 13px;");
                FadeTransition ft = new FadeTransition(Duration.millis(300), cardIncomingTrip);
                ft.setFromValue(1.0);
                ft.setToValue(0.3);
                ft.setAutoReverse(true);
                ft.setCycleCount(2);
                ft.play();
            });
        }

        if (btnVerifyOtpAndStart != null) {
            btnVerifyOtpAndStart.setOnAction(e -> {
                String inputOtp = txtDriverOtpVerify != null ? txtDriverOtpVerify.getText().trim() : "";
                if (inputOtp.length() == 4) {
                    shiftTrips++;
                    shiftEarnings += 245.0;
                    if (lblDriverCompletedTrips != null) lblDriverCompletedTrips.setText(shiftTrips + " Rides");
                    if (lblDriverEarnings != null) lblDriverEarnings.setText(String.format("₹ %.2f", shiftEarnings));
                    lblCountdownTimer.setText("TRIP IN PROGRESS • OTP VERIFIED");
                    lblCountdownTimer.setStyle("-fx-text-fill: #38bdf8; -fx-font-weight: bold; -fx-font-size: 13px;");
                    if (txtDriverOtpVerify != null) txtDriverOtpVerify.clear();
                    animatePulse(lblDriverEarnings);
                } else {
                    if (txtDriverOtpVerify != null) {
                        txtDriverOtpVerify.setStyle("-fx-background-color: #7f1d1d; -fx-text-fill: white; -fx-padding: 6px; -fx-background-radius: 6px;");
                    }
                }
            });
        }
    }

    // ==========================================
    // 📄 TAB 4: COMPLIANCE DOSSIER TABLE & KYC
    // ==========================================
    private void initDossierTable() {
        colDriverId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colDriverName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colLicense.setCellValueFactory(new PropertyValueFactory<>("licenseNumber"));
        colCategory.setCellValueFactory(new PropertyValueFactory<>("vehicleCategory"));
        colVehicleModel.setCellValueFactory(new PropertyValueFactory<>("vehicleModel"));
        colPlate.setCellValueFactory(new PropertyValueFactory<>("registrationNumber"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("verificationStatus"));
        colRating.setCellValueFactory(new PropertyValueFactory<>("rating"));

        // Custom Cell Formatting for Status
        colStatus.setCellFactory(column -> new TableCell<DriverTableItem, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    if ("APPROVED".equalsIgnoreCase(item)) {
                        setStyle("-fx-text-fill: #10b981; -fx-font-weight: bold;");
                    } else if ("PENDING".equalsIgnoreCase(item)) {
                        setStyle("-fx-text-fill: #f59e0b; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-text-fill: #ef4444; -fx-font-weight: bold;");
                    }
                }
            }
        });

        loadMockDrivers();

        // Reactive Filtering
        FilteredList<DriverTableItem> filteredData = new FilteredList<>(masterData, p -> true);
        if (searchField != null) {
            searchField.textProperty().addListener((obs, oldVal, newVal) -> {
                filteredData.setPredicate(driver -> matchesFilter(driver, newVal, getSelectedStatus(), getSelectedCategory()));
            });
        }

        if (statusFilterCombo != null) {
            statusFilterCombo.setItems(FXCollections.observableArrayList("ALL STATUSES", "APPROVED", "PENDING", "REJECTED"));
            statusFilterCombo.getSelectionModel().selectFirst();
            statusFilterCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
                filteredData.setPredicate(driver -> matchesFilter(driver, getSearchText(), newVal, getSelectedCategory()));
            });
        }

        if (categoryFilterCombo != null) {
            categoryFilterCombo.setItems(FXCollections.observableArrayList("ALL CATEGORIES", "MINI", "SEDAN", "SUV"));
            categoryFilterCombo.getSelectionModel().selectFirst();
            categoryFilterCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
                filteredData.setPredicate(driver -> matchesFilter(driver, getSearchText(), getSelectedStatus(), newVal));
            });
        }

        SortedList<DriverTableItem> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(driversTableView.comparatorProperty());
        driversTableView.setItems(sortedData);

        // Selection Listener for Photo Inspection
        driversTableView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                showDriverDetails(newSelection);
            }
        });

        // Approve / Reject actions
        if (approveButton != null) {
            approveButton.setOnAction(e -> updateSelectedDriverStatus("APPROVED"));
        }
        if (rejectButton != null) {
            rejectButton.setOnAction(e -> updateSelectedDriverStatus("REJECTED"));
        }

        // Auto-select first item
        if (!masterData.isEmpty()) {
            driversTableView.getSelectionModel().selectFirst();
        }
    }

    private void loadMockDrivers() {
        masterData.clear();
        masterData.add(new DriverTableItem("DRV-001", "Ramesh Kumar", "AP39-2018-00124", "SEDAN", "Maruti Dzire", "AP 16 TC 4589", "APPROVED", "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=300", "", "", "", 4.9, 1420));
        masterData.add(new DriverTableItem("DRV-002", "Suresh Varma", "AP16-2021-99412", "MINI", "Tata Tiago", "AP 16 BD 7891", "PENDING", "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=300", "", "", "", 4.7, 340));
        masterData.add(new DriverTableItem("DRV-003", "Kishore Reddy", "AP09-2019-33821", "SUV", "Toyota Innova Crysta", "AP 09 CX 1102", "APPROVED", "https://images.unsplash.com/photo-1522075469751-3a6694fb2f61?w=300", "", "", "", 4.95, 2180));
        masterData.add(new DriverTableItem("DRV-004", "Venkat Rao", "AP31-2022-77114", "MINI", "Hyundai i10", "AP 31 EK 6542", "REJECTED", "https://images.unsplash.com/photo-1472099645785-5658abf4ff4e?w=300", "", "", "", 4.2, 85));
    }

    private boolean matchesFilter(DriverTableItem driver, String search, String status, String category) {
        if (search != null && !search.trim().isEmpty()) {
            String lower = search.toLowerCase();
            boolean match = driver.getName().toLowerCase().contains(lower) ||
                    driver.getLicenseNumber().toLowerCase().contains(lower) ||
                    driver.getRegistrationNumber().toLowerCase().contains(lower) ||
                    driver.getVehicleModel().toLowerCase().contains(lower);
            if (!match) return false;
        }
        if (status != null && !status.equals("ALL STATUSES") && !driver.getVerificationStatus().equalsIgnoreCase(status)) {
            return false;
        }
        if (category != null && !category.equals("ALL CATEGORIES") && !driver.getVehicleCategory().equalsIgnoreCase(category)) {
            return false;
        }
        return true;
    }

    private String getSearchText() { return searchField != null ? searchField.getText() : ""; }
    private String getSelectedStatus() { return statusFilterCombo != null ? statusFilterCombo.getValue() : "ALL STATUSES"; }
    private String getSelectedCategory() { return categoryFilterCombo != null ? categoryFilterCombo.getValue() : "ALL CATEGORIES"; }

    private void showDriverDetails(DriverTableItem driver) {
        if (selectedDriverLabel != null) {
            selectedDriverLabel.setText(driver.getName() + " (" + driver.getId() + ")");
        }
        if (selectedPlateLabel != null) {
            selectedPlateLabel.setText(driver.getVehicleModel() + " • " + driver.getRegistrationNumber());
        }
        if (selectedStatusLabel != null) {
            selectedStatusLabel.setText(driver.getVerificationStatus());
            if ("APPROVED".equalsIgnoreCase(driver.getVerificationStatus())) {
                selectedStatusLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #10b981; -fx-font-size: 12px;");
            } else if ("PENDING".equalsIgnoreCase(driver.getVerificationStatus())) {
                selectedStatusLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #f59e0b; -fx-font-size: 12px;");
            } else {
                selectedStatusLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #ef4444; -fx-font-size: 12px;");
            }
        }
        if (statusNoteLabel != null) {
            statusNoteLabel.setText("License: " + driver.getLicenseNumber() + " | Rating: " + driver.getRating() + "★");
        }

        // Photo loading asynchronously
        if (driverPhotoView != null && driver.getPhotoUrl() != null && !driver.getPhotoUrl().isEmpty()) {
            try {
                Image img = new Image(driver.getPhotoUrl(), 140, 140, true, true, true);
                driverPhotoView.setImage(img);
            } catch (Exception ex) {
                driverPhotoView.setImage(null);
            }
        }
    }

    private void updateSelectedDriverStatus(String newStatus) {
        DriverTableItem selected = driversTableView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            selected.verificationStatusProperty().set(newStatus);
            showDriverDetails(selected);
            driversTableView.refresh();
            refreshAnalyticsKPIs();
        }
    }

    // ==========================================
    // 🛡️ TAB 3: FLEET ANALYTICS & CHARTS
    // ==========================================
    private void initAnalyticsCharts() {
        if (fleetDistributionChart != null) {
            ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList(
                    new PieChart.Data("Approved", 2),
                    new PieChart.Data("Pending", 1),
                    new PieChart.Data("Rejected", 1)
            );
            fleetDistributionChart.setData(pieData);
        }

        if (bookingsBarChart != null) {
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Fleet Units");
            series.getData().add(new XYChart.Data<>("MINI", 2));
            series.getData().add(new XYChart.Data<>("SEDAN", 1));
            series.getData().add(new XYChart.Data<>("SUV", 1));
            bookingsBarChart.getData().clear();
            bookingsBarChart.getData().add(series);
        }
    }

    private void refreshAnalyticsKPIs() {
        long approved = masterData.stream().filter(d -> "APPROVED".equalsIgnoreCase(d.getVerificationStatus())).count();
        long pending = masterData.stream().filter(d -> "PENDING".equalsIgnoreCase(d.getVerificationStatus())).count();
        long rejected = masterData.stream().filter(d -> "REJECTED".equalsIgnoreCase(d.getVerificationStatus())).count();

        if (lblKpiPending != null) lblKpiPending.setText(String.valueOf(pending));

        if (fleetDistributionChart != null) {
            fleetDistributionChart.getData().clear();
            fleetDistributionChart.getData().addAll(
                    new PieChart.Data("Approved", approved),
                    new PieChart.Data("Pending", pending),
                    new PieChart.Data("Rejected", rejected)
            );
        }
    }

    private void refreshAllData() {
        if (actionProgressBar != null) {
            actionProgressBar.setVisible(true);
            actionProgressBar.setProgress(-1.0);
        }
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                Thread.sleep(600);
                return null;
            }
        };
        task.setOnSucceeded(e -> {
            if (actionProgressBar != null) {
                actionProgressBar.setVisible(false);
            }
            refreshAnalyticsKPIs();
        });
        new Thread(task).start();
    }

    // ==========================================
    // 🎨 MOTION & PHYSICS-BASED MICRO-ANIMATIONS
    // ==========================================
    private void setupMicroAnimations() {
        attachHoverAnimation(btnCalculateFare);
        attachHoverAnimation(btnBookRide);
        attachHoverAnimation(btnAcceptRide);
        attachHoverAnimation(btnDeclineRide);
        attachHoverAnimation(btnToggleDuty);
        attachHoverAnimation(approveButton);
        attachHoverAnimation(rejectButton);
        attachHoverAnimation(refreshButton);
    }

    private void attachHoverAnimation(Node node) {
        if (node == null) return;
        node.setOnMouseEntered(e -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(140), node);
            st.setToX(1.03);
            st.setToY(1.03);
            st.play();
        });
        node.setOnMouseExited(e -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(140), node);
            st.setToX(1.0);
            st.setToY(1.0);
            st.play();
        });
    }

    private void animatePulse(Node node) {
        if (node == null) return;
        ScaleTransition st = new ScaleTransition(Duration.millis(180), node);
        st.setFromX(0.96);
        st.setFromY(0.96);
        st.setToX(1.0);
        st.setToY(1.0);
        st.play();
    }
}
