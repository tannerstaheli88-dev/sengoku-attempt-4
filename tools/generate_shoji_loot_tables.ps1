# PowerShell generator for shoji loot tables
$ErrorActionPreference = 'Stop'

$MODID = 'sengoku'
$Root = (Split-Path -Parent $PSScriptRoot)
$OutDir = Join-Path $Root "src/main/resources/data/$MODID/loot_tables/blocks"

$woods = @(
  'oak','birch','black_pine','dark_cedar','keyaki','kiso','mangrove','bamboo','sakura','bloodgood','weeping_willow'
)
$doubleVariants = @(
  '{0}_double_shoji_door','{0}_checkered_double_shoji_door','{0}_paly_double_shoji_door'
)
$tripleVariants = @(
  '{0}_triple_shoji_door','{0}_checkered_triple_shoji_door','{0}_paly_triple_shoji_door'
)
$frameVariants = @(
  '{0}_shoji_frame','{0}_checkered_shoji_frame','{0}_paly_shoji_frame','{0}_covered_shoji_frame'
)

New-Item -ItemType Directory -Path $OutDir -Force | Out-Null

function Write-Json($Path, $Obj) {
  $json = $Obj | ConvertTo-Json -Depth 8
  # Ensure LF endings and trailing newline
  $json = ($json -replace "\r\n", "\n") + "`n"
  Set-Content -LiteralPath $Path -Value $json -Encoding UTF8
}

foreach ($wood in $woods) {
  foreach ($tmpl in $doubleVariants) {
    $name = [string]::Format($tmpl, $wood)
  $data = @{ type = 'minecraft:block'; pools = @(@{ rolls = 1; entries = @(@{ type = 'minecraft:item'; name = "${MODID}:${name}" }); conditions = @(@{ condition = 'minecraft:survives_explosion' }, @{ condition = 'minecraft:block_state_property'; block = "${MODID}:${name}"; properties = @{ half = 'lower'; side = 'false' } }) }) }
    Write-Json (Join-Path $OutDir "$name.json") $data
  }
  foreach ($tmpl in $tripleVariants) {
    $name = [string]::Format($tmpl, $wood)
  $data = @{ type = 'minecraft:block'; pools = @(@{ rolls = 1; entries = @(@{ type = 'minecraft:item'; name = "${MODID}:${name}" }); conditions = @(@{ condition = 'minecraft:survives_explosion' }, @{ condition = 'minecraft:block_state_property'; block = "${MODID}:${name}"; properties = @{ half = 'lower'; part = 'middle' } }) }) }
    Write-Json (Join-Path $OutDir "$name.json") $data
  }
  foreach ($tmpl in $frameVariants) {
    $name = [string]::Format($tmpl, $wood)
  $data = @{ type = 'minecraft:block'; pools = @(@{ rolls = 1; entries = @(@{ type = 'minecraft:item'; name = "${MODID}:${name}" }); conditions = @(@{ condition = 'minecraft:survives_explosion' }) }) }
    Write-Json (Join-Path $OutDir "$name.json") $data
  }
}

Write-Host "Generated loot tables in $OutDir"