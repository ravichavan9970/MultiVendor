@echo off
title MultiVendor Backend Server (Port 8081)
echo ===================================================
echo Starting MultiVendor Spring Boot Backend Server...
echo Database: Persistent Disk (./data/multivendordb)
echo API URL:  http://localhost:8081/api/v1
echo Swagger:  http://localhost:8081/swagger-ui.html
echo H2 DB:    http://localhost:8081/h2-console
echo ===================================================
cd /d "%~dp0backend"
mvn spring-boot:run
pause
