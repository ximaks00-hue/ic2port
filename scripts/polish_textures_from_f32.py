import json
import os
import urllib.request
from pathlib import Path

ROOT = Path(r"d:\IC1.20.1")
MANIFEST = ROOT / "docs" / "ASSET_MANIFEST.json"
BASE_OUT = ROOT / "src" / "main" / "resources" / "assets" / "ic2port" / "textures"

API_TREE = "https://api.github.com/repos/CrossVas/F32AddonsIC2Classic/git/trees/master?recursive=1"
RAW_BASE = "https://raw.githubusercontent.com/CrossVas/F32AddonsIC2Classic/master"


def fetch_tree_paths() -> list[str]:
    req = urllib.request.Request(API_TREE, headers={"User-Agent": "ic2port-f32-polish"})
    with urllib.request.urlopen(req) as r:
        data = json.loads(r.read().decode("utf-8"))
    paths = []
    for node in data.get("tree", []):
        if node.get("type") == "blob":
            p = node.get("path", "")
            if p.endswith(".png") and "F32-1.19.2/assets/ic2/textures/" in p:
                paths.append(p)
    return paths


def choose_best(paths: list[str], kind: str, asset_id: str) -> str | None:
    exact = [p for p in paths if p.endswith(f"/{asset_id}.png")]
    if not exact:
        return None
    if kind == "item":
        item_pref = [p for p in exact if "/item/" in p]
        if item_pref:
            return sorted(item_pref, key=len)[0]
    if kind == "block":
        block_pref = [p for p in exact if "/block/" in p]
        if block_pref:
            return sorted(block_pref, key=len)[0]
    return sorted(exact, key=len)[0]


def download(url: str, out_path: Path) -> None:
    out_path.parent.mkdir(parents=True, exist_ok=True)
    req = urllib.request.Request(url, headers={"User-Agent": "ic2port-f32-polish"})
    with urllib.request.urlopen(req) as r:
        data = r.read()
    out_path.write_bytes(data)


def main() -> None:
    with open(MANIFEST, "r", encoding="utf-8-sig") as f:
        manifest = json.load(f)

    f32_paths = fetch_tree_paths()
    by_name = {}
    for p in f32_paths:
        name = Path(p).name
        by_name.setdefault(name, []).append(p)

    replaced = 0
    per_kind = {"block": 0, "item": 0}

    for asset in manifest["assets"]:
        asset_id = asset["id"]
        kind = asset["kind"]
        target = BASE_OUT / kind / f"{asset_id}.png"
        if not target.exists():
            continue

        name = f"{asset_id}.png"
        candidates = by_name.get(name, [])
        best = choose_best(candidates, kind, asset_id)
        if not best:
            continue

        url = f"{RAW_BASE}/{best}"
        download(url, target)
        replaced += 1
        per_kind[kind] += 1
        print(f"REPLACE {kind}/{asset_id}.png <- {best}")

    print(f"Replaced total: {replaced} (block={per_kind['block']}, item={per_kind['item']})")


if __name__ == "__main__":
    main()
