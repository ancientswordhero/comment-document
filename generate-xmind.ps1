$jsonContent = Get-Content -Path "$PSScriptRoot\mindmap-content.json" -Raw -Encoding UTF8
$timestamp = Get-Date -Format 'yyyy-MM-ddTHH:mm:ss'
$xmindContent = "{`n  `"meta`": {`n    `"creator`": {`n      `"name`": `"AI Assistant`",`n      `"version`": `"1.0`"`n    },`n    `"created`": `"$timestamp`",`n    `"modified`": `"$timestamp`"`n  },`n  `"data`": $jsonContent`n}"
$metadata = "{`n  `"creator`": {`n    `"name`": `"XMind`",`n    `"version`": `"2020.4.2.202101080046`"`n  }`n}"
$tempDir = "$env:TEMP\xmind_temp_$PID"
New-Item -ItemType Directory -Path $tempDir -Force | Out-Null
$contentJsonPath = Join-Path $tempDir "content.json"
$metadataPath = Join-Path $tempDir "metadata.json"
[System.IO.File]::WriteAllText($contentJsonPath, $xmindContent, [System.Text.Encoding]::UTF8)
[System.IO.File]::WriteAllText($metadataPath, $metadata, [System.Text.Encoding]::UTF8)
$zipPath = "$PSScriptRoot\cloud-library-mindmap.zip"
$outputPath = "$PSScriptRoot\cloud-library-mindmap.xmind"
Compress-Archive -Path "$tempDir\*" -DestinationPath $zipPath -Force
Rename-Item -Path $zipPath -NewName "cloud-library-mindmap.xmind" -Force
Remove-Item -Path $tempDir -Recurse -Force
Write-Host "Done: $outputPath"
