"""Validate the exact supplied fault import and the complete offline engineering reference graph.

Uses only Python's standard library; no Android SDK, network access or pytest is required.
This checks source integrity and navigation, not OEM approval or handwritten electrical accuracy.
"""
import json
from pathlib import Path


def main():
    # Compare immutable original fields separately from derived explanations and navigational indexes.
    root = Path(__file__).resolve().parents[1]
    data = json.loads((root / 'app/src/main/assets/engineering.json').read_text())
    tools = json.loads((root / 'app/src/main/assets/engineering_tools.json').read_text())
    rows = (root / 'app/src/test/resources/faults-original.tsv').read_text().splitlines()
    actual = ['|'.join(map(str, [f['serial'], f['code'], f['message'], f['reset'], f['group']])) for f in data['faults']]
    assert rows == actual and len(rows) == 125
    assert {f['serial'] for f in data['faults']} == set(range(1, 129)) - {112, 113, 114}
    assert len({f['code'] for f in data['faults']}) == 125
    ids = {r['id'] for r in data['records']}
    sources = {s['id'] for s in data['sources']}
    assert len(ids) == len(data['records'])
    assert sum(s['type'] == 'Photograph transcription' for s in data['sources']) == 14
    for record in data['records'] + data['faults']:
        assert record['sources'] and set(record['sources']) <= sources
        assert set(record['related']) <= ids
    for marker in data['markers']:
        assert marker['record'] in ids and marker['source'] in sources
        assert 0 <= marker['x'] <= 1000 and 0 <= marker['y'] <= 560
    assert len({t['id'] for t in tools}) == len(tools) == 15
    for tool in tools:
        assert tool['formula'] and tool['assumptions'] and tool['fields']
        assert len({f['key'] for f in tool['fields']}) == len(tool['fields'])
    assert 'output-Q9–Q12' in ids
    assert not {'output-Q9', 'output-Q10', 'output-Q11', 'output-Q12'} & ids
    print(f"PASS: 125 exact faults, {len(ids)} technical records, 14 photographs, 15 tools; all sources and routes resolve.")


if __name__ == '__main__':
    main()
