$root = Join-Path -Path 'src/main/resources/data' -ChildPath ''
$namespaces = @('shioh','sengoku')
$searchDirs = @('structure','worldgen')
$searchStrings = @('sengoku:air','final_state','sengoku:')

$found = $false
foreach ($ns in $namespaces) {
  foreach ($dir in $searchDirs) {
    $base = Join-Path $root (Join-Path $ns $dir)
    if (-not (Test-Path $base)) { continue }
    Get-ChildItem -Path $base -Recurse -Filter *.nbt | ForEach-Object {
      try {
        $bytes = [System.IO.File]::ReadAllBytes($_.FullName)
        $text = [System.Text.Encoding]::ASCII.GetString($bytes)
        $hit = $false
        foreach ($s in $searchStrings) {
          if ($text.Contains($s)) { $hit = $true; break }
        }
        if ($hit) {
          $found = $true
          Write-Host "Found in: $($_.FullName)" -ForegroundColor Cyan
          foreach ($s in $searchStrings) {
            $idx = $text.IndexOf($s)
            if ($idx -ge 0) {
              $start = [Math]::Max(0, $idx - 40)
              $len = [Math]::Min(120, $text.Length - $start)
              $snippet = $text.Substring($start, $len)
              Write-Host ("  ..." + $snippet + "...") -ForegroundColor DarkGray
            }
          }
          Write-Host ""
        }
      } catch {
        Write-Warning "Failed to read $($_.FullName): $_"
      }
    }
  }
}
if (-not $found) { Write-Host 'No candidate .nbt files contain the target strings.' -ForegroundColor Green }
