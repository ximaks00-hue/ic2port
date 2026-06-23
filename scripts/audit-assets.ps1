param(
    [string]$RepoRoot = (Resolve-Path (Join-Path $PSScriptRoot "..")).Path,
    [string]$ManifestPath = "docs/ASSET_MANIFEST.json",
    [string]$AuditPath = "docs/ASSET_AUDIT.md"
)

$ErrorActionPreference = "Stop"

function Get-RegisteredIds {
    param(
        [string]$FilePath,
        [string]$RegistryToken
    )

    if (-not (Test-Path $FilePath)) {
        throw "Registry file not found: $FilePath"
    }

    $content = Get-Content -Path $FilePath -Raw
    $matches = [regex]::Matches($content, "$RegistryToken\.register\(""([^""]+)""")
    $ids = @()
    foreach ($match in $matches) {
        $ids += $match.Groups[1].Value
    }
    return $ids | Sort-Object -Unique
}

function Get-ImportMapping {
    param([string]$ImportManifestPath)

    $mapping = @{}
    if (-not (Test-Path $ImportManifestPath)) {
        return $mapping
    }

    $manifest = Get-Content -Path $ImportManifestPath -Raw | ConvertFrom-Json
    foreach ($entry in $manifest.entries) {
        $tier = if ($entry.source -eq "crossvas_f32") { "yellow" } else { "green" }
        $mapping[[string]$entry.dest] = @{
            repo = [string]$entry.source
            source = [string]$entry.src
            tier = $tier
        }
    }

    return $mapping
}

function Get-SourceOverrideMapping {
    param([string]$OverridePath)
    $mapping = @{}
    if (-not (Test-Path $OverridePath)) {
        return $mapping
    }
    $json = Get-Content -Path $OverridePath -Raw | ConvertFrom-Json
    foreach ($entry in $json.overrides) {
        $mapping[[string]$entry.dest] = @{
            repo = [string]$entry.repo
            source = [string]$entry.source
            tier = [string]$entry.tier
        }
    }
    return $mapping
}

function Convert-TextureRefToPng {
    param([string]$TextureRef)

    if ([string]::IsNullOrWhiteSpace($TextureRef)) { return $null }
    if (-not $TextureRef.StartsWith("ic2port:")) { return $null }

    $rel = $TextureRef.Substring("ic2port:".Length)
    return "textures/$rel.png"
}

function Get-ModelData {
    param(
        [string]$ModelPath,
        [string]$RepoRoot
    )

    $result = [ordered]@{
        exists = $false
        textures = @()
        placeholder = $false
        placeholderReasons = @()
    }

    if (-not (Test-Path $ModelPath)) { return $result }

    $result.exists = $true
    $raw = Get-Content -Path $ModelPath -Raw

    try {
        $json = $raw | ConvertFrom-Json -ErrorAction Stop
        if ($null -ne $json.textures) {
            $textureRefs = @()
            foreach ($prop in $json.textures.PSObject.Properties) {
                $textureRefs += [string]$prop.Value
            }
            foreach ($textureRef in $textureRefs | Sort-Object -Unique) {
                if ($textureRef -match "advanced_machine_casing|basic_machine_casing") {
                    $result.placeholder = $true
                    $result.placeholderReasons += "casing_texture"
                }
                if ($textureRef.StartsWith("minecraft:block/")) {
                    $result.placeholder = $true
                    $result.placeholderReasons += "vanilla_block_texture"
                }
                $pngRel = Convert-TextureRefToPng -TextureRef $textureRef
                if ($null -ne $pngRel) { $result.textures += $pngRel }
            }
        }
    } catch {
        # Ignore malformed JSON here; missing/invalid model will be visible in audit.
    }

    $result.textures = $result.textures | Sort-Object -Unique
    $result.placeholderReasons = $result.placeholderReasons | Sort-Object -Unique
    return $result
}

$blockRegistry = Join-Path $RepoRoot "src/main/java/dev/ic2port/setup/BlockRegistry.java"
$itemRegistry = Join-Path $RepoRoot "src/main/java/dev/ic2port/setup/ItemRegistry.java"
$modelsRoot = Join-Path $RepoRoot "src/main/resources/assets/ic2port/models"
$texturesRoot = Join-Path $RepoRoot "src/main/resources/assets/ic2port/textures"
$importManifest = Join-Path $RepoRoot "docs/ASSET_IMPORT_MANIFEST.json"
$sourceOverridesPath = Join-Path $RepoRoot "docs/ASSET_SOURCE_OVERRIDES.json"

$blockIds = Get-RegisteredIds -FilePath $blockRegistry -RegistryToken "BLOCKS"
$itemIds = Get-RegisteredIds -FilePath $itemRegistry -RegistryToken "ITEMS"
$importMapping = Get-ImportMapping -ImportManifestPath $importManifest
$sourceOverrides = Get-SourceOverrideMapping -OverridePath $sourceOverridesPath

$entries = New-Object System.Collections.Generic.List[object]

foreach ($id in $blockIds) {
    $modelRel = "block/$id.json"
    $modelAbs = Join-Path $modelsRoot $modelRel
    $modelData = Get-ModelData -ModelPath $modelAbs -RepoRoot $RepoRoot

    $candidateTextures = New-Object System.Collections.Generic.List[string]
    foreach ($t in $modelData.textures) { $candidateTextures.Add($t) }
    $candidateTextures.Add("textures/block/$id.png")

    $existingTextures = @()
    foreach ($rel in $candidateTextures | Sort-Object -Unique) {
        $abs = Join-Path (Join-Path $RepoRoot "src/main/resources/assets/ic2port") $rel
        if (Test-Path $abs) { $existingTextures += $rel }
    }
    $existingTextures = $existingTextures | Sort-Object -Unique

    $source = $null
    foreach ($rel in $existingTextures) {
        $lookup = $rel.Replace("textures/", "")
        if ($sourceOverrides.ContainsKey($lookup)) {
            $source = $sourceOverrides[$lookup]
            break
        }
        if ($importMapping.ContainsKey($lookup)) {
            $source = $importMapping[$lookup]
            break
        }
    }

    $status = if (-not $modelData.exists) { "missing_model" } elseif ($existingTextures.Count -eq 0) { "missing_texture" } else { "ok" }

    $entries.Add([pscustomobject][ordered]@{
        id = $id
        kind = "block"
        model = $modelRel
        model_exists = $modelData.exists
        textures = @($existingTextures)
        status = $status
        placeholder = $modelData.placeholder
        placeholder_reasons = @($modelData.placeholderReasons)
        source = if ($null -ne $source) { $source.repo } else { "local_ic2port" }
        source_path = if ($null -ne $source) { $source.source } else { "local/generated_or_adapted" }
        license_tier = if ($null -ne $source) { $source.tier } else { "green" }
    })
}

foreach ($id in $itemIds) {
    $modelRel = "item/$id.json"
    $modelAbs = Join-Path $modelsRoot $modelRel
    $modelData = Get-ModelData -ModelPath $modelAbs -RepoRoot $RepoRoot

    $candidateTextures = New-Object System.Collections.Generic.List[string]
    foreach ($t in $modelData.textures) { $candidateTextures.Add($t) }
    $candidateTextures.Add("textures/item/$id.png")

    $existingTextures = @()
    foreach ($rel in $candidateTextures | Sort-Object -Unique) {
        $abs = Join-Path (Join-Path $RepoRoot "src/main/resources/assets/ic2port") $rel
        if (Test-Path $abs) { $existingTextures += $rel }
    }
    $existingTextures = $existingTextures | Sort-Object -Unique

    $source = $null
    foreach ($rel in $existingTextures) {
        $lookup = $rel.Replace("textures/", "")
        if ($sourceOverrides.ContainsKey($lookup)) {
            $source = $sourceOverrides[$lookup]
            break
        }
        if ($importMapping.ContainsKey($lookup)) {
            $source = $importMapping[$lookup]
            break
        }
    }

    $status = if (-not $modelData.exists) { "missing_model" } elseif ($existingTextures.Count -eq 0) { "missing_texture" } else { "ok" }

    $entries.Add([pscustomobject][ordered]@{
        id = $id
        kind = "item"
        model = $modelRel
        model_exists = $modelData.exists
        textures = @($existingTextures)
        status = $status
        placeholder = $modelData.placeholder
        placeholder_reasons = @($modelData.placeholderReasons)
        source = if ($null -ne $source) { $source.repo } else { "local_ic2port" }
        source_path = if ($null -ne $source) { $source.source } else { "local/generated_or_adapted" }
        license_tier = if ($null -ne $source) { $source.tier } else { "green" }
    })
}

$entries = $entries | Sort-Object kind, id
$manifestAbs = Join-Path $RepoRoot $ManifestPath
$auditAbs = Join-Path $RepoRoot $AuditPath

$manifestDir = Split-Path $manifestAbs -Parent
if (-not (Test-Path $manifestDir)) { New-Item -Path $manifestDir -ItemType Directory -Force | Out-Null }

$auditDir = Split-Path $auditAbs -Parent
if (-not (Test-Path $auditDir)) { New-Item -Path $auditDir -ItemType Directory -Force | Out-Null }

$manifest = [ordered]@{
    generated_at_utc = (Get-Date).ToUniversalTime().ToString("yyyy-MM-ddTHH:mm:ssZ")
    repository_root = $RepoRoot
    counts = [ordered]@{
        blocks = $blockIds.Count
        items = $itemIds.Count
        total = $entries.Count
    }
    assets = $entries
}

($manifest | ConvertTo-Json -Depth 8) | Set-Content -Path $manifestAbs -Encoding UTF8

$missingModel = @($entries | Where-Object { $_.status -eq "missing_model" })
$missingTexture = @($entries | Where-Object { $_.status -eq "missing_texture" })
$placeholders = @($entries | Where-Object { $_.placeholder -eq $true })
$unknownSource = @($entries | Where-Object { $_.source -eq "unknown" })

$auditLines = New-Object System.Collections.Generic.List[string]
$auditLines.Add("# Asset Audit")
$auditLines.Add("")
$auditLines.Add("Generated by `scripts/audit-assets.ps1`.")
$auditLines.Add("")
$auditLines.Add("## Summary")
$auditLines.Add("")
$auditLines.Add("- Total IDs: $($entries.Count)")
$auditLines.Add("- Missing models: $($missingModel.Count)")
$auditLines.Add("- Missing textures: $($missingTexture.Count)")
$auditLines.Add("- Placeholder models: $($placeholders.Count)")
$auditLines.Add("- Unknown source mapping: $($unknownSource.Count)")
$auditLines.Add("")
$auditLines.Add("## P0 candidates")
$auditLines.Add("")
if ($missingModel.Count -eq 0 -and $missingTexture.Count -eq 0) {
    $auditLines.Add("- None")
} else {
    foreach ($entry in ($missingModel + $missingTexture | Sort-Object kind, id)) {
        $auditLines.Add("- $($entry.kind): `"$($entry.id)`" ($($entry.status))")
    }
}
$auditLines.Add("")
$auditLines.Add("## Placeholder sample (first 50)")
$auditLines.Add("")
if ($placeholders.Count -eq 0) {
    $auditLines.Add("- None")
} else {
    foreach ($entry in ($placeholders | Select-Object -First 50)) {
        $reasons = ($entry.placeholder_reasons -join ", ")
        $auditLines.Add("- $($entry.kind): `"$($entry.id)`" ($reasons)")
    }
}
$auditLines.Add("")
$auditLines.Add("## Unknown source sample (first 50)")
$auditLines.Add("")
if ($unknownSource.Count -eq 0) {
    $auditLines.Add("- None")
} else {
    foreach ($entry in ($unknownSource | Select-Object -First 50)) {
        $auditLines.Add("- $($entry.kind): `"$($entry.id)`"")
    }
}

$auditLines | Set-Content -Path $auditAbs -Encoding UTF8

Write-Host "Wrote $manifestAbs"
Write-Host "Wrote $auditAbs"
