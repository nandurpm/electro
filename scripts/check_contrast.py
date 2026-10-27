"""Parse shipping theme hex colours, check text contrast, and reject stale evidence.

Use --write only after an intentional palette edit; CI compares committed results.
"""
import argparse
import re
from pathlib import Path
ROOT = Path(__file__).resolve().parents[1]

def luminance(rgb):
    # WCAG requires linear-light sRGB, not arithmetic averages of encoded channels.
    channels = [int(rgb[i:i+2],16)/255 for i in (0,2,4)]
    return sum(w*(c/12.92 if c<=.04045 else ((c+.055)/1.055)**2.4) for w,c in zip((.2126,.7152,.0722),channels))

def ratio(fg,bg):
    # Sorting makes the contrast ratio independent of foreground/background order.
    a,b = sorted((luminance(fg),luminance(bg)))
    return (b+.05)/(a+.05)

def token(source,name):
    # Toolbar tokens derive from their actual shared source definitions, not the light palette.
    match = re.search(rf'val {name} = Color\(0xFF([0-9A-Fa-f]{{6}})\)',source)
    assert match, f'Missing colour token {name}'
    return match[1]

def palette(source,name):
    # Missing/changed source syntax fails explicitly instead of retaining an obsolete colour list.
    block = re.search(rf'private val {name}\s*=\s*\w+ColorScheme\((.*?)\n    \)',source,re.S)
    assert block, f'Cannot locate {name} palette'
    values = dict(re.findall(r'(\w+)\s*=\s*Color\(0xFF([0-9A-Fa-f]{6})\)',block[1]))
    if re.search(r'primary\s*=\s*BrandNavy',block[1]):values['primary']=token(source,'BrandNavy')
    return values

def report():
    # All five tested pairs use shipping source tokens; this is not a full-screen accessibility audit.
    source=(ROOT/'app/src/main/java/com/electrokit/app/ui/theme/Theme.kt').read_text()
    dark,light=palette(source,'Dark'),palette(source,'Light')
    checks=[('dark heading',dark['primary'],dark['background']),('light heading',light['primary'],light['background']),('top bar',token(source,'TopBarText'),token(source,'BrandNavy')),('dark body',dark['onSurface'],dark['background']),('light body',light['onSurface'],light['background'])]
    # Tinted surfaces also carry primary text in cards and chips.
    for name,colors in [('dark',dark),('light',light)]:
        for surface in ['surfaceVariant','surfaceContainer']:
            checks.append((name+' primary on '+surface,colors['primary'],colors[surface]))
        for role in ['primaryContainer','secondaryContainer','tertiaryContainer','errorContainer']:
            checks.append((name+' '+role,colors['on'+role[0].upper()+role[1:]],colors[role]))
    lines=[]
    for name,fg,bg in checks:
        actual=ratio(fg,bg)
        assert actual>=4.5,(name,actual)
        lines.append(f'{name}: {actual:.2f}:1')
    return '\n'.join(lines)+'\n'

def main():
    # Evidence drift fails even if both old and new palettes happen to meet the threshold.
    parser=argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--write',action='store_true')
    args=parser.parse_args();result=report();evidence=ROOT/'verification/contrast.txt'
    if args.write:evidence.write_text(result)
    else:assert evidence.read_text()==result,'Stale contrast evidence; run scripts/check_contrast.py --write'
    print(result,end='')
if __name__=='__main__':main()
