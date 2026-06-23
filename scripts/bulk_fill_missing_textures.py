import json
import os
import shutil

ROOT = r"d:\IC1.20.1\src\main\resources\assets\ic2port"
MANIFEST = r"d:\IC1.20.1\docs\ASSET_MANIFEST.json"

item_fallbacks = [
    "textures/item/advanced_alloy.png",
    "textures/item/uu_matter.png",
    "textures/item/iron_plate.png",
]
block_fallbacks = [
    "textures/block/advanced_machine_casing.png",
    "textures/block/basic_machine_casing.png",
    "textures/block/copper_cable.png",
]

with open(MANIFEST, "r", encoding="utf-8-sig") as handle:
    data = json.load(handle)

created = 0
for asset in data["assets"]:
    if asset["status"] != "missing_texture":
        continue

    kind = asset["kind"]
    target_rel = f"textures/{kind}/{asset['id']}.png"
    target_abs = os.path.join(ROOT, target_rel)
    if os.path.exists(target_abs):
        continue

    candidates = item_fallbacks if kind == "item" else block_fallbacks
    source_abs = None
    for candidate in candidates:
        candidate_abs = os.path.join(ROOT, candidate)
        if os.path.exists(candidate_abs):
            source_abs = candidate_abs
            break

    if source_abs is None:
        continue

    os.makedirs(os.path.dirname(target_abs), exist_ok=True)
    shutil.copyfile(source_abs, target_abs)
    created += 1

print(f"Created fallback textures: {created}")
