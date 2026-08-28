@echo off
cd /d %~dp0
set PYTHONPATH=%~dp0
echo ============================================
echo   WebDAV share
echo   URL  : http://0.0.0.0:8088
echo   /        -> %~dp0downloads
echo   /pronhub -> E:\pronhub
echo   Auth : anonymous
echo   Stop : close this window
echo ============================================
"C:\Users\zpl\AppData\Local\Python\pythoncore-3.14-64\Scripts\wsgidav.exe" -c "%~dp0wsgidav-config.yaml"
pause
