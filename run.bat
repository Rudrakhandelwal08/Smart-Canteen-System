@echo off
cd /d "%~dp0"
where java >nul 2>nul
if errorlevel 1 (
  echo Java was not found. Install JDK 17 or newer and try again.
  pause
  exit /b 1
)
if not exist out mkdir out
 echo Compiling Smart Canteen...
javac --add-modules jdk.httpserver -encoding UTF-8 -d out src\com\smartcanteen\Main.java
if errorlevel 1 (
  echo Compilation failed. Check the error messages above.
  pause
  exit /b 1
)
echo.
echo Starting BiteBox at http://localhost:8091
java --add-modules jdk.httpserver -cp out com.smartcanteen.Main
pause
