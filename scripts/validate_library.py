"""Validate bundled component IDs, general component categories, PNG integrity and image-memory boundaries."""
import hashlib
import json
import struct
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1] / 'app/src/main/assets'


def validate(root=ROOT):
    """Reject broken links/traversal, duplicate records/images and corrupt image headers."""
    records = json.loads((root / 'components.json').read_text())
    ids, images, hashes = set(), set(), set()
    for item in records:
        assert all(isinstance(item.get(k), str) and item[k] for k in ('id', 'name', 'category', 'image')), item
        assert item['id'] not in ids, f"Duplicate component ID: {item['id']}"
        ids.add(item['id'])
        path = (root / item['image']).resolve()
        assert path.is_relative_to(root.resolve() / 'components'), f"Image outside component directory: {item['image']}"
        data = path.read_bytes()
        assert data[:8] == b'\x89PNG\r\n\x1a\n', path
        width, height = struct.unpack('>II', data[16:24])
        assert 0 < width <= 4096 and 0 < height <= 4096, (path, width, height)
        if path not in images:
            digest = hashlib.sha256(data).hexdigest()
            assert digest not in hashes, f'Duplicate image bytes: {path}; reuse the same asset path'
            hashes.add(digest)
        images.add(path)
    actual = {p.resolve() for p in (root / 'components').glob('*.png')}
    assert actual == images, 'Unreferenced or missing component files'
    assert sum(p.stat().st_size for p in images) < 32 * 1024 * 1024, 'Review excessive library size'
    print(f"PASS: {len(records)} component records, {len(images)} unique PNGs, valid local paths and dimensions.")


if __name__ == '__main__':
    validate()
