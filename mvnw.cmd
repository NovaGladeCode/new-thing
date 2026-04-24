@echo off
setlocal

set "JAVA_HOME=C:\Users\NovaGlade\AppData\Local\Programs\Eclipse Adoptium\jdk-21.0.4.7-hotspot"
set "JAVA_EXE=%JAVA_HOME%\bin\java.exe"
set "WRAPPER_JAR=%~dp0.mvn\wrapper\maven-wrapper.jar"
set "WRAPPER_PROPS=%~dp0.mvn\wrapper\maven-wrapper.properties"

if not exist "%JAVA_EXE%" (
    echo ERROR: Java not found at %JAVA_EXE%
    exit /B 1
)

if not exist "%WRAPPER_JAR%" (
    echo ERROR: maven-wrapper.jar not found. Run the setup again.
    exit /B 1
)

"%JAVA_EXE%" -jar "%WRAPPER_JAR%" %*
endlocal
