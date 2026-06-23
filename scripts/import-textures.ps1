param(
    [ValidateSet("green", "all")]
    [string]$Tier = "green",
    [string]$ManifestPath = "docs/ASSET_IMPORT_MANIFEST.json"
)

$ErrorActionPreference = "Stop"
$repoRoot = Resolve-Path (Join-Path $PSScriptRoot "..")
$base = Join-Path $repoRoot "src\main\resources\assets\ic2port\textures"
$manifestAbs = Join-Path $repoRoot $ManifestPath

$sourceBases = @{
    techreborn = "https://raw.githubusercontent.com/TechReborn/TechReborn/26.2/src/main/resources/assets/techreborn/textures"
    modern_industrialization = "https://raw.githubusercontent.com/AztechMC/Modern-Industrialization/1.21.x"
    crossvas_f32 = "https://raw.githubusercontent.com/CrossVas/F32AddonsIC2Classic/master/F32-1.19.2/assets/ic2/textures"
}

$greenSources = @("techreborn", "modern_industrialization")

function Get-Asset {
    param([string]$Url, [string]$Dest, [bool]$Force = $false)
    $dir = Split-Path $Dest -Parent
    if (-not (Test-Path $dir)) { New-Item -ItemType Directory -Path $dir -Force | Out-Null }
    if ((Test-Path $Dest) -and -not $Force) { return }
    Write-Host "GET $Dest"
    try {
        Invoke-WebRequest -Uri $Url -OutFile $Dest -Headers @{ "User-Agent" = "ic2port-asset-import" }
    } catch {
        Write-Host "MISS $Dest"
    }
}

if (-not (Test-Path $manifestAbs)) {
    throw "Import manifest not found: $manifestAbs. Run scripts/build-import-manifest.ps1 first."
}

$manifest = Get-Content -Path $manifestAbs -Raw | ConvertFrom-Json
$entries = @($manifest.entries)

foreach ($entry in $entries) {
    $source = [string]$entry.source
    if ($Tier -eq "green" -and $source -notin $greenSources) {
        continue
    }
    if (-not $sourceBases.ContainsKey($source)) {
        continue
    }

    $url = "$($sourceBases[$source])/$($entry.src)"
    $dest = Join-Path $base ([string]$entry.dest)
    $force = [bool]$entry.force
    Get-Asset -Url $url -Dest $dest -Force:$force
}

# Explicit metadata downloads not represented in manifest.
if ($Tier -eq "all") {
    Get-Asset -Url "$($sourceBases['crossvas_f32'])/block/fluids/steam_still.png.mcmeta" `
        -Dest (Join-Path $base "block/steam_still.png.mcmeta") -Force:$true
}

Write-Host "Done. Imported with tier '$Tier'."
