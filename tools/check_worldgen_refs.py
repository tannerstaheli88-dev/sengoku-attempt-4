import json
import os
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
DATA = ROOT / 'src' / 'main' / 'resources' / 'data'

refs = set()

def find_features(obj):
    if isinstance(obj, dict):
        for k, v in obj.items():
            if k == 'feature' and isinstance(v, str):
                refs.add(v)
            else:
                find_features(v)
    elif isinstance(obj, list):
        for it in obj:
            find_features(it)

# scan template_pool and placed_feature and configured_feature files
for sub in ['worldgen/template_pool', 'worldgen/placed_feature', 'worldgen/configured_feature']:
    folder = DATA
    # iterate namespaces
    if folder.exists():
        for namespace in folder.iterdir():
            if namespace.is_dir():
                base = namespace / sub
                if base.exists():
                    for root, _, files in os.walk(base):
                        for f in files:
                            if f.endswith('.json'):
                                p = Path(root) / f
                                try:
                                    j = json.load(open(p, 'r', encoding='utf-8'))
                                    find_features(j)
                                except Exception as e:
                                    print(f'ERROR parsing {p}: {e}')

# also scan all template_pool files for element.feature entries
pool_base = DATA
for ns in pool_base.iterdir():
    if ns.is_dir():
        base = ns / 'worldgen' / 'template_pool'
        if base.exists():
            for root, _, files in os.walk(base):
                for f in files:
                    if f.endswith('.json'):
                        p = Path(root) / f
                        try:
                            j = json.load(open(p, 'r', encoding='utf-8'))
                            find_features(j)
                        except Exception as e:
                            print(f'ERROR parsing {p}: {e}')

# collect available placed/configured feature keys
available = set()
for ns in DATA.iterdir():
    if not ns.is_dir():
        continue
    for kind in ['worldgen/placed_feature', 'worldgen/configured_feature']:
        base = ns / kind
        if base.exists():
            for root, _, files in os.walk(base):
                for f in files:
                    if f.endswith('.json'):
                        rel = Path(root).relative_to(ns / 'worldgen' / kind)
                        key = f[:-5]
                        # join subfolders
                        prefix = rel.as_posix()
                        if prefix == '.':
                            full = f'{ns.name}:{key}'
                        else:
                            full = f'{ns.name}:{prefix}/{key}'
                        available.add(full)

missing = []
for r in sorted(refs):
    # skip obvious vanilla features that are likely present in builtin registries
    ns, _, path = r.partition(':')
    if ns == 'minecraft':
        continue
    if r not in available:
        missing.append(r)

print('Found', len(refs), 'feature refs (excluding minecraft:).')
if missing:
    print('Missing feature keys (not present under data/<ns>/worldgen/placed_feature or configured_feature):')
    for m in missing:
        print(' -', m)
else:
    print('No missing non-minecraft feature refs detected.')
