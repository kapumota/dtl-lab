#!/usr/bin/env python3
"""G3-v2: comparación control/mutante y oráculos sobre un mismo escenario Java.

Campaña NUEVA, separada e inmutable respecto a G3-v1; no debe ejecutarse
sin cerrar primero la auditoría de la evidencia congelada G3-v1.
"""

import argparse
import csv
import hashlib
import json
from pathlib import Path
import platform
import shutil
import sys
import tempfile
import time

HERE = Path(__file__).resolve().parent
G3 = HERE.parent / "g3"
sys.path.insert(0, str(G3))
import ejecutar_g3 as legacy  # noqa: E402

JAVA = HERE / "G3ComparadorMismoEscenario.java"
DEST = Path("src/test/java/dltlab/research/paper1/g3v2/G3ComparadorMismoEscenario.java")
CONFIG = G3 / "g3_mutants.json"
SCENARIOS = {"beneficiario", "rollback", "contencion", "aislamiento"}
LEVELS = {"TERMINAL", "VALOR", "ESTRUCTURA_UTXO", "TITULARIDAD", "RECIBOS", "BLOQUEOS",
          "G2_JOURNAL", "SALUD_G1_NO_DETECTOR"}
DETECTORES = LEVELS - {"SALUD_G1_NO_DETECTOR"}
ASERCION_AISLAMIENTO = "java.lang.AssertionError: El lector accedio al ledger antes de liberar al escritor."
BRANCH = "research/paper1-editor-o1-o3"
BASELINE = "e4422ae483ab0eb4c0af48bbb58be1c49c8cf816"


def parse_lines(stdout, scenario, exit_status, stderr=""):
    if scenario == "aislamiento":
        if exit_status == 0:
            estado = "PASS"
        elif exit_status is not None and ASERCION_AISLAMIENTO in stderr:
            estado = "FAIL"
        else:
            estado = "ERROR_EJECUCION"
        return {"AISLAMIENTO_P1C": {
            "estado": estado,
            "detalle": "Contrato de exclusion mutua P1C; retorno=" + str(exit_status)}}
    result = {}
    for line in stdout.splitlines():
        if not line.startswith("G3V2|"):
            continue
        fields = line.split("|", 4)
        if len(fields) != 5 or fields[1] != scenario or fields[2] not in LEVELS:
            raise ValueError("Registro estructurado G3-v2 inválido: " + line[:160])
        _, _, level, status, detail = fields
        if level in result or status not in {"PASS", "FAIL", "NO_EVALUABLE", "ERROR_OBSERVADOR"}:
            raise ValueError("Nivel duplicado o estado inválido: " + line[:160])
        result[level] = {"estado": status, "detalle": detail}
    if set(result) != LEVELS:
        raise ValueError(f"Faltan niveles de comparación en {scenario}: {sorted(LEVELS-set(result))}")
    return result


def run_scenario(worktree, target, scenario, timeout, mutante_id=None):
    if scenario == "aislamiento":
        cls, argv = "dltlab.sharding.protocol.P1CIsolationTest", []
    else:
        cls = "dltlab.research.paper1.g3v2.G3ComparadorMismoEscenario"
        argv = [scenario]
    command = ["java", "-cp", "build/classes:build/test-classes", cls, *argv]
    t0 = time.monotonic()
    try:
        out = legacy.shell(command, worktree, timeout)
        stdout, stderr, status = out.stdout, out.stderr, out.returncode
        try:
            result = parse_lines(stdout, scenario, status, stderr)
        except ValueError as error:
            result = {"EJECUCION_JAVA": {
                "estado": "SALIDA_INVALIDA" if status == 0 else "ERROR_EJECUCION",
                "detalle": f"retorno={status}; salida incompleta: {error}"}}
        # No borrar los veredictos de los observadores por un retorno Java anómalo.
        # Ese retorno se conserva separado y bloquea la atribución automática.
        if status != 0 and scenario != "aislamiento":
            result["EJECUCION_JAVA"] = {
                "estado": "ERROR_EJECUCION", "detalle": f"retorno={status}"}
    except Exception as error:
        if type(error).__name__ == "TimeoutExpired":
            stdout = getattr(error, "stdout", b"") or b""
            stderr = getattr(error, "stderr", b"") or b""
            if isinstance(stdout, bytes):
                stdout = stdout.decode(errors="replace")
            if isinstance(stderr, bytes):
                stderr = stderr.decode(errors="replace")
            status = None
            result = {"EJECUCION": {"estado": "TIMEOUT", "detalle": str(error)}}
        else:
            raise
    legacy.guardar(target / f"{scenario}.stdout.log", stdout)
    legacy.guardar(target / f"{scenario}.stderr.log", stderr)
    marcas = [line.strip() for line in stderr.splitlines()
              if line.strip().startswith("G3_ALCANZADO:")]
    esperado = f"G3_ALCANZADO:{mutante_id}" if mutante_id else None
    return {"escenario": scenario, "codigo_retorno": status,
            "segundos": round(time.monotonic()-t0, 3), "niveles": result,
            "marcas_alcance": marcas,
            "alcance": esperado in marcas if esperado else False}


def run_case(repo, sha, spec, scenarios, out, timeout):
    label = "CONTROL" if spec is None else spec["id"]
    folder = out / label
    folder.mkdir(parents=True, exist_ok=False)
    with tempfile.TemporaryDirectory(prefix="dtl-g3v2-worktree-") as temp:
        worktree = Path(temp) / "repo"
        legacy.checked(["git", "worktree", "add", "--detach", str(worktree), sha], repo)
        try:
            if (worktree / DEST).exists():
                raise RuntimeError("La clase G3-v2 ya existe en el commit fuente")
            destination = worktree / DEST
            destination.parent.mkdir(parents=True, exist_ok=True)
            shutil.copy2(JAVA, destination)
            meta = {"baseline": sha, "comparador_sha256": legacy.checksum(JAVA),
                    "config_sha256": legacy.checksum(CONFIG), "id": label}
            if spec:
                meta["mutacion"] = legacy.mutar(worktree, spec)
                diff = legacy.checked(["git", "diff", "--", spec["file"]], worktree)
                legacy.guardar(folder / "mutacion_java.diff", diff + "\n")
            legacy.guardar(folder / "procedencia.json", json.dumps(meta, indent=2, ensure_ascii=False)+"\n")
            compilado = legacy.compilar(worktree, folder)
            if not compilado:
                return {"id": label, "compila": False, "escenarios": []}
            return {"id": label, "compila": True,
                    "escenarios": [run_scenario(worktree, folder, s, timeout,
                                               spec["id"] if spec else None) for s in scenarios]}
        finally:
            legacy.checked(["git", "worktree", "remove", "--force", str(worktree)], repo)


def main():
    parser = argparse.ArgumentParser(description="G3-v2: nueva campaña de comparación justa")
    parser.add_argument("--repo", type=Path, required=True)
    parser.add_argument("--out", type=Path, required=True)
    group = parser.add_mutually_exclusive_group(required=True)
    group.add_argument("--solo", help="Mutante particular para piloto")
    group.add_argument("--todos", action="store_true", help="Todos los diez mutantes")
    parser.add_argument("--timeout", type=int, default=70)
    parser.add_argument("--autorizar-nueva-campana", action="store_true",
                        help="Reconoce explícitamente que es evidencia nueva, separada de G3-v1")
    args = parser.parse_args()
    if not args.autorizar_nueva_campana:
        parser.error("No se ejecuta G3-v2 sin autorización explícita; audite G3-v1 primero")
    repo, out = args.repo.expanduser().resolve(), args.out.expanduser().resolve()
    if out.exists() or repo == out or repo in out.parents:
        parser.error("Salida ya existente o dentro del repositorio")
    if args.timeout <= 0 or not JAVA.is_file() or not CONFIG.is_file():
        parser.error("Timeout o archivos de campaña inválidos")
    if legacy.checked(["git", "branch", "--show-current"], repo) != BRANCH:
        parser.error("Rama incorrecta; la rama editorial no se modifica")
    legacy.checked(["git", "cat-file", "-e", BASELINE + "^{commit}"], repo)
    if legacy.shell(["git", "merge-base", "--is-ancestor", BASELINE, "HEAD"], repo).returncode:
        parser.error("La rama no desciende del baseline congelado")
    config = json.loads(CONFIG.read_text(encoding="utf-8"))
    if config.get("source_commit") != BASELINE or config.get("branch_local") != BRANCH:
        parser.error("La configuración histórica no corresponde al baseline")
    mutations = config["mutation_rules"]
    if args.solo:
        mutations = [s for s in mutations if s["id"] == args.solo]
        if not mutations:
            parser.error("ID de mutante no registrado")
    if any(s.get("expected_scenario") not in SCENARIOS for s in mutations):
        parser.error("Escenarios de mutante inválidos")
    scenarios = sorted({s["expected_scenario"] for s in mutations})
    out.mkdir(parents=True)
    context = {"version": "g3v2-mismo-escenario-2.1", "baseline": BASELINE,
               "fecha": time.strftime("%Y-%m-%dT%H:%M:%S%z"), "rama": BRANCH,
               "java_sha256": legacy.checksum(JAVA), "config_sha256": legacy.checksum(CONFIG),
               "python": sys.version, "plataforma": platform.platform(),
               "cobertura": "Solo escenarios predeclarados; no mide crash ni prueba refinamiento"}
    legacy.guardar(out / "AMBIENTE_V2.json", json.dumps(context, indent=2, ensure_ascii=False)+"\n")
    legacy.guardar(out / "G3V2_REGLAS_FIJADAS.json", json.dumps([{
        "id": s["id"], "escenario": s["expected_scenario"], "categoria": s["category"]}
        for s in mutations], indent=2, ensure_ascii=False)+"\n")
    results = []
    try:
        print("G3-v2 CONTROL: escenarios", ", ".join(scenarios), flush=True)
        ctrl = run_case(repo, BASELINE, None, scenarios, out, args.timeout)
        results.append(ctrl)
        if not ctrl["compila"] or any(
                s["codigo_retorno"] != 0 or s["marcas_alcance"]
                or set(s["niveles"]) != (LEVELS if s["escenario"] != "aislamiento" else {"AISLAMIENTO_P1C"})
                or any(v["estado"] != "PASS" for v in s["niveles"].values())
                for s in ctrl["escenarios"]):
            legacy.guardar(out / "NO_GO_CONTROL.txt", "El control no pasó, no se ejecutan mutantes\n")
            return 2
        for spec in mutations:
            print("G3-v2 MUTANTE:", spec["id"], spec["expected_scenario"], flush=True)
            results.append(run_case(repo, BASELINE, spec, [spec["expected_scenario"]], out, args.timeout))
        rows = []
        by_scenario = {s["escenario"]: s for s in ctrl["escenarios"]}
        for res in results[1:]:
            spec = next(s for s in mutations if s["id"] == res["id"])
            scenario = spec["expected_scenario"]
            if not res["compila"]:
                rows.append([res["id"], scenario, "COMPILACION", "PASS", "NO_COMPILA", "NO_EVALUABLE", "no"])
                continue
            mutated = res["escenarios"][0]
            control = by_scenario[scenario]
            for level, item in mutated["niveles"].items():
                ctrl_state = control["niveles"].get(level, {}).get("estado", "NO_EVALUABLE")
                detected = "no_certificado"
                proceso_valido = mutated["codigo_retorno"] == 0 or (
                    scenario == "aislamiento" and level == "AISLAMIENTO_P1C"
                    and item["estado"] == "FAIL")
                detector_admisible = level in DETECTORES or level == "AISLAMIENTO_P1C"
                if (mutated["alcance"] and proceso_valido and detector_admisible
                        and item["estado"] == "FAIL" and ctrl_state == "PASS"):
                    detected = ("candidato_dependiente_journal_G1" if level == "G2_JOURNAL"
                                else "candidato_revisar_log")
                rows.append([res["id"], scenario, level, ctrl_state, item["estado"], detected,
                             "si" if mutated["alcance"] else "no"])
        with (out / "COMPARACION_MISMO_ESCENARIO.csv").open("w", newline="", encoding="utf-8") as fh:
            w = csv.writer(fh)
            w.writerow(["mutante", "escenario", "nivel", "control", "mutado", "atribucion",
                        "marcador_en_escenario"])
            w.writerows(rows)
        legacy.guardar(out / "DICTAMEN_V2_PRELIMINAR.txt",
            "Campaña nueva, separada de V1 y G3-v1.\n"
            "Comparación control/mutante dentro del mismo escenario Java.\n"
            "G2 depende del journal G1, SALUD_G1 no es detector semántico.\n"
            "Los fallos requieren atribución manual en los logs.\n"
            "AISLAMIENTO_P1C no es una ablación G2/G3.\n"
            "Sin afirmaciones de crash recovery o atomicidad distribuida general.\n")
        return 0
    finally:
        legacy.guardar(out / "RESULTADOS_BRUTOS_V2.json", json.dumps(results, indent=2, ensure_ascii=False)+"\n")
        legacy.manifestar(out)


if __name__ == "__main__":
    try:
        sys.exit(main())
    except Exception as error:
        print("ERROR_G3_V2:", str(error), file=sys.stderr)
        sys.exit(1)
