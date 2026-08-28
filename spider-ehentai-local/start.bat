@echo off
cd /d %~dp0
echo ============================================
echo   spider-on-ehentai-local launcher
echo ============================================
echo   config : application.yml (this folder)
echo   save   : ./downloads (change in yml)
echo   api    : http://127.0.0.1:8081
echo   POST /download?url=...^&isDownload=true
echo ============================================
java -jar spider-on-ehentai-local-1.0-SNAPSHOT.jar
echo.
echo Service exited.
pause
