"""Optional online URL reachability audit; never called by the offline application.

An HTTP response is not proof that a page supports a table. Sites may reject
HEAD requests or automated clients. This script records that distinction.
"""
import argparse
import concurrent.futures
import json
from pathlib import Path
import urllib.error
import urllib.request

root=Path(__file__).resolve().parents[1]
parser=argparse.ArgumentParser()
parser.add_argument('--output',default=str(root/'verification/reference-links.json'))
args=parser.parse_args()
a=root/'app/src/main/assets'
pages=json.loads((a/'pages.json').read_text())
modules=json.loads((a/'modules.json').read_text())
urls=sorted({url for page in pages.values() for url in page['sources']} | {p['source'] for parts in modules.values() for p in parts} | {v for parts in modules.values() for p in parts for v in p['specs'].values() if v.startswith('https://')})
def check(url):
    # A blocked HEAD response is inconclusive; reachability never certifies table accuracy.
    try:
        request=urllib.request.Request(url,method='HEAD',headers={'User-Agent':'ElectroKit-reference-check/1.0'})
        with urllib.request.urlopen(request,timeout=15) as r:return {'url':url,'status':r.status,'resolved_url':r.url,'checked':'reachability only'}
    except urllib.error.HTTPError as e:return {'url':url,'status':e.code,'checked':'blocked, unavailable, or HEAD unsupported; manual check needed'}
    except Exception as e:return {'url':url,'error':type(e).__name__,'checked':'could not verify'}
with concurrent.futures.ThreadPoolExecutor(max_workers=12) as pool:results=list(pool.map(check,urls))
Path(args.output).write_text(json.dumps(results,indent=2)+'\n')
print(f'{len(results)} reference URLs checked for reachability; see output for limitations.')
