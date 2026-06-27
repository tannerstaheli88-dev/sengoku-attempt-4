import json
from pathlib import Path

# Configuration
MODID = "sengoku"
ROOT = Path(__file__).resolve().parents[1]  # project root
OUT_DIR = ROOT / "src" / "main" / "resources" / "data" / MODID / "loot_tables" / "blocks"

woods = [
    "oak",
    "birch",
    "black_pine",
    "dark_cedar",
    "keyaki",
    "kiso",
    "mangrove",
    "bamboo",
    "sakura",
    "bloodgood",
    "weeping_willow",
]

double_variants = [
    ("", "{wood}_double_shoji_door"),
    ("checkered_", "{wood}_checkered_double_shoji_door"),
    ("paly_", "{wood}_paly_double_shoji_door"),
]

triple_variants = [
    ("", "{wood}_triple_shoji_door"),
    ("checkered_", "{wood}_checkered_triple_shoji_door"),
    ("paly_", "{wood}_paly_triple_shoji_door"),
]

frame_variants = [
    "{wood}_shoji_frame",
    "{wood}_checkered_shoji_frame",
    "{wood}_paly_shoji_frame",
    "{wood}_covered_shoji_frame",
]


def ensure_dir(p: Path):
    p.mkdir(parents=True, exist_ok=True)


def write_json(path: Path, data: dict):
    ensure_dir(path.parent)
    with path.open("w", encoding="utf-8") as f:
        json.dump(data, f, indent=2)
        f.write("\n")


def loot_item(name: str) -> dict:
    return {"type": "minecraft:item", "name": f"{MODID}:{name}"}


def condition_survives() -> dict:
    return {"condition": "minecraft:survives_explosion"}


def condition_block_state(block_name: str, properties: dict) -> dict:
    return {
        "condition": "minecraft:block_state_property",
        "block": f"{MODID}:{block_name}",
        "properties": properties,
    }


def make_double_loot(wood: str, name_tmpl: str):
    name = name_tmpl.format(wood=wood)
    data = {
        "type": "minecraft:block",
        "pools": [
            {
                "rolls": 1,
                "entries": [loot_item(name)],
                "conditions": [
                    condition_survives(),
                    condition_block_state(name, {"half": "lower", "side": "false"}),
                ],
            }
        ],
    }
    write_json(OUT_DIR / f"{name}.json", data)


def make_triple_loot(wood: str, name_tmpl: str):
    name = name_tmpl.format(wood=wood)
    data = {
        "type": "minecraft:block",
        "pools": [
            {
                "rolls": 1,
                "entries": [loot_item(name)],
                "conditions": [
                    condition_survives(),
                    condition_block_state(name, {"half": "lower", "part": "middle"}),
                ],
            }
        ],
    }
    write_json(OUT_DIR / f"{name}.json", data)


def make_frame_loot(wood: str, name_tmpl: str):
    name = name_tmpl.format(wood=wood)
    data = {
        "type": "minecraft:block",
        "pools": [
            {"rolls": 1, "entries": [loot_item(name)], "conditions": [condition_survives()]}
        ],
    }
    write_json(OUT_DIR / f"{name}.json", data)


def main():
    ensure_dir(OUT_DIR)
    count = 0
    for wood in woods:
        for _, name_tmpl in double_variants:
            make_double_loot(wood, name_tmpl)
            count += 1
        for _, name_tmpl in triple_variants:
            make_triple_loot(wood, name_tmpl)
            count += 1
        for name_tmpl in frame_variants:
            make_frame_loot(wood, name_tmpl)
            count += 1
    print(f"Generated {count} loot tables in {OUT_DIR}")


if __name__ == "__main__":
    main()
