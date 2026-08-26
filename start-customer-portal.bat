@echo off
title MultiVendor Customer & Public Portal (Port 5500)
echo ===================================================
echo Starting Customer & Public Services Portal...
echo URL: http://localhost:5500/index.html
echo ===================================================
cd /d "%~dp0frontend"
npx serve -l 5500
pause
