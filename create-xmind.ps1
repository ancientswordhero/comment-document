$tempDir = "$env:TEMP\xmind_temp_$(Get-Random)"
New-Item -ItemType Directory -Path $tempDir -Force | Out-Null

$contentPath = Join-Path $tempDir "content.json"
$metadataPath = Join-Path $tempDir "metadata.json"

Copy-Item -Path "$PSScriptRoot\content.json" -Destination $contentPath -Force
Copy-Item -Path "$PSScriptRoot\metadata.json" -Destination $metadataPath -Force

$zipPath = "$PSScriptRoot\temp_xmind.zip"
$outputPath = "$PSScriptRoot\cloud-library-mindmap.xmind"

if (Test-Path $zipPath) { Remove-Item $zipPath -Force }
if (Test-Path $outputPath) { Remove-Item $outputPath -Force }

Add-Type -AssemblyName System.IO.Compression.FileSystem
[System.IO.Compression.ZipFile]::CreateFromDirectory($tempDir, $zipPath)

Copy-Item -Path $zipPath -Destination $outputPath -Force

Remove-Item $zipPath -Force
Remove-Item -Path $tempDir -Recurse -Force

Write-Host "XMind file created: $outputPath"
Write-Host "Please open it with XMind application"