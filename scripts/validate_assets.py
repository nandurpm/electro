"""Validate offline registry, links, table shapes and module coverage using Python 3."""
import json
from pathlib import Path
from collections import Counter
# Validate shape and offline coverage, not electrical accuracy or standards compliance.
root=Path(__file__).resolve().parents[1]
a=root/'app/src/main/assets'
registry=json.loads((a/'registry.json').read_text())
pages=json.loads((a/'pages.json').read_text())
modules=json.loads((a/'modules.json').read_text())
assert Counter(x['tab'] for x in registry)=={'CALCULATORS':31,'PIN-OUTS':55,'RESOURCES':21,'PLUGINS':3}
assert len({x['id'] for x in registry})==len(registry)
assert len({x['icon'] for x in registry})==len(registry)
for entry in registry:
    if entry['screenType']=='reference':assert entry['contentId'] in pages,entry
    if entry['screenType']=='module':assert len(modules[entry['id']])>=30
used={e['contentId'] for e in registry if e['screenType']=='reference'}
assert set(pages)==used, ('Unreferenced or missing pages',set(pages)^used)
for key,page in pages.items():
    assert page['title'] and page['sections'],key
    for s in page['sections']:
        assert s['title'] and s['headers'] and s['rows'],(key,s)
        for i,header in enumerate(s['headers']):
            assert any(row[i].strip() for row in s['rows']),(key,s['title'],'Empty column',header)
        for row in s['rows']:assert len(row)==len(s['headers']),(key,s['title'],row,s['headers'])
        for d in s['diagrams']:
            assert d['caption']
            for pin in d['pins']:assert pin['label'] and 0<=pin['x']<=1 and 0<=pin['y']<=1,(key,pin)
for key,entries in modules.items():
    assert len({p['name'] for p in entries})==len(entries)
    assert all(p['name'] and p['category'] and p['specs'] and p['source'].startswith('https://') for p in entries)
    for part in entries:
        assert set(part['numericKeys'])<=set(part['specs']),(key,part['name'])
        assert not {'Description','Use','Package note'} & set(part['numericKeys'])
        for name in part['numericKeys']:
            value=part['specs'][name].replace('−','-')
            try: float(value)
            except ValueError: raise AssertionError((part['name'],name,value))
manifest=(root/'app/src/main/AndroidManifest.xml').read_text()
# 4.3 uses only INTERNET to retrieve published release metadata; prohibit other permissions.
import re
permissions = re.findall(r'<uses-permission\s+android:name="([^"]+)"\s*/>', manifest)
assert permissions == ['android.permission.INTERNET'], ('Unexpected app permission', permissions)
assert 'http://' not in manifest and 'android:usesCleartextTraffic="true"' not in manifest

# Every page must disclose provenance; URL presence alone must not imply verified table cells.
assert all(p.get("provenance", "").strip() for p in pages.values())

print('PASS: 31 calculators, 55 pin-outs, 21 resources, 3 modules; all routes, tables, diagrams and module records are structurally valid.')
