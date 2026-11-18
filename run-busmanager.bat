@echo off
setlocal
cd /d "%~dp0"
set "BUS_JAVA=java"
set "BUS_JAVAC=javac"
if defined JAVA_HOME (
    if exist "%JAVA_HOME%\bin\javac.exe" (
        set "BUS_JAVA=%JAVA_HOME%\bin\java.exe"
        set "BUS_JAVAC=%JAVA_HOME%\bin\javac.exe"
    )
)
if not exist "out" mkdir "out"
if exist sources.txt del sources.txt
for /r src %%f in (*.java) do echo "%%f" >> sources.txt
"%BUS_JAVAC%" -encoding UTF-8 -d out @sources.txt
if errorlevel 1 (
    echo Compilation failed. Make sure JDK 17 or newer is installed.
    pause
    exit /b 1
)
"%BUS_JAVA%" "-Dbusmanager.dataDir=%~dp0shared-data" -cp out Main
pause
endlocal
