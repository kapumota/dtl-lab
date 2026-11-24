#!/usr/bin/env python3
"""Audita íntegramente una campaña G3-v1 sin modificarla ni ejecutar Java.

Solo interpreta evidencia real que exista en el directorio. Las pruebas PASS
no constituyen demostración de suficiencia del oráculo; los FAIL no se
atribuyen automáticamente a un defecto de implementación.
"""

import argparse
import csv
import hashlib
import json
from pathlib import Path, PurePosixPath
import re
import tarfile
import tempfile

BASELINE = "e4422ae483ab0eb4c0af48bbb58be1c49c8cf816"
EXPECTED_TESTS = (
    "HIST_PROTOCOL", "HIST_ROLLBACK", "HIST_ISOLATION", "G1_AUDIT",
    "G2_BOUNDARY", "G2_INTERMEDIATE", "G2_DECISION", "G3_RECIPIENT",
    "G3_ROLLBACK", "G3_CONTENTION",
)
SCENARIOS = {
    "beneficiario": "G3_RECIPIENT", "rollback": "G3_ROLLBACK",
    "contencion": "G3_CONTENTION", "aislamiento": "HIST_ISOLATION",
}
MAX_FILES = 1000
MAX_BYTES = 200 * 1024 * 1024


def sha256(path):
    h = hashlib.sha256()
    with path.open("rb") as handle:
        for piece in iter(lambda: handle.read(1048576), b""):
            h.update(piece)
    return h.hexdigest()


def parse_archive(archivo, destination):
    total, count = 0, 0
    with tarfile.open(archivo, "r:gz") as stream:
        for entry in stream:
            pos = PurePosixPath(entry.name)
            if pos.is_absolute() or ".." in pos.parts or not pos.parts:
                raise ValueError("Ruta insegura dentro del TAR")
            if pos.parts[0] not in {"dtl-g3-campana-completa", "dtl-g3-verificacion-sha256.txt"}:
                raise ValueError("Contenido inesperado dentro del TAR")
            if not (entry.isfile() or entry.isdir()):
                raise ValueError("Enlace o tipo de entrada no permitido en el TAR")
            count += 1
            total += entry.size
            if count > MAX_FILES or total > MAX_BYTES:
                raise ValueError("El TAR excede los límites de seguridad")
            ruta = destination.joinpath(*pos.parts)
            if entry.isdir():
                ruta.mkdir(parents=True, exist_ok=True)
            else:
                if ruta.exists():
                    raise ValueError("Archivo duplicado en el TAR")
                ruta.parent.mkdir(parents=True, exist_ok=True)
                fuente = stream.extractfile(entry)
                if fuente is None:
                    raise ValueError("No se pudo leer una entrada del TAR")
                with ruta.open("wb") as salida:
                    import shutil
                    shutil.copyfileobj(fuente, salida)
    base = destination / "dtl-g3-campana-completa"
    if not base.is_dir():
        raise ValueError("El TAR no contiene la campaña G3 esperada")
    return base


def verify_manifest(base):
    manifest = base / "SHA256SUMS.txt"
    if not manifest.is_file():
        raise ValueError("Falta SHA256SUMS.txt")
    indexed = set()
    for line in manifest.read_text(encoding="utf-8").splitlines():
        if not line.strip():
            continue
        match = re.fullmatch(r"([0-9a-f]{64})  (.+)", line)
        if not match:
            raise ValueError("Formato de manifiesto incorrecto")
        digest, relative = match.groups()
        fragment = PurePosixPath(relative)
        if fragment.is_absolute() or ".." in fragment.parts or relative == "SHA256SUMS.txt":
            raise ValueError("Ruta prohibida en el manifiesto")
        if relative in indexed:
            raise ValueError("Archivo duplicado en el manifiesto")
        indexed.add(relative)
        file = base.joinpath(*fragment.parts)
        if not file.is_file() or file.is_symlink() or sha256(file) != digest:
            raise ValueError("Archivo ausente o hash incorrecto: " + relative)
    real = {str(p.relative_to(base)) for p in base.rglob("*") if p.is_file()}
    real.discard("SHA256SUMS.txt")
    if indexed != real:
        raise ValueError("El manifiesto no cubre todos los archivos: " +
                         str(sorted(indexed.symmetric_difference(real))[:12]))
    return len(indexed), sha256(manifest)


def read_json(base, name):
    return json.loads((base / name).read_text(encoding="utf-8"))


def diagnostic_text(base, mutant, test):
    text = ""
    for ext in ("stderr", "stdout"):
        path = base / mutant / f"{test}.{ext}.log"
        if not path.is_file():
            raise ValueError("Falta log de prueba: " + str(path))
        text += "\n" + path.read_text(encoding="utf-8", errors="replace")
    return text


def evaluate(base):
    size, manifest_sha = verify_manifest(base)
    cfg = read_json(base, "G3_PRE_REGISTRO_COPIA.json")
    env = read_json(base, "AMBIENTE.json")
    results = read_json(base, "resultado_integral.json")
    raw = read_json(base, "RESUMEN_BRUTO.json")
    if cfg.get("source_commit") != BASELINE or env.get("commit_fuente_worktree") != BASELINE:
        raise ValueError("Commit de origen divergente")
    cfg_path = base / "G3_PRE_REGISTRO_COPIA.json"
    if sha256(cfg_path) != env.get("config_sha256"):
        raise ValueError("La configuración original no coincide con la procedencia")
    specs = cfg["mutation_rules"]
    ids = [s["id"] for s in specs]
    if len(ids) != 10 or len(set(ids)) != 10:
        raise ValueError("La campaña no contiene los diez mutantes originales")
    if [r["id"] for r in results] != ["CONTROL"] + ids:
        raise ValueError("Orden o identidad inesperada en resultado_integral.json")
    if [r["id"] for r in raw] != ["CONTROL"] + ids:
        raise ValueError("El resumen bruto no corresponde con la campaña completa")
    with (base / "matriz_detector_mutante.csv").open(encoding="utf-8", newline="") as f:
        table = list(csv.DictReader(f))
    expected_nrows = sum(len(r["pruebas"]) for r in results)
    if len(table) != expected_nrows:
        raise ValueError(f"Filas CSV esperadas {expected_nrows}, encontradas {len(table)}")
    by_id = {}
    for r in results:
        tests = {p["test_id"]: p for p in r["pruebas"]}
        if not r.get("compila"):
            if tests:
                raise ValueError("El mutante sin compilar no debe registrar pruebas: " + r["id"])
        elif len(tests) != 10 or set(tests) != set(EXPECTED_TESTS):
            raise ValueError("Pruebas incompletas o duplicadas: " + r["id"])
        for name, p in tests.items():
            diagnostic_text(base, r["id"], name)
        meta = read_json(base, r["id"] + "/procedencia.json")
        if meta.get("source_sha") != BASELINE or meta.get("sonda_sha256") != env.get("sonda_sha256"):
            raise ValueError("Procedencia del worktree inconsistente: " + r["id"])
        by_id[r["id"]] = tests
    if any(p["estado"] != "PASS" for p in by_id["CONTROL"].values()):
        raise ValueError("El control original tiene pruebas que no pasan")
    expected_rows = {(r["id"], p["test_id"]): p for r in results for p in r["pruebas"]}
    rows_csv = {}
    for row in table:
        key = (row["mutante"], row["detector"])
        if key in rows_csv or key not in expected_rows:
            raise ValueError("Fila CSV duplicada o extraña: " + str(key))
        rows_csv[key] = row
        if row["resultado"] != expected_rows[key]["estado"]:
            raise ValueError("Inconsistencia entre CSV y JSON: " + str(key))
    if set(rows_csv) != set(expected_rows):
        raise ValueError("Faltan filas del CSV de campaña")
    observations = []
    analyses = []
    for spec in specs:
        sid = spec["id"]
        expected = spec.get("expected_scenario")
        test = SCENARIOS.get(expected)
        if not test:
            raise ValueError("Escenario registrado desconocido: " + str(expected))
        record = next(r for r in results if r["id"] == sid)
        annotated = {t["test_id"]: t for t in record["pruebas"]}
        witness = annotated.get(test)
        markers = [p["test_id"] for p in record["pruebas"]
                   if any("G3_ALCANZADO:" + sid in mark for mark in p["marcas_alcance"])]
        targeted_reached = test in markers
        diff = (base / sid / "mutacion_java.diff").read_text(encoding="utf-8")
        if "G3_ALCANZADO:" + sid not in diff or "diff --git" not in diff:
            raise ValueError("La mutación Java no corresponde al ID: " + sid)
        meta = read_json(base, sid + "/procedencia.json")
        if meta.get("mutacion", {}).get("ruta") != spec.get("file"):
            raise ValueError("Ruta de mutación inconsistente: " + sid)
        failures = [p["test_id"] for p in record["pruebas"] if p["estado"] != "PASS"]
        v2 = witness.get("veredictos_g2", []) if witness is not None else []
        witness_logs = diagnostic_text(base, sid, test) if witness is not None else ""
        errors = [x.strip()[:240] for x in witness_logs.splitlines()
                  if any(k in x for k in ("AssertionError", "Exception", "ERROR", "FAIL", "G3_ALCANZADO"))][:12]
        special = test == "HIST_ISOLATION"
        if not record["compila"]:
            state = "NO_COMPILA_NO_EVALUABLE"
        elif not markers:
            state = "NO_ALCANZADO_EN_LA_BATERIA"
        elif not targeted_reached:
            state = "ALCANZADO_SOLO_FUERA_DEL_ESCENARIO_PREVISTO"
        elif witness["estado"] == "TIMEOUT":
            state = "TIMEOUT_NO_ATRIBUIDO"
        elif witness["estado"] == "FAIL":
            state = "FALLO_DEL_ESCENARIO_PREVISTO_ATRIBUCION_PENDIENTE"
        else:
            state = "ESCENARIO_PREVISTO_PASA_SIN_EVIDENCIA_DE_DETECCION"
        # Evidencia específica de M02, confirmable por mensajes; sigue sin probar superioridad metodológica.
        if sid == "G3-M02" and targeted_reached and witness is not None and witness["estado"] == "FAIL":
            if "Se acreditó al destinatario equivocado" in witness_logs and \
                    "G3_G2_VEREDICTO:DIVERGENTE" in witness_logs:
                state = "M02_DESVIACION_TITULARIDAD_Y_G2_DIVERGENTE"
        entry = {
            "mutante": sid, "categoria": spec["category"],
            "escenario_declarado": expected, "prueba_del_escenario": test,
            "compila": record["compila"], "marcador_en_prueba_prevista": targeted_reached,
            "pruebas_que_alcanzaron_el_codigo": markers,
            "estado_escenario": witness["estado"] if witness is not None else "NO_COMPILA",
            "veredicto_g2_en_sonda": v2,
            "fallos_de_bateria": failures,
            "diagnosticos": errors,
            "clasificacion": state,
            "comparacion_mismo_caso_control_mutante": True,
            "comparacion_transversal_entre_detectores": "NO_VALIDADA_ESCENARIOS_DISTINTOS",
            "componente_g1_g2_compartido": True,
            "testigo_aislamiento_especializado": special,
            "evidencia_de_crash_recovery": False,
        }
        analyses.append(entry)
        for name in (EXPECTED_TESTS if record["compila"] else ()):
            control, mutant = by_id["CONTROL"][name], annotated[name]
            observations.append({
                "mutante": sid, "prueba": name, "grupo": mutant["grupo"],
                "escenario_predefinido": test, "misma_prueba_control": True,
                "estado_control": control["estado"], "estado_mutante": mutant["estado"],
                "alcance_en_esta_prueba": name in markers,
                "veredicto_g2": ";".join(mutant.get("veredictos_g2", [])),
                "interpretacion": "FALLO_REQUIERE_ATRIBUCION" if mutant["estado"] != "PASS"
                    else "SIN_FALLO_EN_ESTA_PRUEBA",
            })
    return {
        "fuente": str(base), "sha256_manifesto": manifest_sha,
        "archivos_verificados": size, "commit_congelado": BASELINE,
        "controles": len(EXPECTED_TESTS), "mutantes": len(analyses),
        "comparacion_cruzada_valida": False,
        "razon": "Los detectores de G3-v1 usan escenarios distintos; G2 y G3 comparten journal.",
        "m02_puede_ser_comparado_con_su_control": True,
        "conclusion_editorial": "NO_GO_PARA_CLAIM_METODOLOGICO_SOLO_CON_G3V1",
        "observaciones": observations, "mutantes_analizados": analyses,
    }


def save(report, out):
    out.mkdir(parents=True, exist_ok=False)
    (out / "auditoria_g3v1.json").write_text(json.dumps(report, indent=2, ensure_ascii=False) + "\n", encoding="utf-8")
    with (out / "matriz_observacional_g3v1.csv").open("w", encoding="utf-8", newline="") as f:
        names = list(report["observaciones"][0])
        writer = csv.DictWriter(f, fieldnames=names)
        writer.writeheader()
        writer.writerows(report["observaciones"])
    lines = ["# Dictamen de G3-v1 basado en evidencia congelada", "",
             "Esta auditoría no ejecuta ni modifica Java, TLC, Alloy ni los experimentos V1.",
             "", f"- Integridad: {report['archivos_verificados']} archivos SHA-256 verificados.",
             f"- Baseline: `{report['commit_congelado']}`.",
             "- Control: 10 pruebas PASS antes de introducir mutantes.",
             "- Comparación entre detectores: NO CERTIFICADA, escenarios diferentes.",
             "- Dependencia entre G2 y G3: existe, no afirmar independencia.",
             "- Decisión metodológica: NO GO con esta campaña aislada.", "",
             "| Mutante | Escenario | Alcance focal | Resultado focal | Estado científico preliminar |",
             "|---|---|---|---|---|"]
    for m in report["mutantes_analizados"]:
        lines.append(f"| {m['mutante']} | {m['escenario_declarado']} | "
                     f"{m['marcador_en_prueba_prevista']} | {m['estado_escenario']} | "
                     f"{m['clasificacion']} |")
    lines += ["", "Ningún porcentaje global de detección puede inferirse automáticamente de los fallos.",
              "Para cerrar atribuciones deben examinarse los logs de cada mutante y su diff.",
              "M02 puede describirse como caso específico de identidad del beneficiario.",
              "G3-v1 no prueba tolerancia a crashes, liveness ni atomicidad distribuida general.", ""]
    (out / "DICTAMEN_G3V1.md").write_text("\n".join(lines), encoding="utf-8")
    checksum_lines = []
    for file in sorted(out.iterdir()):
        if file.is_file():
            checksum_lines.append(f"{sha256(file)}  {file.name}")
    (out / "SHA256SUMS.txt").write_text("\n".join(checksum_lines) + "\n", encoding="utf-8")


def main():
    parser = argparse.ArgumentParser(description="Auditar G3-v1 sin repetir experimentos")
    parser.add_argument("--entrada", type=Path, required=True,
                        help="Directorio dtl-g3-campana-completa o TAR original")
    parser.add_argument("--salida", type=Path, required=True,
                        help="Directorio NUEVO fuera de la evidencia congelada")
    args = parser.parse_args()
    entrada = args.entrada.expanduser().resolve()
    salida = args.salida.expanduser().resolve()
    if not entrada.exists() or salida.exists():
        parser.error("La entrada debe existir y la salida no debe existir")
    try:
        if entrada.is_dir():
            if entrada == salida or entrada in salida.parents:
                raise ValueError("La salida debe quedar fuera de la entrada")
            report = evaluate(entrada)
        else:
            with tempfile.TemporaryDirectory(prefix="dtl-g3v1-lectura-") as temporal:
                base = parse_archive(entrada, Path(temporal))
                report = evaluate(base)
                report["fuente"] = str(entrada)
        save(report, salida)
    except (OSError, ValueError, KeyError, TypeError, json.JSONDecodeError, tarfile.TarError) as error:
        parser.exit(2, "ERROR_AUDITORIA_G3: " + str(error) + "\n")
    print("INTEGRIDAD_PASS:", report["archivos_verificados"], "archivos")
    print("MUTANTES_ANALIZADOS:", len(report["mutantes_analizados"]))
    for m in report["mutantes_analizados"]:
        print(m["mutante"], m["clasificacion"])
    print("DECISION:", report["conclusion_editorial"])
    print("DICTAMEN:", salida / "DICTAMEN_G3V1.md")


if __name__ == "__main__":
    main()
