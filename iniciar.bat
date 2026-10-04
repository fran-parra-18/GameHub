@echo off
title GameHub
cd /d "%~dp0backend"
echo Iniciando GameHub en http://localhost:8080 ...
echo (Cerra esta ventana o presiona Ctrl+C para detener el servidor)
start "" cmd /c "timeout /t 15 /nobreak >/dev/null & start http://localhost:8080"
if exist ".m2\repository" (
    call mvn -Dmaven.repo.local=.m2\repository spring-boot:run
) else (
    call mvn spring-boot:run
)
pause
