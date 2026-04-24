@echo off
setlocal

set "JAVA_HOME=C:\Users\NovaGlade\AppData\Local\Programs\Eclipse Adoptium\jdk-21.0.4.7-hotspot"
set "MVN=C:\Users\NovaGlade\.gemini\antigravity\scratch\maven\apache-maven-3.9.9\bin\mvn.cmd"

echo Building SoulLink plugin...
"%MVN%" clean package -q
if %ERRORLEVEL% EQU 0 (
    echo.
    echo BUILD SUCCESS
    echo JAR is at: target\SoulLink-1.0.0.jar
) else (
    echo.
    echo BUILD FAILED - check output above
)
