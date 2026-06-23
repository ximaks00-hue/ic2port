import json
import os
import shutil
import urllib.request
from pathlib import Path

ROOT = Path(r"d:\IC1.20.1")
IMPORT_MANIFEST = ROOT / "docs" / "ASSET_IMPORT_MANIFEST.json"
TEXTURES_ROOT = ROOT / "src" / "main" / "resources" / "assets" / "ic2port" / "textures"

SOURCE_BASES = {
    "techreborn": "https://raw.githubusercontent.com/TechReborn/TechReborn/26.2/src/main/resources/assets/techreborn/textures",
    "modern_industrialization": "https://raw.githubusercontent.com/AztechMC/Modern-Industrialization/1.21.x",
}

# Tier-yellow destinations currently tied to CrossVas; replace with green-safe equivalents.
YELLOW_FALLBACKS = {
    "block/steam_still.png": "block/geothermal_generator_top_on.png",
    "block/steam_flow.png": "block/geothermal_generator_top.png",
    "item/hazmat_helmet.png": "item/nano_helmet.png",
    "item/hazmat_chestplate.png": "item/nano_chestplate.png",
    "item/hazmat_leggings.png": "item/nano_leggings.png",
    "item/hazmat_boots.png": "item/nano_boots.png",
    "models/armor/hazmat_layer_1.png": "models/armor/nano_layer_1.png",
    "models/armor/hazmat_layer_2.png": "models/armor/nano_layer_2.png",
    "item/capacitor_cell.png": "item/energy_crystal.png",
}


def download(url: str, out_path: Path) -> None:
    out_path.parent.mkdir(parents=True, exist_ok=True)
    req = urllib.request.Request(url, headers={"User-Agent": "ic2port-green-enforce"})
    with urllib.request.urlopen(req) as r:
        data = r.read()
    out_path.write_bytes(data)


def copy_rel(src_rel: str, dst_rel: str) -> None:
    src = TEXTURES_ROOT / src_rel
    dst = TEXTURES_ROOT / dst_rel
    if not src.exists():
        return
    dst.parent.mkdir(parents=True, exist_ok=True)
    shutil.copyfile(src, dst)


def main() -> None:
    with open(IMPORT_MANIFEST, "r", encoding="utf-8-sig") as f:
        manifest = json.load(f)

    green_count = 0
    for entry in manifest["entries"]:
        source = entry["source"]
        if source not in SOURCE_BASES:
            continue
        url = f"{SOURCE_BASES[source]}/{entry['src']}"
        dst = TEXTURES_ROOT / entry["dest"]
        try:
            download(url, dst)
            green_count += 1
            print(f"GREEN {entry['dest']}")
        except Exception:
            # Some generated paths may disappear between upstream versions.
            print(f"MISS {entry['dest']} <- {entry['src']}")

    fallback_count = 0
    for dst_rel, src_rel in YELLOW_FALLBACKS.items():
        copy_rel(src_rel, dst_rel)
        fallback_count += 1
        print(f"FALLBACK {dst_rel} <- {src_rel}")

    # Keep steam animation metadata if present.
    steam_mcmeta = TEXTURES_ROOT / "block" / "steam_still.png.mcmeta"
    if not steam_mcmeta.exists():
        steam_mcmeta.write_text('{"animation":{"frametime":2}}\n', encoding="utf-8")

    print(f"Applied green imports: {green_count}")
    print(f"Applied yellow fallbacks: {fallback_count}")


if __name__ == "__main__":
    main()
