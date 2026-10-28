"""Check KDoc on Kotlin types, functions, member state and constructor properties.

Local functions are included. Temporary local vals and lambda parameters are not
API declarations. Constructor fields accept an adjacent KDoc or @property/@param
on the owning class. Syntax-tree parsing prevents strings and comments from
manufacturing declarations. Install scripts/requirements-dev.txt before running.
"""
from pathlib import Path
import re
import sys
from tree_sitter import Language, Parser
import tree_sitter_kotlin
ROOT = Path(__file__).resolve().parents[1]
PARSER = Parser(Language(tree_sitter_kotlin.language()))
TYPES = {'class_declaration', 'object_declaration', 'function_declaration', 'secondary_constructor', 'type_alias'}
DENSITY = {'Calculators.kt', 'Engineering.kt', 'App.kt', 'CalculatorScreen.kt', 'AppViewModel.kt'}
def walk(node):
    # Include nested helpers, not just top-level APIs.
    yield node
    for child in node.named_children:
        yield from walk(child)
def preceding_doc(node, source):
    # Annotations belong to the declaration node, so its previous sibling is the KDoc.
    previous = node.prev_named_sibling
    if previous and previous.type == 'block_comment':
        text = source[previous.start_byte:previous.end_byte].decode()
        if text.startswith('/**'):
            return text
    return ''
def declared_name(node, source):
    # Properties wrap their name; annotation names must not be mistaken for property names.
    target = next((n for n in node.named_children if n.type == 'variable_declaration'), node)
    identifiers = [n for n in target.named_children if n.type == 'identifier']
    if not identifiers:
        identifiers = [n for n in walk(target) if n.type == 'identifier']
    return source[identifiers[0].start_byte:identifiers[0].end_byte].decode() if identifiers else node.type

def audit(path):
    # Fail closed on grammar errors rather than reporting incomplete coverage as success.
    source = path.read_bytes()
    tree = PARSER.parse(source)
    failures = []
    if tree.root_node.has_error:
        failures.append(f'{path.relative_to(ROOT)}: Kotlin syntax tree contains an error; coverage is not trustworthy')
    count = 0
    for node in walk(tree.root_node):
        # Test methods communicate their contract through assertion names and bodies.
        if 'src/test' in str(path) and node.type == 'function_declaration':
            continue
        # Local/private implementation details do not need public-API boilerplate.
        prefix = source[node.start_byte:node.end_byte].split(b'{', 1)[0]
        if re.search(rb'\b(private|internal)\b', prefix):
            continue
        if node.type in TYPES and any(parent.type == 'function_body' for parent in [node.parent] if parent):
            continue
        required = node.type in TYPES or (node.type == 'property_declaration' and node.parent.type in {'source_file', 'class_body'})
        if node.type == 'class_parameter' and re.search(rb'\b(val|var)\b', source[node.start_byte:node.end_byte]):
            count += 1
            owner = node.parent.parent.parent
            name = declared_name(node, source)
            if not preceding_doc(node, source) and not re.search(r'@(?:property|param)\s+' + re.escape(name) + r'\b', preceding_doc(owner, source)):
                failures.append(f'{path.relative_to(ROOT)}:{node.start_point.row+1}: undocumented constructor property {name}')
        elif required:
            count += 1
            doc = preceding_doc(node, source)
            if not doc:
                failures.append(f'{path.relative_to(ROOT)}:{node.start_point.row+1}: undocumented {node.type} {declared_name(node, source)}')
            elif node.type == 'function_declaration':
                parameters = next((n for n in node.named_children if n.type == 'function_value_parameters'), None)
                if parameters:
                    for parameter in parameters.named_children:
                        if parameter.type == 'parameter':
                            name = declared_name(parameter, source)
                            if not re.search(r'@param\s+' + re.escape(name) + r'\b', doc):
                                failures.append(f'{path.relative_to(ROOT)}:{node.start_point.row+1}: missing @param {name}')
                if '@return' not in doc:
                    failures.append(f'{path.relative_to(ROOT)}:{node.start_point.row+1}: missing @return contract')
    if path.name in DENSITY:
        comments = sum(source[n.start_byte:n.end_byte].count(b'\n') + 1 for n in walk(tree.root_node) if n.type in {'block_comment', 'line_comment'})
        code = sum(bool(line.strip()) for line in source.decode().splitlines()) - comments
        if comments * 8 < code:
            failures.append(f'{path.name}: {comments} comment lines for {code} code lines; minimum 1:8')
        print(f'{path.name}: {comments} comment lines / {code} nonblank code lines')
    return count, failures

def main():
    # Inspect every shipped source set; generated build output is deliberately outside this tree.
    total, errors = 0, []
    for path in sorted((ROOT / 'app/src').rglob('*.kt')):
        count, failures = audit(path)
        total += count
        errors += failures
    if errors:
        print('\n'.join(errors), file=sys.stderr)
        return 1
    print(f'PASS: {total} Kotlin declarations documented; five density thresholds met.')
    return 0
if __name__ == '__main__':
    sys.exit(main())
