@echo off
title RideEase Spring Boot Web Server (Ports 8080, 8081, 8082)
echo ========================================================
echo   Starting RideEase Backend Web Server
echo   - Passenger Portal:     http://localhost:8080/
echo   - Driver Terminal:      http://localhost:8081/driver/login
echo   - Fleet Command Admin:  http://localhost:8082/admin/login
echo ========================================================
cd /d "%~dp0backend"
mvn spring-boot:run
pause
