@echo off
title MultiVendor Vendor Command Center (Port 5501)
echo ===================================================
echo Starting Dedicated Vendor Command Portal...
echo URL: http://localhost:5501/vendor.html
echo ===================================================
cd /d "%~dp0frontend"
npx serve -l 5501
pause
