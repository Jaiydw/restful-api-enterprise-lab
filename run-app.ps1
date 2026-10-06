Write-Host "===============================================================================" -ForegroundColor Cyan
Write-Host "  Enterprise RESTful APIs Lab - Spring Boot 3.4.3" -ForegroundColor Green
Write-Host "  Experiments: 2.1.1, 2.1.2, 2.2.1, 2.2.2, 2.3.1, 2.3.2" -ForegroundColor Yellow
Write-Host "===============================================================================" -ForegroundColor Cyan
Write-Host ""

$mavenCmd = "C:\Users\imjay\maven\apache-maven-3.9.9\bin\mvn.cmd"
if (Test-Path $mavenCmd) {
    & $mavenCmd spring-boot:run
} else {
    mvn spring-boot:run
}
