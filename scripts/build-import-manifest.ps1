param(
    [string]$RepoRoot = (Resolve-Path (Join-Path $PSScriptRoot "..")).Path,
    [string]$ImportScriptPath = "scripts/import-textures.ps1",
    [string]$OutPath = "docs/ASSET_IMPORT_MANIFEST.json"
)

$ErrorActionPreference = "Stop"

$importAbs = Join-Path $RepoRoot $ImportScriptPath
$outAbs = Join-Path $RepoRoot $OutPath

if (-not (Test-Path $importAbs)) {
    throw "Import script not found: $importAbs"
}

$entries = New-Object System.Collections.Generic.List[object]
$lines = Get-Content -Path $importAbs

foreach ($line in $lines) {
    if ($line -match 'Get-Tr\s+"([^"]+)"\s+"([^"]+)"') {
        $entries.Add([pscustomobject][ordered]@{
            source = "techreborn"
            src = $matches[1]
            dest = $matches[2]
            force = $false
        })
        continue
    }

    if ($line -match 'Get-Mi\s+"([^"]+)"\s+"([^"]+)"') {
        $entries.Add([pscustomobject][ordered]@{
            source = "modern_industrialization"
            src = $matches[1]
            dest = $matches[2]
            force = $false
        })
        continue
    }

    if ($line -match 'Get-Ic2c\s+"([^"]+)"\s+"([^"]+)"(?:\s+-Force)?') {
        $entries.Add([pscustomobject][ordered]@{
            source = "crossvas_f32"
            src = $matches[1]
            dest = $matches[2]
            force = ($line -match "-Force")
        })
        continue
    }
}

$manifest = [pscustomobject][ordered]@{
    generated_at_utc = (Get-Date).ToUniversalTime().ToString("yyyy-MM-ddTHH:mm:ssZ")
    source_script = $ImportScriptPath
    entries = $entries
}

$outDir = Split-Path $outAbs -Parent
if (-not (Test-Path $outDir)) { New-Item -Path $outDir -ItemType Directory -Force | Out-Null }

($manifest | ConvertTo-Json -Depth 6) | Set-Content -Path $outAbs -Encoding UTF8
Write-Host "Wrote $outAbs ($($entries.Count) entries)"
