@echo off
echo Cleaning and compiling Health App...
if exist bin rmdir /s /q bin
mkdir bin

javac --module-path "C:\Users\Lenovo\Desktop\JavaFX\21\javafx-sdk-21.0.10\lib" ^
 --add-modules javafx.controls,javafx.fxml,javafx.graphics ^
 -cp "lib\mysql-connector-j-8.4.0\mysql-connector-j-8.4.0.jar;C:\Users\Lenovo\Desktop\JavaFX\21\javafx-sdk-21.0.10\lib\*" ^
 -d bin ^
 @sources.txt

if %ERRORLEVEL% EQU 0 (
    if not exist bin\gui mkdir bin\gui
    copy /y "src\gui\styles.css" "bin\gui\styles.css" >nul
    echo.
    echo ✓ Compile successful! All .class files in bin/
) else (
    echo.
    echo ✗ Compile failed! Check errors above.
)
pause
