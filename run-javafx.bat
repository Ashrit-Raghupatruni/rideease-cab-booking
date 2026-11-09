@echo off
title RideEase Unified Command Console (JavaFX Desktop - All Portals)
echo ========================================================
echo   Launching RideEase JavaFX Desktop Application
echo   Includes all 3 portals:
echo     [Tab 1] Rider Portal (Port 8080)
echo     [Tab 2] Driver Terminal (Port 8081)
echo     [Tab 3] Fleet Operations Admin (Port 8082)
echo     [Tab 4] Compliance Dossier (TableView & KYC Previews)
echo ========================================================
cd /d "%~dp0backend"
mvn exec:java -Dexec.mainClass="com.rideease.javafx.RideEaseFXApp"
pause
