$tempDir = "$env:TEMP\xmind_temp_$(Get-Random)"
New-Item -ItemType Directory -Path $tempDir -Force | Out-Null

$metaInfDir = Join-Path $tempDir "META-INF"
New-Item -ItemType Directory -Path $metaInfDir -Force | Out-Null

$contentXmlPath = Join-Path $tempDir "content.xml"
$manifestXmlPath = Join-Path $tempDir "manifest.xml"
$manifestMfPath = Join-Path $metaInfDir "MANIFEST.MF"

Copy-Item -Path "$PSScriptRoot\content.xml" -Destination $contentXmlPath -Force
Copy-Item -Path "$PSScriptRoot\manifest.xml" -Destination $manifestXmlPath -Force
Copy-Item -Path "$PSScriptRoot\MANIFEST.MF" -Destination $manifestMfPath -Force

$zipPath = "$PSScriptRoot\temp_xmind.zip"
$outputPath = "$PSScriptRoot\cloud-library-mindmap.xmind"

if (Test-Path $zipPath) { Remove-Item $zipPath -Force }
if (Test-Path $outputPath) { Remove-Item $outputPath -Force }

Add-Type -AssemblyName System.IO.Compression.FileSystem
[System.IO.Compression.ZipFile]::CreateFromDirectory($tempDir, $zipPath)

Copy-Item -Path $zipPath -Destination $outputPath -Force

Remove-Item $zipPath -Force
Remove-Item -Path $tempDir -Recurse -Force

Write-Host "XMind XML format file created: $outputPath"
Write-Host "This format is compatible with XMind 3.x and later versions"