param(
    [string]$Root
)

# Resolve default loot tables directory if not supplied
if (-not $Root -or [string]::IsNullOrWhiteSpace($Root)) {
    $Root = Join-Path (Split-Path -Parent $PSScriptRoot) 'src\main\resources\data\sengoku\loot_tables\blocks'
}

$Root = (Resolve-Path -LiteralPath $Root).Path
Write-Host "Scanning for malformed loot table JSONs under: $Root"

$files = Get-ChildItem -LiteralPath $Root -Recurse -Filter *.json -File -ErrorAction Stop

[int]$fixedCount = 0
[int]$skippedNoEscapes = 0
[int]$skippedInvalid = 0

foreach ($file in $files) {
    try {
        $content = Get-Content -LiteralPath $file.FullName -Raw -Encoding UTF8
    } catch {
        Write-Warning "Failed to read: $($file.FullName) -> $($_.Exception.Message)"
        continue
    }

    if ($content -notmatch '\\n' -and $content -notmatch '\\t') {
        $skippedNoEscapes++
        continue
    }

    # Replace literal escape sequences with actual characters
    $fixed = $content

    # First handle literal CRLF and CR/LF combos written as text (\\r\\n, \\r, \\n)
    $fixed = $fixed -replace '\\r\\n', "`n"
    $fixed = $fixed -replace '\\r', "`n"
    $fixed = $fixed -replace '\\n', "`n"

    # Replace literal \t with real tab
    $fixed = $fixed -replace '\\t', "`t"

    # Optionally normalize multiple blank lines (harmless if left, but cleaner)
    $fixed = [regex]::Replace($fixed, "`n{3,}", "`n`n")

    # Validate JSON before writing (ConvertFrom-Json doesn't support -Depth in Windows PowerShell 5.1)
    $isValid = $true
    try {
        $null = $fixed | ConvertFrom-Json -ErrorAction Stop
    } catch {
        $isValid = $false
        $skippedInvalid++
        Write-Warning "Skipping (would become invalid JSON): $($file.FullName) -> $($_.Exception.Message)"
    }

    if ($isValid) {
        try {
            Set-Content -LiteralPath $file.FullName -Value $fixed -NoNewline -Encoding UTF8
            Write-Host "Fixed: $($file.FullName)" -ForegroundColor Green
            $fixedCount++
        } catch {
            Write-Warning "Failed to write: $($file.FullName) -> $($_.Exception.Message)"
        }
    }
}

Write-Host "Done. Fixed: $fixedCount, Skipped (no escapes): $skippedNoEscapes, Skipped (invalid after replace): $skippedInvalid" -ForegroundColor Cyan
