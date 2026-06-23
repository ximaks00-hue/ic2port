import json
from pathlib import Path

ROOT = Path(r"d:\IC1.20.1")
MANIFEST = ROOT / "docs" / "ASSET_IMPORT_MANIFEST.json"


def main() -> None:
    data = json.loads(MANIFEST.read_text(encoding="utf-8-sig"))
    entries = data.get("entries", [])
    filtered = [e for e in entries if e.get("source") != "crossvas_f32"]
    removed = len(entries) - len(filtered)
    data["entries"] = filtered
    MANIFEST.write_text(json.dumps(data, indent=2) + "\n", encoding="utf-8")
    print(f"Removed crossvas_f32 entries: {removed}")


if __name__ == "__main__":
    main()
