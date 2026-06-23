param(
    [string]$RepoRoot = (Resolve-Path (Join-Path $PSScriptRoot "..")).Path,
    [string]$ManifestPath = "docs/ASSET_MANIFEST.json",
    [string]$OutJsonPath = "docs/F32_DISCOVERY.json",
    [string]$OutMdPath = "docs/F32_DISCOVERY.md"
)

$ErrorActionPreference = "Stop"

$baseApi = "https://api.github.com/repos/CrossVas/F32AddonsIC2Classic/git/trees/master?recursive=1"
$rawBase = "https://raw.githubusercontent.com/CrossVas/F32AddonsIC2Classic/master"

function Get-Json {
    param([string]$Url)
    return Invoke-RestMethod -Uri $Url -Headers @{ "User-Agent" = "ic2port-f32-discovery" }
}

function Normalize-Token {
    param([string]$Text)
    if ([string]::IsNullOrWhiteSpace($Text)) { return "" }
    return ($Text.ToLowerInvariant() -replace "[^a-z0-9]+", "_").Trim("_")
}

function Get-FileNameToken {
    param([string]$Path)
    $name = [System.IO.Path]::GetFileNameWithoutExtension($Path)
    return Normalize-Token -Text $name
}

$manifestAbs = Join-Path $RepoRoot $ManifestPath
if (-not (Test-Path $manifestAbs)) {
    throw "Manifest is missing. Run scripts/audit-assets.ps1 first."
}

$manifest = Get-Content -Path $manifestAbs -Raw | ConvertFrom-Json
$assetIds = @($manifest.assets | ForEach-Object { $_.id } | Sort-Object -Unique)

$tree = Get-Json -Url $baseApi
$allPaths = @($tree.tree | Where-Object { $_.type -eq "blob" } | ForEach-Object { $_.path })
$pngPaths = @($allPaths | Where-Object { $_ -like "*.png" -and $_ -like "*assets/ic2/textures/*" })
$packagePaths = @($allPaths | Where-Object { $_ -like "*texture_package.json" -and $_ -like "*assets/ic2/textures/*" })

$tokenToPaths = @{}
foreach ($path in $pngPaths) {
    $token = Get-FileNameToken -Path $path
    if ([string]::IsNullOrWhiteSpace($token)) { continue }
    if (-not $tokenToPaths.ContainsKey($token)) {
        $tokenToPaths[$token] = New-Object System.Collections.Generic.List[string]
    }
    $tokenToPaths[$token].Add($path)
}

$suggestions = New-Object System.Collections.Generic.List[object]
foreach ($id in $assetIds) {
    $token = Normalize-Token -Text $id
    $candidates = @()

    if ($tokenToPaths.ContainsKey($token)) {
        $candidates += $tokenToPaths[$token]
    }

    foreach ($kv in $tokenToPaths.GetEnumerator()) {
        if ($kv.Key -like "*$token*" -or $token -like "*$($kv.Key)*") {
            $candidates += $kv.Value
        }
    }

    $candidates = $candidates | Sort-Object -Unique
    if ($candidates.Count -gt 0) {
        $suggestions.Add([pscustomobject][ordered]@{
            id = $id
            token = $token
            candidates = @($candidates | Select-Object -First 10)
            candidate_count = $candidates.Count
        })
    }
}

$outAbs = Join-Path $RepoRoot $OutJsonPath
$outMdAbs = Join-Path $RepoRoot $OutMdPath

$outDir = Split-Path $outAbs -Parent
if (-not (Test-Path $outDir)) { New-Item -Path $outDir -ItemType Directory -Force | Out-Null }

$result = [pscustomobject][ordered]@{
    generated_at_utc = (Get-Date).ToUniversalTime().ToString("yyyy-MM-ddTHH:mm:ssZ")
    source_repo = "CrossVas/F32AddonsIC2Classic"
    png_files_scanned = $pngPaths.Count
    texture_packages_found = $packagePaths.Count
    texture_package_paths = @($packagePaths)
    suggestions = $suggestions
}

($result | ConvertTo-Json -Depth 8) | Set-Content -Path $outAbs -Encoding UTF8

$md = New-Object System.Collections.Generic.List[string]
$md.Add("# F32 Path Discovery")
$md.Add("")
$md.Add("Generated from `CrossVas/F32AddonsIC2Classic` repository tree.")
$md.Add("")
$md.Add("- PNG files scanned: $($pngPaths.Count)")
$md.Add("- texture_package.json files found: $($packagePaths.Count)")
$md.Add("- IDs with at least one candidate: $($suggestions.Count)")
$md.Add("")
$md.Add("## texture_package.json paths")
$md.Add("")
if ($packagePaths.Count -eq 0) {
    $md.Add("- None found")
} else {
    foreach ($p in $packagePaths) { $md.Add("- $p") }
}
$md.Add("")
$md.Add("## Top candidate examples")
$md.Add("")
foreach ($row in ($suggestions | Sort-Object candidate_count -Descending | Select-Object -First 80)) {
    $md.Add("### $($row.id)")
    foreach ($cand in $row.candidates) {
        $md.Add("- $cand")
    }
    $md.Add("")
}

$md | Set-Content -Path $outMdAbs -Encoding UTF8

Write-Host "Wrote $outAbs"
Write-Host "Wrote $outMdAbs"
