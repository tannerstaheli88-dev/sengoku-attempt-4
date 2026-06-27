$base = "D:\Minecraft\mods\mod workspace\sengoku attempt 4\run\resourcepacks\Shogun Texture Pack 1.21.1 MOD\assets\sengoku"
$blockstates = Join-Path $base "blockstates"
$models = Join-Path $base "models\block\double_shoji"
$textures_double = Join-Path $base "textures\block\double_door"
$textures_base = Join-Path $base "textures\block"

$missingModels = @()
$missingTextures = @()
$parsedModels = [System.Collections.Generic.HashSet[string]]::new()

$base = "D:\Minecraft\mods\mod workspace\sengoku attempt 4\run\resourcepacks\Shogun Texture Pack 1.21.1 MOD\assets\sengoku"
$blockstates = Join-Path $base "blockstates"
$models = Join-Path $base "models\block\double_shoji"
$textures_double = Join-Path $base "textures\block\double_door"
$textures_base = Join-Path $base "textures\block"

$missingModels = @()
$missingTextures = @()
$parsedModels = [System.Collections.Generic.HashSet[string]]::new()

function Collect-Strings($obj) {
    if ($null -eq $obj) { return }
    if ($obj -is [string]) { return ,$obj }
    if ($obj -is [System.Collections.IEnumerable]) {
        $results = @()
        foreach ($i in $obj) { $results += Collect-Strings $i }
        return $results
    }
    if ($obj -is [PSObject]) {
        $results = @()
        foreach ($p in $obj.psobject.Properties) { $results += Collect-Strings $p.Value }
        return $results
    }
    return @()
}

if (-not (Test-Path $blockstates)) { Write-Host "Blockstates dir not found: $blockstates"; exit 2 }
if (-not (Test-Path $models)) { Write-Host "Models dir not found: $models"; exit 2 }

$bsFiles = Get-ChildItem -Path $blockstates -Filter "*_double_shoji_door.json" -File | Sort-Object Name
if ($bsFiles.Count -eq 0) { Write-Host "No blockstates found"; exit 1 }

foreach ($f in $bsFiles) {
    try {
        $json = Get-Content $f.FullName -Raw | ConvertFrom-Json -ErrorAction Stop
    } catch {
        Write-Host ("Failed to parse JSON {0}: {1}" -f $($f.FullName), $($_))
        continue
    }
    $strings = Collect-Strings $json
    foreach ($s in $strings) {
        if ($s -and $s -is [string] -and $s.Contains('sengoku:block/double_shoji/')) {
            $model = $s.Split('sengoku:block/double_shoji/')[1]
            $parsedModels.Add($model) | Out-Null
        }
    }
}

foreach ($m in $parsedModels | Sort-Object) {
    $mp = Join-Path $models ($m + '.json')
    if (-not (Test-Path $mp)) { $missingModels += $mp.Substring($base.Length+1) }
    else {
        try {
            $mm = Get-Content $mp -Raw | ConvertFrom-Json -ErrorAction Stop
        } catch {
            Write-Host ("Failed to parse model JSON {0}: {1}" -f $($mp), $($_))
            continue
        }
        $mstrings = Collect-Strings $mm
        foreach ($s in $mstrings) {
            if ($s -and $s -is [string] -and $s.StartsWith('sengoku:block/double_door/')) {
                $tex = $s.Split('sengoku:block/double_door/')[1]
                $png = Join-Path $textures_double ($tex + '.png')
                if (-not (Test-Path $png)) { $missingTextures += $png.Substring($base.Length+1) }
            }
            if ($s -and $s -is [string] -and $s.StartsWith('sengoku:block/') -and $s.Contains('planks')) {
                $tex = $s.Split('sengoku:block/')[1]
                $png = Join-Path $textures_base ($tex + '.png')
                if (-not (Test-Path $png)) { $missingTextures += $png.Substring($base.Length+1) }
            }
        }
    }
}

Write-Host "Parsed models count: " $parsedModels.Count
Write-Host "Missing model files: " $missingModels.Count
foreach ($mm in $missingModels | Sort-Object) { Write-Host " MISSING MODEL: $mm" }
Write-Host "Missing texture files: " $missingTextures.Count
foreach ($mt in $missingTextures | Sort-Object) { Write-Host " MISSING TEXTURE: $mt" }

if ($missingModels.Count -eq 0 -and $missingTextures.Count -eq 0) { Write-Host "`nVERIFICATION OK: all referenced models and textures exist."; exit 0 }
else { exit 3 }