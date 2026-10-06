@echo off
echo ===============================================================================
echo   Enterprise RESTful APIs Lab - Spring Boot 3.4.3
echo   Experiments: 2.1.1, 2.1.2, 2.2.1, 2.2.2, 2.3.1, 2.3.2
echo ===============================================================================
echo.

SET "MAVEN_CMD=C:\Users\imjay\maven\apache-maven-3.9.9\bin\mvn.cmd"
IF EXIST "%MAVEN_CMD%" (
    echo Starting Spring Boot application using installed Maven...
    "%MAVEN_CMD%" spring-boot:run
) ELSE (
    echo Starting Spring Boot using system mvn...
    mvn spring-boot:run
)

pause
