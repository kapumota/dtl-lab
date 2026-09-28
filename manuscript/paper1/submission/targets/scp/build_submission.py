#!/usr/bin/env python3
"""Construye el candidato editorial SCP sin ejecutar ciencia."""

import argparse
import hashlib
import json
import os
from pathlib import Path
import shutil
import subprocess
import zipfile


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--output', required=True)
    parser.add_argument('--latex-support', type=Path)
    args = parser.parse_args()
    paper = Path(__file__).resolve().parents[3]
    root = paper.parents[1]
    target = paper / 'submission/targets/scp'
    output = Path(args.output).resolve()
    if output.exists():
        raise SystemExit('La carpeta de salida ya existe. Use una carpeta nueva.')
    source = output / 'source'
    source.mkdir(parents=True)
    files = [paper / 'main.tex', paper / 'references.bib']
    files += sorted((paper / 'sections').glob('*.tex'))
    files += sorted((target / 'declarations').glob('*.tex'))
    for path in files:
        dest = source / path.relative_to(paper)
        dest.parent.mkdir(parents=True, exist_ok=True)
        shutil.copyfile(path, dest)
    support = {}
    if args.latex_support:
        for name in ['elsarticle.cls', 'elsarticle-num.bst', 'LICENSE']:
            path = args.latex_support / name
            if path.is_file():
                shutil.copyfile(path, source / name)
                support[name] = hashlib.sha256(path.read_bytes()).hexdigest()
    commit = subprocess.check_output(['git', 'rev-parse', 'HEAD'], cwd=root, text=True).strip()
    epoch = subprocess.check_output(['git', 'show', '-s', '--format=%ct', 'e41ef499a645107dbbae071508464e6183669a6f'], cwd=root, text=True).strip()
    env = dict(os.environ, SOURCE_DATE_EPOCH=epoch, FORCE_SOURCE_DATE='1', TZ='UTC')
    with (output / 'compile.log').open('w') as log:
        subprocess.run(['latexmk', '-pdf', '-interaction=nonstopmode', '-halt-on-error', 'main.tex'],
                       cwd=source, env=env, stdout=log, stderr=subprocess.STDOUT, check=True)
    shutil.copyfile(source / 'main.pdf', output / 'SCP_manuscript_CANDIDATE.pdf')
    shutil.copyfile(paper / 'highlights.txt', output / 'Highlights.txt')
    shutil.copyfile(target / 'SCP_COVER_LETTER_DRAFT.md', output / 'Cover_Letter_SCP_DRAFT.md')
    shutil.copyfile(target / 'declarations/SCP_DATA_AVAILABILITY_DRAFT.md', output / 'SCP_DATA_AVAILABILITY.md')
    shutil.copyfile(target / 'declarations/SCP_AUTHOR_CONFIRMATIONS.md', output / 'AUTHOR_CONFIRMATIONS_REQUIRED.md')
    shutil.copyfile(target / 'declarations/SCP_AI_DECLARATION.tex', output / 'SCP_AI_DECLARATION.tex')
    with zipfile.ZipFile(output / 'SCP_latex_source_CANDIDATE.zip', 'w', zipfile.ZIP_DEFLATED) as archive:
        for path in sorted(source.rglob('*')):
            if path.is_file() and (path.suffix in {'.tex', '.bib', '.bbl', '.cls', '.bst'} or path.name == 'LICENSE'):
                info = zipfile.ZipInfo(path.relative_to(source).as_posix(), (2026, 9, 25, 0, 0, 0))
                info.compress_type = zipfile.ZIP_DEFLATED
                archive.writestr(info, path.read_bytes())
    metadata = {
        'status': 'NOT_READY_TO_SUBMIT',
        'source_commit': commit,
        'working_tree_status': subprocess.check_output(['git', 'status', '--porcelain'], cwd=root, text=True),
        'scientific_freeze_commit': '06bea7de70971d5b22d705a2df19137122758c08',
        'protocol_id': 'paper1-q3-v1',
        'source_date_epoch': epoch,
        'latex_support_sha256': support,
        'scientific_executions': 0,
        'source_file_sha256': {str(p.relative_to(paper)): hashlib.sha256(p.read_bytes()).hexdigest() for p in files},
    }
    (output / 'BUILD_PROVENANCE.json').write_text(json.dumps(metadata, ensure_ascii=False, indent=2) + '\n')
    checksum_paths = sorted(p for p in output.iterdir() if p.is_file())
    (output / 'SHA256SUMS.txt').write_text(''.join(
        f'{hashlib.sha256(p.read_bytes()).hexdigest()}  {p.name}\n' for p in checksum_paths))
    print('Candidato editorial generado. No se declara listo para envío.')


if __name__ == '__main__':
    main()
