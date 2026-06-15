# Guarda la ubicacion de la raiz del proyecto
$rootPath = Get-Location
$globalReportFile = Join-Path $rootPath "cobertura-global.html"

# Encuentra todas las carpetas con pom.xml
$microservicios = Get-ChildItem -Directory | Where-Object { Test-Path (Join-Path $_.FullName "pom.xml") }

Write-Host "Iniciando pruebas y reportes para $($microservicios.Count) microservicios..." -ForegroundColor Yellow

# Estructura inicial del HTML global
$htmlContent = @"
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <title>Panel de Cobertura Global - AutoCare</title>
    <style>
        body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; margin: 40px; background-color: #f4f6f9; color: #333; }
        h1 { color: #2c3e50; border-bottom: 2px solid #34495e; padding-bottom: 10px; }
        .grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(300px, 1fr)); gap: 20px; margin-top: 30px; }
        .card { background: white; padding: 20px; border-radius: 8px; box-shadow: 0 4px 6px rgba(0,0,0,0.1); transition: transform 0.2s; }
        .card:hover { transform: translateY(-5px); }
        .card h3 { margin-top: 0; color: #2980b9; }
        .btn { display: inline-block; margin-top: 10px; padding: 8px 15px; background-color: #27ae60; color: white; text-decoration: none; border-radius: 4px; font-weight: bold; }
        .btn:hover { background-color: #219653; }
        .badge { background-color: #e74c3c; color: white; padding: 3px 8px; border-radius: 12px; font-size: 12px; float: right; }
        .badge.success { background-color: #27ae60; }
    </style>
</head>
<body>
    <h1>Dashboard de Cobertura de Codigo - Ecosistema AutoCare</h1>
    <p>Selecciona un microservicio para auditar sus lineas de codigo detalladamente en JaCoCo:</p>
    <div class="grid">
"@

foreach ($ms in $microservicios) {
    Write-Host "`n=================================================" -ForegroundColor Cyan
    Write-Host "Ejecutando en: $($ms.Name)" -ForegroundColor Green
    Write-Host "=================================================" -ForegroundColor Cyan
    
    Set-Location $ms.FullName
    
    # 1. Ejecuta tests y genera el reporte individual
    if (Test-Path ".\mvnw.cmd") {
        .\mvnw.cmd clean test
        .\mvnw.cmd jacoco:report
    } else {
        mvn clean test
        mvn jacoco:report
    }
    
    # 2. Verifica si se genero el index.html para anadirlo al panel global
    $relativeHtmlPath = "$($ms.Name)/target/site/jacoco/index.html"
    $absoluteHtmlPath = Join-Path $rootPath $relativeHtmlPath
    
    if (Test-Path $absoluteHtmlPath) {
        $statusBadge = "<span class='badge success'>Disponible</span>"
        $actionButton = "<a class='btn' href='./$relativeHtmlPath' target='_blank'>Ver Reporte -&gt;</a>"
    } else {
        $statusBadge = "<span class='badge'>Sin Reporte</span>"
        $actionButton = "<p style='color: #7f8c8d; font-size: 14px;'>No se genero el reporte (Revisa si fallaron los tests)</p>"
    }
    
    # Anade la tarjeta del microservicio al contenido HTML
    $htmlContent += @"
        <div class="card">
            <h3>$statusBadge $($ms.Name)</h3>
            <p>Modulo de la arquitectura de AutoCare.</p>
            $actionButton
        </div>
"@
    
    Set-Location $rootPath
}

# Cierre del HTML
$htmlContent += @"
    </div>
</body>
</html>
"@

# Guarda el archivo index unificado en la raiz (Forzando UTF-8)
$htmlContent | Out-File -FilePath $globalReportFile -Encoding utf8

Write-Host "`nPanel Global Creado. Abre 'cobertura-global.html' en tu raiz para navegar por todos los reportes." -ForegroundColor Magenta