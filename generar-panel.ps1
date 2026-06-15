# Guarda la ubicacion de la raiz
$rootPath = Get-Location
$globalReportFile = Join-Path $rootPath "panel-cobertura.html"

# Encuentra todas las carpetas con pom.xml
$microservicios = Get-ChildItem -Directory | Where-Object { Test-Path (Join-Path $_.FullName "pom.xml") }

Write-Host "Construyendo el panel unificado de cobertura..." -ForegroundColor Yellow

# Estructura inicial del HTML
$htmlContent = @"
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <title>Panel de Cobertura - AutoCare</title>
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
    <h1>Dashboard de Cobertura - Ecosistema AutoCare</h1>
    <p>Selecciona un microservicio para ver su reporte (generado previamente):</p>
    <div class="grid">
"@

foreach ($ms in $microservicios) {
    # Solo arma la ruta y verifica si existe el reporte generado antes
    $relativeHtmlPath = "$($ms.Name)/target/site/jacoco/index.html"
    $absoluteHtmlPath = Join-Path $rootPath $relativeHtmlPath
    
    if (Test-Path $absoluteHtmlPath) {
        $statusBadge = "<span class='badge success'>Disponible</span>"
        $actionButton = "<a class='btn' href='./$relativeHtmlPath' target='_blank'>Ver Reporte -&gt;</a>"
    } else {
        $statusBadge = "<span class='badge'>Sin Reporte</span>"
        $actionButton = "<p style='color: #7f8c8d; font-size: 14px;'>Falta ejecutar pruebas aquí</p>"
    }
    
    # Añade la tarjeta
    $htmlContent += @"
        <div class="card">
            <h3>$statusBadge $($ms.Name)</h3>
            $actionButton
        </div>
"@
}

# Cierre del HTML
$htmlContent += @"
    </div>
</body>
</html>
"@

# Guarda el archivo
$htmlContent | Out-File -FilePath $globalReportFile -Encoding utf8

Write-Host "¡Listo! Se ha creado 'panel-cobertura.html' en la raíz." -ForegroundColor Green