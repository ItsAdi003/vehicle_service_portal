# Runs the container lifecycle on Windows PowerShell and records everything in evidence\docker-lifecycle.log
# Usage (repository root, Docker Desktop running):   powershell -ExecutionPolicy Bypass -File scripts\docker-lifecycle-demo.ps1
param([string]$Version = "1.0.0")
$Image = "vehicle-service-portal"; $Name = "vsbp-demo"; $Port = 9090
New-Item -ItemType Directory -Force -Path evidence | Out-Null
Start-Transcript -Path "evidence\docker-lifecycle.log" -Force | Out-Null

function Wait-Healthy {
  for ($i = 1; $i -le 30; $i++) {
    try { Invoke-RestMethod "http://localhost:$Port/actuator/health" | Out-Null; Write-Host "healthy after $($i*3)s"; return } catch { Start-Sleep 3 }
  }
  Write-Host "NOT healthy after 90s"
}

Write-Host "### 1. Tools";            docker version --format "client {{.Client.Version}} / server {{.Server.Version}}"
Write-Host "### 2. Build and tag";    docker build -t "${Image}:$Version" -t "${Image}:latest" .; docker images $Image
Write-Host "### 3. Inspect image";    docker image inspect "${Image}:$Version" --format "id={{.Id}} size={{.Size}} ports={{.Config.ExposedPorts}} user={{.Config.User}}"
Write-Host "### 4. Run, map port";    docker rm -f $Name 2>$null; docker run -d --name $Name -p "${Port}:8080" -e APP_ENVIRONMENT=docker-demo -v "${Name}-data:/data" "${Image}:$Version"; docker ps --filter "name=$Name"; Wait-Healthy; (Invoke-RestMethod "http://localhost:$Port/actuator/health") | ConvertTo-Json -Compress
Write-Host "### 5. Logs and exec";    docker logs --tail 15 $Name; docker exec $Name ls -l /app /data; docker inspect --format "health={{.State.Health.Status}} started={{.State.StartedAt}}" $Name
Write-Host "### 6. Stop";             docker stop $Name; docker ps -a --filter "name=$Name"
Write-Host "### 7. Start";            docker start $Name; Wait-Healthy
Write-Host "### 8. Restart";          docker restart $Name; Wait-Healthy
Write-Host "### 9. Remove container"; docker rm -f $Name; docker ps -a --filter "name=$Name"; docker volume ls --filter "name=$Name-data"
Stop-Transcript | Out-Null
