# Stop-Process -Name java -Force

# .\run-services.ps1

Write-Host "=== Iniciando Arquitectura Completa de Microservicios AutoCare (Pestañas) ===" -ForegroundColor Cyan

# 1. Discovery Server (Eureka)
Write-Host "1. Levantando Discovery Server (Eureka)..." -ForegroundColor Yellow
# Crea la ventana "AutoCare" y lanza la primera pestaña
wt -w AutoCare new-tab --title "Eureka" -d ".\eureka-server" powershell -NoExit -Command ".\mvnw.cmd spring-boot:run"
Write-Host "Esperando 15 segundos a que Eureka este listo..." -ForegroundColor Gray
Start-Sleep -Seconds 15

# 2. API Gateway
Write-Host "2. Levantando API Gateway..." -ForegroundColor Yellow
# Se une a la ventana "AutoCare" y crea otra pestaña
wt -w AutoCare new-tab --title "Gateway" -d ".\api-gateway" powershell -NoExit -Command ".\mvnw.cmd spring-boot:run"
Write-Host "Esperando 10 segundos a que el Gateway se registre..." -ForegroundColor Gray
Start-Sleep -Seconds 10

# 3. Servicios Base (Core)
Write-Host "3. Levantando servicios principales (garage y booking)..." -ForegroundColor Yellow
wt -w AutoCare new-tab --title "Garage" -d ".\garage-service" powershell -NoExit -Command ".\mvnw.cmd spring-boot:run"
wt -w AutoCare new-tab --title "Booking" -d ".\booking-service" powershell -NoExit -Command ".\mvnw.cmd spring-boot:run"
Write-Host "Esperando 10 segundos..." -ForegroundColor Gray
Start-Sleep -Seconds 10

# 4. Demas Servicios de Negocio
Write-Host "4. Levantando los 9 servicios de negocio restantes..." -ForegroundColor Yellow
$servicios = @(
    "analytics-service", 
    "billing-service", 
    "diagnostics-service", 
    "hr-service", 
    "inventory-service",
    "loyalty-service",
    "notification-service",
    "procurement-service",
    "workshop-service"
)

foreach ($srv in $servicios) {
    Write-Host "Lanzando $srv en nueva pestaña..." -ForegroundColor DarkYellow
    wt -w AutoCare new-tab --title $srv -d ".\$srv" powershell -NoExit -Command ".\mvnw.cmd spring-boot:run"
    Start-Sleep -Seconds 3
}

Write-Host "¡Listo! Revisa la ventana de Windows Terminal llamada 'AutoCare'." -ForegroundColor Green