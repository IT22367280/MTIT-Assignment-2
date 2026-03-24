# start-backend.ps1
$ErrorActionPreference = "Stop"

$workspaceDir = "d:\micro\MTIT-Assignment-2\food-ordering-system"
Set-Location $workspaceDir

# 1. Load Environment Variables from .env
Write-Host "Loading environment variables from .env..." -ForegroundColor Cyan
if (Test-Path ".env") {
    Get-Content ".env" | Where-Object { $_ -match '=' -and $_ -notmatch '^#' } | ForEach-Object {
        $name, $value = $_.Split('=', 2)
        [System.Environment]::SetEnvironmentVariable($name.Trim(), $value.Trim(), "Process")
    }
    Write-Host ".env loaded successfully!" -ForegroundColor Green
} else {
    Write-Host "Warning: .env file not found. Ensure NeonDB details are set." -ForegroundColor Yellow
}

$services = @("api-gateway", "customer-service", "menu-service", "order-service", "payment-service")

# 2. Start all services in new PowerShell windows
Write-Host "Starting microservices..." -ForegroundColor Cyan

foreach ($service in $services) {
    Write-Host "Starting $service..."
    $servicePath = Join-Path $workspaceDir $service
    	
    # Use Start-Process to open a new terminal window for each service
    Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd `"$servicePath`"; Write-Host `"Starting $service...`" -ForegroundColor Green; mvn spring-boot:run"
}

Write-Host "All 5 services have been started in separate windows." -ForegroundColor Green
Write-Host "Wait for them to boot, then access the API Gateway on http://localhost:8080"
