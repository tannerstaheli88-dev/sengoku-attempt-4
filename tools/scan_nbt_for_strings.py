import os
import sys

ROOT = os.path.join('src', 'main', 'resources', 'data')
TARGET_NAMESPACES = ('shioh', 'sengoku')
SEARCH_DIRS = ['structure', 'worldgen']
SEARCH_STRINGS = [b'sengoku:air', b'sengoku:', b'final_state']

matches = []
for ns in TARGET_NAMESPACES:
    ns_dir = os.path.join(ROOT, ns)
    for sub in SEARCH_DIRS:
        base = os.path.join(ns_dir, sub)
        if not os.path.isdir(base):
            continue
        for root, _, files in os.walk(base):
            for f in files:
                if not f.lower().endswith('.nbt'):
                    continue
                path = os.path.join(root, f)
                try:
                    data = open(path, 'rb').read()
                except Exception as e:
                    print(f"Failed to read {path}: {e}", file=sys.stderr)
                    continue
                hit = False
                for s in SEARCH_STRINGS:
                    if s in data:
                        hit = True
                        break
                if hit:
                    # Provide a tiny context window around first occurrence
                    idx = min((data.find(s) for s in SEARCH_STRINGS if s in data), default=-1)
                    snippet = data[max(0, idx-32): idx+64] if idx >= 0 else b''
                    # Replace non-printable
                    snippet_printable = ''.join(chr(b) if 32 <= b < 127 else '.' for b in snippet)
                    matches.append((path, snippet_printable))

if not matches:
    print('No candidate .nbt files contain the target strings.')
else:
    for path, snippet in matches:
        print(f"Found in: {path}\n  ...{snippet}...\n")
