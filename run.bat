@echo off
echo Launching Health Assistance System...

java --module-path "C:\Users\Lenovo\Desktop\JavaFX\21\javafx-sdk-21.0.10\lib" ^
     --add-modules javafx.controls,javafx.fxml,javafx.graphics ^
     --class-path "bin;lib\mysql-connector-j-8.4.0\mysql-connector-j-8.4.0.jar" ^
     HealthAppRunner

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ✓ App launched successfully!
) else (
    echo.
    echo ✗ Launch failed. Check:
    echo   - OpenJFX in lib/javafx/javafx/lib/
    echo   - All classes compiled in bin/
    echo   - Java 17+ installed
)
pause
