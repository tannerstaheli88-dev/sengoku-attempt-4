import json
import os
from pathlib import Path

BASE = Path(r"D:\Minecraft\mods\mod workspace\sengoku attempt 4\run\resourcepacks\Shogun Texture Pack 1.21.1 MOD\assets\sengoku")
BLOCKSTATES = BASE / "blockstates"
MODELS_DIR = BASE / "models" / "block" / "double_shoji"
TEXTURES_DOUBLE = BASE / "textures" / "block" / "double_door"
TEXTURES_BASE = BASE / "textures" / "block"

missing_models = set()
missing_textures = set()
parsed_models = set()

def collect_strings(obj):
    """Recursively collect all string values from JSON object."""
    if isinstance(obj, str):
        yield obj
    elif isinstance(obj, dict):
        for v in obj.values():
            yield from collect_strings(v)
    elif isinstance(obj, list):
        for item in obj:
            yield from collect_strings(item)


def main():
    if not BLOCKSTATES.exists():
        print("Blockstates directory not found:", BLOCKSTATES)
        return 2
    if not MODELS_DIR.exists():
        print("Models directory not found:", MODELS_DIR)
        return 2

    bs_files = sorted([p for p in BLOCKSTATES.glob("*_double_shoji_door.json")])
    if not bs_files:
        print("No blockstate files found matching *_double_shoji_door.json in", BLOCKSTATES)
        return 1

    for p in bs_files:
        try:
            data = json.loads(p.read_text(encoding='utf-8'))
        except Exception as e:
            print(f"Failed to parse JSON {p}: {e}")
            continue
        for s in collect_strings(data):
            if isinstance(s, str) and 'sengoku:block/double_shoji/' in s:
                model_name = s.split('sengoku:block/double_shoji/')[-1]
                parsed_models.add(model_name)

    for m in sorted(parsed_models):
        model_path = MODELS_DIR / (m + '.json')
        if not model_path.exists():
            missing_models.add(str(model_path.relative_to(BASE)))
        else:
            # parse model for texture refs
            try:
                mm = json.loads(model_path.read_text(encoding='utf-8'))
            except Exception as e:
                print(f"Failed to parse model JSON {model_path}: {e}")
                continue
            for s in collect_strings(mm):
                if isinstance(s, str) and s.startswith('sengoku:block/double_door/'):
                    tex = s.split('sengoku:block/double_door/')[-1]
                    # texture key may reference atlas (no extension)
                    png = TEXTURES_DOUBLE / (tex + '.png')
                    if not png.exists():
                        missing_textures.add(str(png.relative_to(BASE)))
                # also check plank textures under sengoku:block/<wood>_planks
                if isinstance(s, str) and s.startswith('sengoku:block/') and 'planks' in s:
                    tex = s.split('sengoku:block/')[-1]
                    png = TEXTURES_BASE / (tex + '.png')
                    if not png.exists():
                        missing_textures.add(str(png.relative_to(BASE)))

    print('Parsed models count:', len(parsed_models))
    print('Missing model files:', len(missing_models))
    for m in sorted(missing_models):
        print(' MISSING MODEL:', m)
    print('Missing texture files:', len(missing_textures))
    for t in sorted(missing_textures):
        print(' MISSING TEXTURE:', t)

    if not missing_models and not missing_textures:
        print('\nVERIFICATION OK: all referenced models and textures exist.')
        return 0
    else:
        return 3

if __name__ == '__main__':
    exit(main())
