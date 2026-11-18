@echo off
setlocal
cd /d "%~dp0"
set "BUS_JAVA=java"
set "BUS_JAVAC=javac"
if defined JAVA_HOME (
    set "BUS_JAVA=%JAVA_HOME%\bin\java.exe"
    set "BUS_JAVAC=%JAVA_HOME%\bin\javac.exe"
)
if not exist "out" mkdir "out"
"%BUS_JAVAC%" -encoding UTF-8 -sourcepath "src" -d "out" "src\test.java"
if errorlevel 1 (
    echo Compilation failed. Install/select JDK 17 or newer.
    pause
    exit /b 1
)
"%BUS_JAVA%" "-Dbusmanager.dataDir=%~dp0..\shared-data" -cp "out" test
pause
endlocal
