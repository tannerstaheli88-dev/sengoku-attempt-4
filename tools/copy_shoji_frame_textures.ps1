$src = 'd:\Minecraft\mods\mod workspace\sengoku attempt 4\run\resource_pacls\shogun texture pack\sengoku\textures'
$dst = Join-Path $src 'shoji_frame'
if(-not (Test-Path -Path $dst)){
    New-Item -ItemType Directory -Path $dst | Out-Null
}
$files = Get-ChildItem -Path $src -Filter '*trapdoor*.png' -Recurse -File -ErrorAction SilentlyContinue
if($files -and $files.Count -gt 0){
    foreach($f in $files){
        $new = $f.Name -replace 'trapdoor','shoji_frame'
        Copy-Item -Path $f.FullName -Destination (Join-Path $dst $new) -Force
    }
    Write-Output "COPIED $($files.Count) FILES"
} else {
    Write-Output 'NO_MATCH'
}