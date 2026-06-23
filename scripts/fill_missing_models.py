import json
import os
import re
from pathlib import Path

ROOT = Path(r"d:\IC1.20.1")
AUDIT = ROOT / "docs" / "ASSET_AUDIT.md"
BLOCK_MODELS = ROOT / "src" / "main" / "resources" / "assets" / "ic2port" / "models" / "block"
ITEM_MODELS = ROOT / "src" / "main" / "resources" / "assets" / "ic2port" / "models" / "item"


def read_missing_models():
    text = AUDIT.read_text(encoding="utf-8")
    result = []
    for line in text.splitlines():
        m = re.search(r'- (block|item): "([^"]+)" \(missing_model\)', line)
        if m:
            result.append((m.group(1), m.group(2)))
    return result


def write_json(path: Path, obj: dict):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(obj, indent=2) + "\n", encoding="utf-8")


def build_block_model(block_id: str) -> dict:
    cable_core = {
        "copper_cable": "copper_cable_core",
        "gold_cable": "gold_cable_core",
        "hv_cable": "hv_cable_core",
        "glass_fiber_cable": "glass_fiber_cable_core",
    }
    if block_id in cable_core:
        return {"parent": f"ic2port:block/{cable_core[block_id]}"}

    if block_id == "detector_cable":
        return {"parent": "ic2port:block/copper_cable_core"}
    if block_id == "splitter_cable":
        return {"parent": "ic2port:block/copper_cable_core"}

    tube_core = {
        "item_tube": "item_tube_core",
        "extraction_tube": "extraction_tube_core",
        "request_tube": "request_tube_core",
        "limiter_tube": "limiter_tube_core",
        "sorting_tube": "sorting_tube_core",
        "filter_tube": "filter_tube_core",
        "speed_tube": "speed_tube_core",
        "color_filter_tube": "color_filter_tube_core",
        "hover_tube": "hover_tube_core",
        "pickup_tube": "pickup_tube_core",
        "redstone_tube": "redstone_tube_core",
        "switch_tube": "switch_tube_core",
        "transport_tube": "transport_tube_core",
        "round_robin_tube": "round_robin_tube_core",
        "stacking_tube": "stacking_tube_core",
        "teleport_tube": "teleport_tube_core",
        "provider_tube": "provider_tube_core",
        "insertion_tube": "insertion_tube_core",
        "sticky_tube": "sticky_tube_core",
        "void_tube": "void_tube_core",
    }
    if block_id in tube_core:
        return {"parent": f"ic2port:block/{tube_core[block_id]}"}

    if block_id == "crop_sticks":
        return {"parent": "ic2port:block/crop_sticks_stage0"}

    if block_id == "reinforced_door":
        return {
            "parent": "minecraft:block/door_bottom",
            "textures": {"bottom": "ic2port:block/reinforced_stone", "top": "ic2port:block/reinforced_stone"},
        }

    return {
        "parent": "minecraft:block/cube_all",
        "textures": {"all": f"ic2port:block/{block_id}"},
    }


def build_item_model(item_id: str) -> dict:
    return {
        "parent": "minecraft:item/generated",
        "textures": {"layer0": f"ic2port:item/{item_id}"},
    }


def main():
    missing = read_missing_models()
    created = 0
    for kind, asset_id in missing:
        if kind == "block":
            target = BLOCK_MODELS / f"{asset_id}.json"
            if not target.exists():
                write_json(target, build_block_model(asset_id))
                created += 1
        else:
            target = ITEM_MODELS / f"{asset_id}.json"
            if not target.exists():
                write_json(target, build_item_model(asset_id))
                created += 1
    print(f"Created missing models: {created}")


if __name__ == "__main__":
    main()
