$url = "http://localhost:9090/api/categories"
try {
    $response = Invoke-WebRequest -Uri $url -UseBasicParsing
    Write-Host "Success! Status Code: $($response.StatusCode)"
    Write-Host "Response: $($response.Content)"
} catch {
    Write-Host "Error: $_"
}