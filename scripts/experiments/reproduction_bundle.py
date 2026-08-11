#!/usr/bin/env python3
"""Construye, verifica y extrae el artefacto de reproducción de Fase 8E."""

from __future__ import annotations

import argparse
import gzip
import hashlib
import json
import os
import shutil
import subprocess
import tarfile
import tempfile
from pathlib import Path
from typing import Any, Iterable

PROTOCOL_ID = "paper1-q3-v1"
EXPECTED_TASKS = 1272
EXPECTED_TABLES = 8
EXPECTED_FIGURES = 8


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(
        description="Gestiona el artefacto de reproducción independiente."
    )
    subparsers = parser.add_subparsers(dest="command", required=True)

    build = subparsers.add_parser(
        "build",
        help="Construye un artefacto de reproducción verificable.",
    )
    build.add_argument("--repository-root", required=True)
    build.add_argument("--raw-archive", required=True)
    build.add_argument("--raw-checksum", required=True)
    build.add_argument("--derived-dir", required=True)
    build.add_argument("--tables-dir", required=True)
    build.add_argument("--figures-dir", required=True)
    build.add_argument("--output-archive", required=True)

    verify = subparsers.add_parser(
        "verify",
        help="Verifica y extrae un artefacto de reproducción.",
    )
    verify.add_argument("--bundle", required=True)
    verify.add_argument("--output-dir", required=True)

    extract = subparsers.add_parser(
        "extract-raw",
        help="Extrae de forma segura el respaldo raw contenido en el artefacto.",
    )
    extract.add_argument("--archive", required=True)
    extract.add_argument("--output-dir", required=True)

    return parser.parse_args()


def calculate_sha256(path: Path) -> str:
    digest = hashlib.sha256()
    with path.open("rb") as stream:
        for chunk in iter(lambda: stream.read(1024 * 1024), b""):
            digest.update(chunk)
    return digest.hexdigest()


def load_json(path: Path) -> dict[str, Any]:
    try:
        value = json.loads(path.read_text(encoding="utf-8"))
    except FileNotFoundError as error:
        raise SystemExit(f"Falta el archivo JSON: {path}.") from error
    except json.JSONDecodeError as error:
        raise SystemExit(f"JSON inválido en {path}: {error}.") from error
    if not isinstance(value, dict):
        raise SystemExit(f"El archivo {path} debe contener un objeto JSON.")
    return value


def write_json(path: Path, value: Any) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    payload = json.dumps(value, ensure_ascii=False, indent=2, sort_keys=True) + "\n"
    path.write_text(payload, encoding="utf-8", newline="\n")


def read_expected_checksum(path: Path) -> str:
    try:
        text = path.read_text(encoding="utf-8").strip()
    except FileNotFoundError as error:
        raise SystemExit(f"Falta el checksum: {path}.") from error
    if not text:
        raise SystemExit(f"El checksum está vacío: {path}.")
    value = text.split()[0].lower()
    if len(value) != 64 or any(character not in "0123456789abcdef" for character in value):
        raise SystemExit(f"El checksum no contiene un SHA-256 válido: {path}.")
    return value


def require_directory(path: Path, field_name: str) -> None:
    if not path.is_dir():
        raise SystemExit(f"{field_name} no es un directorio: {path}.")


def require_file(path: Path, field_name: str) -> None:
    if not path.is_file():
        raise SystemExit(f"{field_name} no es un archivo: {path}.")


def current_commit(repository_root: Path) -> str:
    completed = subprocess.run(
        ["git", "-C", str(repository_root), "rev-parse", "HEAD"],
        check=False,
        capture_output=True,
        text=True,
    )
    if completed.returncode != 0:
        raise SystemExit("No se pudo identificar el commit del repositorio.")
    value = completed.stdout.strip()
    if len(value) != 40:
        raise SystemExit("El commit del repositorio no tiene el formato esperado.")
    return value


def tracked_changes(repository_root: Path) -> list[str]:
    completed = subprocess.run(
        [
            "git",
            "-C",
            str(repository_root),
            "status",
            "--porcelain",
            "--untracked-files=normal",
        ],
        check=False,
        capture_output=True,
        text=True,
    )
    if completed.returncode != 0:
        raise SystemExit("No se pudo verificar el estado del repositorio.")
    return [line for line in completed.stdout.splitlines() if line.strip()]


def copy_tree(source: Path, destination: Path) -> None:
    require_directory(source, "El origen de referencia")
    if destination.exists():
        raise SystemExit(f"El destino ya existe: {destination}.")
    shutil.copytree(source, destination)


def iter_files(root: Path) -> Iterable[Path]:
    return (
        path
        for path in sorted(root.rglob("*"), key=lambda item: item.as_posix())
        if path.is_file()
    )


def create_checksums(root: Path) -> dict[str, str]:
    checksums: dict[str, str] = {}
    for path in iter_files(root):
        relative = path.relative_to(root).as_posix()
        if relative == "checksums.sha256":
            continue
        checksums[relative] = calculate_sha256(path)
    lines = [f"{value}  {name}" for name, value in sorted(checksums.items())]
    (root / "checksums.sha256").write_text(
        "\n".join(lines) + "\n",
        encoding="utf-8",
        newline="\n",
    )
    return checksums


def normalized_tar_info(path: Path, arcname: str) -> tarfile.TarInfo:
    info = tarfile.TarInfo(arcname)
    stat = path.stat()
    info.mode = 0o644 if path.is_file() else 0o755
    info.uid = 0
    info.gid = 0
    info.uname = ""
    info.gname = ""
    info.mtime = 0
    if path.is_file():
        info.size = stat.st_size
        info.type = tarfile.REGTYPE
    else:
        info.size = 0
        info.type = tarfile.DIRTYPE
    return info


def write_deterministic_archive(source_root: Path, output_archive: Path) -> None:
    output_archive.parent.mkdir(parents=True, exist_ok=True)
    temporary = output_archive.with_name(f".{output_archive.name}.tmp")
    temporary.unlink(missing_ok=True)
    try:
        with temporary.open("wb") as raw_stream:
            with gzip.GzipFile(filename="", fileobj=raw_stream, mode="wb", mtime=0) as gzip_stream:
                with tarfile.open(fileobj=gzip_stream, mode="w") as archive:
                    directories = [source_root]
                    directories.extend(
                        path
                        for path in sorted(
                            source_root.rglob("*"),
                            key=lambda item: item.as_posix(),
                        )
                        if path.is_dir()
                    )
                    for path in directories:
                        relative = path.relative_to(source_root.parent).as_posix()
                        archive.addfile(normalized_tar_info(path, relative))
                    for path in iter_files(source_root):
                        relative = path.relative_to(source_root.parent).as_posix()
                        info = normalized_tar_info(path, relative)
                        with path.open("rb") as stream:
                            archive.addfile(info, stream)
        temporary.replace(output_archive)
    except Exception:
        temporary.unlink(missing_ok=True)
        raise


def safe_extract(archive_path: Path, output_dir: Path) -> None:
    require_file(archive_path, "El archivo TAR")
    output_dir.mkdir(parents=True, exist_ok=True)
    root = output_dir.resolve()
    try:
        with tarfile.open(archive_path, mode="r:gz") as archive:
            for member in archive.getmembers():
                target = (output_dir / member.name).resolve()
                try:
                    target.relative_to(root)
                except ValueError as error:
                    raise SystemExit(
                        f"El archivo contiene una ruta insegura: {member.name}."
                    ) from error
                if member.issym() or member.islnk():
                    raise SystemExit(
                        f"El archivo contiene un enlace no permitido: {member.name}."
                    )
            archive.extractall(output_dir, filter="data")
    except tarfile.TarError as error:
        raise SystemExit(f"El archivo TAR no es válido: {archive_path}.") from error


def find_bundle_root(output_dir: Path) -> Path:
    candidates = [
        path.parent
        for path in output_dir.rglob("bundle-manifest.json")
        if path.is_file()
    ]
    if len(candidates) != 1:
        raise SystemExit(
            "El artefacto debe contener un único bundle-manifest.json."
        )
    return candidates[0]


def verify_checksum_file(bundle_root: Path) -> dict[str, str]:
    checksum_path = bundle_root / "checksums.sha256"
    require_file(checksum_path, "El inventario de checksums")
    expected: dict[str, str] = {}
    for line_number, raw_line in enumerate(
        checksum_path.read_text(encoding="utf-8").splitlines(),
        start=1,
    ):
        line = raw_line.strip()
        if not line:
            continue
        parts = line.split(maxsplit=1)
        if len(parts) != 2:
            raise SystemExit(
                f"Línea de checksum inválida en {checksum_path}:{line_number}."
            )
        digest, relative_text = parts
        relative = relative_text.lstrip("* ")
        if relative.startswith("/") or ".." in Path(relative).parts:
            raise SystemExit(f"Ruta insegura en checksums: {relative}.")
        path = bundle_root / relative
        require_file(path, "El archivo enumerado")
        actual = calculate_sha256(path)
        if actual != digest.lower():
            raise SystemExit(f"El checksum no coincide para {relative}.")
        expected[relative] = digest.lower()
    actual_files = {
        path.relative_to(bundle_root).as_posix()
        for path in iter_files(bundle_root)
        if path.name != "checksums.sha256"
    }
    if actual_files != set(expected):
        missing = sorted(actual_files.difference(expected))
        extra = sorted(set(expected).difference(actual_files))
        raise SystemExit(
            "El inventario de checksums no coincide con el contenido. "
            f"Sin registrar={missing}, ausentes={extra}."
        )
    return expected



def validate_raw_archive(path: Path) -> None:
    require_file(path, "El respaldo raw")
    try:
        with tarfile.open(path, mode="r:gz") as archive:
            manifests = []
            for member in archive.getmembers():
                candidate = Path(member.name)
                if candidate.is_absolute() or ".." in candidate.parts:
                    raise SystemExit(
                        f"El respaldo raw contiene una ruta insegura: {member.name}."
                    )
                if member.issym() or member.islnk():
                    raise SystemExit(
                        f"El respaldo raw contiene un enlace no permitido: {member.name}."
                    )
                if candidate.name == "raw-manifest.json":
                    manifests.append(member.name)
                if any(part.endswith(".tlc-meta") for part in candidate.parts):
                    raise SystemExit(
                        "El respaldo raw contiene metadatos temporales de TLC."
                    )
            if len(manifests) != 1:
                raise SystemExit(
                    "El respaldo raw debe contener un único raw-manifest.json."
                )
    except tarfile.TarError as error:
        raise SystemExit(f"El respaldo raw no es un TAR válido: {path}.") from error


def validate_reference_files(
    derived_dir: Path,
    tables_dir: Path,
    figures_dir: Path,
) -> None:
    expected_derived = {
        "task-results.csv",
        "measured-results.csv",
        "exclusions.csv",
        "statistics.csv",
        "analysis-summary.json",
        "rq-findings.json",
        "rq-findings.md",
        "derived-manifest.json",
    }
    actual_derived = {path.name for path in derived_dir.iterdir() if path.is_file()}
    if actual_derived != expected_derived:
        raise SystemExit(
            "Los resultados derivados de referencia no coinciden con Fase 8D."
        )
    table_csv = list(tables_dir.glob("table-*.csv"))
    table_markdown = list(tables_dir.glob("table-*.md"))
    figure_svg = list(figures_dir.glob("figure-*.svg"))
    if len(table_csv) != EXPECTED_TABLES or len(table_markdown) != EXPECTED_TABLES:
        raise SystemExit("La referencia debe contener ocho tablas CSV y ocho Markdown.")
    if len(figure_svg) != EXPECTED_FIGURES:
        raise SystemExit("La referencia debe contener ocho figuras SVG.")

def validate_reference_manifest(derived_dir: Path) -> dict[str, Any]:
    manifest = load_json(derived_dir / "derived-manifest.json")
    if manifest.get("phase") != "8D":
        raise SystemExit("El manifiesto de referencia no declara Fase 8D.")
    if manifest.get("protocol_id") != PROTOCOL_ID:
        raise SystemExit("El manifiesto de referencia no coincide con el protocolo.")
    if manifest.get("status") != "complete":
        raise SystemExit("El análisis de referencia no está completo.")
    counts = manifest.get("counts", {})
    if counts.get("task_rows") != EXPECTED_TASKS:
        raise SystemExit("El análisis de referencia no contiene 1272 tareas.")
    if counts.get("tables") != EXPECTED_TABLES:
        raise SystemExit("El análisis de referencia no contiene ocho tablas.")
    if counts.get("figures") != EXPECTED_FIGURES:
        raise SystemExit("El análisis de referencia no contiene ocho figuras.")
    if manifest.get("raw_results_modified") is not False:
        raise SystemExit("El manifiesto de referencia no conserva raw.")
    return manifest


def build_bundle(args: argparse.Namespace) -> None:
    repository_root = Path(args.repository_root).resolve()
    raw_archive = Path(args.raw_archive).resolve()
    raw_checksum = Path(args.raw_checksum).resolve()
    derived_dir = Path(args.derived_dir).resolve()
    tables_dir = Path(args.tables_dir).resolve()
    figures_dir = Path(args.figures_dir).resolve()
    output_archive = Path(args.output_archive).resolve()

    require_directory(repository_root, "El repositorio")
    require_file(raw_archive, "El respaldo raw")
    require_file(raw_checksum, "El checksum raw")
    require_directory(derived_dir, "Los resultados derivados")
    require_directory(tables_dir, "Las tablas")
    require_directory(figures_dir, "Las figuras")

    changes = tracked_changes(repository_root)
    if changes:
        raise SystemExit(
            "El repositorio tiene cambios rastreados. Cree el artefacto desde un commit limpio."
        )

    validate_raw_archive(raw_archive)
    validate_reference_files(derived_dir, tables_dir, figures_dir)

    expected_raw = read_expected_checksum(raw_checksum)
    actual_raw = calculate_sha256(raw_archive)
    if expected_raw != actual_raw:
        raise SystemExit("El checksum del respaldo raw no coincide.")

    reference_manifest = validate_reference_manifest(derived_dir)
    source_commit = current_commit(repository_root)

    with tempfile.TemporaryDirectory(prefix="dtl-reproduction-bundle-") as temp_name:
        temp_root = Path(temp_name)
        bundle_root = temp_root / "paper1-q3-v1-reproduction"
        raw_root = bundle_root / "raw"
        reference_root = bundle_root / "reference"
        raw_root.mkdir(parents=True)
        reference_root.mkdir(parents=True)

        raw_target = raw_root / "paper1-q3-v1.tar.gz"
        checksum_target = raw_root / "paper1-q3-v1.tar.gz.sha256"
        shutil.copy2(raw_archive, raw_target)
        checksum_target.write_text(
            f"{actual_raw}  {raw_target.name}\n",
            encoding="utf-8",
            newline="\n",
        )

        copy_tree(derived_dir, reference_root / "derived")
        copy_tree(tables_dir, reference_root / "tables")
        copy_tree(figures_dir, reference_root / "figures")

        manifest = {
            "schema_version": 1,
            "phase": "8E",
            "protocol_id": PROTOCOL_ID,
            "status": "preparado",
            "source_phase": "8D",
            "source_commit": source_commit,
            "clean_repository_required": True,
            "full_matrix_rerun_required": False,
            "scientific_smoke_tasks": 6,
            "raw_archive": {
                "path": "raw/paper1-q3-v1.tar.gz",
                "sha256": actual_raw,
            },
            "reference": {
                "derived_manifest_sha256": calculate_sha256(
                    derived_dir / "derived-manifest.json"
                ),
                "task_rows": reference_manifest["counts"]["task_rows"],
                "tables": reference_manifest["counts"]["tables"],
                "figures": reference_manifest["counts"]["figures"],
                "comparison": "sha256_exacto",
            },
            "workflow": [
                "validar_ambiente",
                "instalar_herramientas_versionadas",
                "ejecutar_validacion_general",
                "ejecutar_smoke_cientifico",
                "regenerar_analisis_desde_raw",
                "comparar_hashes",
                "registrar_incidencias",
            ],
        }
        write_json(bundle_root / "bundle-manifest.json", manifest)
        checksums = create_checksums(bundle_root)
        manifest["bundle_files"] = len(checksums)
        write_json(bundle_root / "bundle-manifest.json", manifest)
        create_checksums(bundle_root)
        write_deterministic_archive(bundle_root, output_archive)

    checksum_output = output_archive.with_name(output_archive.name + ".sha256")
    checksum_output.write_text(
        f"{calculate_sha256(output_archive)}  {output_archive.name}\n",
        encoding="utf-8",
        newline="\n",
    )
    print(f"Artefacto de reproducción generado: {output_archive}.")
    print(f"Checksum generado: {checksum_output}.")


def verify_bundle(args: argparse.Namespace) -> None:
    bundle = Path(args.bundle).resolve()
    output_dir = Path(args.output_dir).resolve()
    if output_dir.exists() and any(output_dir.iterdir()):
        raise SystemExit("El directorio de extracción debe estar vacío.")
    safe_extract(bundle, output_dir)
    bundle_root = find_bundle_root(output_dir)
    verify_checksum_file(bundle_root)
    manifest = load_json(bundle_root / "bundle-manifest.json")
    if manifest.get("phase") != "8E":
        raise SystemExit("El artefacto no declara Fase 8E.")
    if manifest.get("protocol_id") != PROTOCOL_ID:
        raise SystemExit("El artefacto no coincide con el protocolo.")
    if manifest.get("status") != "preparado":
        raise SystemExit("El artefacto no está preparado para reproducción.")
    raw_path = bundle_root / str(manifest.get("raw_archive", {}).get("path", ""))
    require_file(raw_path, "El respaldo raw del artefacto")
    if calculate_sha256(raw_path) != manifest.get("raw_archive", {}).get("sha256"):
        raise SystemExit("El respaldo raw no coincide con el manifiesto del artefacto.")
    validate_reference_manifest(bundle_root / "reference/derived")
    print(bundle_root)


def extract_raw(args: argparse.Namespace) -> None:
    archive = Path(args.archive).resolve()
    output_dir = Path(args.output_dir).resolve()
    if output_dir.exists() and any(output_dir.iterdir()):
        raise SystemExit("El directorio raw de extracción debe estar vacío.")
    safe_extract(archive, output_dir)
    manifests = sorted(output_dir.rglob("raw-manifest.json"))
    if len(manifests) != 1:
        raise SystemExit("El respaldo raw debe contener un único raw-manifest.json.")
    run_dir = manifests[0].parent
    print(run_dir)


def main() -> None:
    args = parse_args()
    if args.command == "build":
        build_bundle(args)
    elif args.command == "verify":
        verify_bundle(args)
    else:
        extract_raw(args)


if __name__ == "__main__":
    main()
