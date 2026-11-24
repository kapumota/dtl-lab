#!/usr/bin/env python3
"""Campaña G3 pre-registrada, sin alterar la rama ni el corpus V1."""

import argparse
import csv
import hashlib
import json
import os
from pathlib import Path
import platform
import shutil
import subprocess
import sys
import tempfile
import time

HERE = Path(__file__).resolve().parent
CONFIG = HERE / "g3_mutants.json"
JAVA_PROBE = HERE / "G3PropertyProbe.java"
JAVA_PROBE_DEST = Path("src/test/java/dltlab/research/paper1/g3/G3PropertyProbe.java")
BASELINE = "e4422ae483ab0eb4c0af48bbb58be1c49c8cf816"
BRANCH = "research/paper1-editor-o1-o3"

TESTS = [
    ("HIST_PROTOCOL", "historico", "dltlab.sharding.protocol.AtomicCommitProtocolTest", None),
    ("HIST_ROLLBACK", "historico", "dltlab.sharding.protocol.AtomicCommitRollbackTest", None),
    ("HIST_ISOLATION", "historico", "dltlab.sharding.protocol.P1CIsolationTest", None),
    ("G1_AUDIT", "G1_con_oraculo_valor", "dltlab.sharding.protocol.CommitAuditIntegrationTest", None),
    ("G2_BOUNDARY", "G2_frontera", "dltlab.conformance.v2.V2BoundaryProjectionTest", None),
    ("G2_INTERMEDIATE", "G2_dependiente_journal_G1", "dltlab.conformance.v2.V2IntermediateAuditTest", None),
    ("G2_DECISION", "G2_dependiente_journal_G1", "dltlab.conformance.v2.V2DecisionWitnessTest", "witness"),
    ("G3_RECIPIENT", "oraculo_g3_adicional", "dltlab.research.paper1.g3.G3PropertyProbe", "beneficiario"),
    ("G3_ROLLBACK", "oraculo_g3_adicional", "dltlab.research.paper1.g3.G3PropertyProbe", "rollback"),
    ("G3_CONTENTION", "oraculo_g3_adicional", "dltlab.research.paper1.g3.G3PropertyProbe", "contencion"),
]


def shell(arguments, cwd, timeout=180):
    return subprocess.run(arguments, cwd=cwd, text=True, capture_output=True,
                          timeout=timeout, check=False)


def checked(arguments, cwd, timeout=180):
    result = shell(arguments, cwd, timeout)
    if result.returncode != 0:
        raise RuntimeError(f"Comando fallido {arguments}: {result.stderr.strip()}")
    return result.stdout.strip()


def checksum(path):
    hasher = hashlib.sha256()
    with open(path, "rb") as stream:
        for fragment in iter(lambda: stream.read(1024 * 1024), b""):
            hasher.update(fragment)
    return hasher.hexdigest()


def guardar(path, contenido):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(contenido, encoding="utf-8")


def instalar_sonda(worktree):
    dest = worktree / JAVA_PROBE_DEST
    dest.parent.mkdir(parents=True, exist_ok=True)
    if dest.exists():
        raise RuntimeError("La sonda G3 ya existe en el commit congelado")
    shutil.copy2(JAVA_PROBE, dest)


def mutar(worktree, spec):
    dest = worktree / spec["file"]
    original = dest.read_text(encoding="utf-8")
    coincidencias = original.count(spec["old"])
    if coincidencias != 1:
        raise RuntimeError(f"{spec['id']}: se requieren 1 ancla exacta, encontradas {coincidencias}")
    actualizado = original.replace(spec["old"], spec["new"], 1)
    if actualizado == original:
        raise RuntimeError("No se modificó código Java")
    dest.write_text(actualizado, encoding="utf-8")
    return {"ruta": spec["file"], "sha256_original": hashlib.sha256(original.encode()).hexdigest(),
            "sha256_mutado": checksum(dest)}


def compilar(worktree, target):
    principal = sorted(str(p.relative_to(worktree)) for p in (worktree / "src/main/java").rglob("*.java"))
    pruebas = sorted(str(p.relative_to(worktree)) for p in (worktree / "src/test/java").rglob("*.java"))
    (worktree / "build/classes").mkdir(parents=True, exist_ok=True)
    (worktree / "build/test-classes").mkdir(parents=True, exist_ok=True)
    # Los argfiles evitan exceder el tamaño permitido de la línea de comandos.
    arg_main = worktree / "g3-javac-main.args"
    arg_test = worktree / "g3-javac-test.args"
    guardar(arg_main, "\n".join(principal) + "\n")
    guardar(arg_test, "\n".join(pruebas) + "\n")
    result_main = shell(["javac", "-encoding", "UTF-8", "-d", "build/classes",
                         "@g3-javac-main.args"], worktree, 180)
    guardar(target / "compilacion_main.log", result_main.stdout + "\n" + result_main.stderr)
    if result_main.returncode != 0:
        return False
    result_test = shell(["javac", "-encoding", "UTF-8", "-cp", "build/classes",
                         "-d", "build/test-classes", "@g3-javac-test.args"], worktree, 180)
    guardar(target / "compilacion_test.log", result_test.stdout + "\n" + result_test.stderr)
    return result_test.returncode == 0


def ejecutar_prueba(worktree, target, test, timeout):
    nombre, grupo, clase, argumento = test
    cmd = ["java", "-cp", "build/classes:build/test-classes", clase]
    if argumento == "witness":
        directorio = target / "testigos"
        directorio.mkdir(parents=True, exist_ok=True)
        cmd.append(str(directorio))
    elif argumento:
        cmd.append(argumento)
    comienzo = time.monotonic()
    try:
        resultado = shell(cmd, worktree, timeout)
        estado = "PASS" if resultado.returncode == 0 else "FAIL"
        out, err = resultado.stdout, resultado.stderr
        retorno = resultado.returncode
    except subprocess.TimeoutExpired as fallo:
        estado, retorno = "TIMEOUT", None
        out = fallo.stdout.decode(errors="replace") if isinstance(fallo.stdout, bytes) else (fallo.stdout or "")
        err = fallo.stderr.decode(errors="replace") if isinstance(fallo.stderr, bytes) else (fallo.stderr or "")
    guardar(target / f"{nombre}.stdout.log", out)
    guardar(target / f"{nombre}.stderr.log", err)
    veredictos = [line.strip() for line in out.splitlines() if line.startswith("G3_G2_VEREDICTO:")]
    return {"test_id": nombre, "grupo": grupo, "estado": estado,
            "codigo_retorno": retorno, "segundos": round(time.monotonic() - comienzo, 3),
            "marcas_alcance": [line.strip() for line in err.splitlines() if "G3_ALCANZADO:" in line],
            "veredictos_g2": veredictos}


def probar(repo, source_sha, spec, salida, timeout):
    etiqueta = "CONTROL" if spec is None else spec["id"]
    carpeta = salida / etiqueta
    carpeta.mkdir(parents=True, exist_ok=False)
    with tempfile.TemporaryDirectory(prefix="dtl-g3-worktrees-") as temporal:
        worktree = Path(temporal) / "codigo"
        checked(["git", "worktree", "add", "--detach", str(worktree), source_sha], repo)
        try:
            instalar_sonda(worktree)
            meta = {"id": etiqueta, "source_sha": source_sha, "sonda_sha256": checksum(JAVA_PROBE)}
            if spec is not None:
                meta["mutacion"] = mutar(worktree, spec)
                diff = checked(["git", "diff", "--", spec["file"]], worktree)
                guardar(carpeta / "mutacion_java.diff", diff + "\n")
            guardar(carpeta / "procedencia.json", json.dumps(meta, ensure_ascii=False, indent=2) + "\n")
            compilado = compilar(worktree, carpeta)
            if not compilado:
                return {"id": etiqueta, "compila": False, "pruebas": []}
            pruebas = [ejecutar_prueba(worktree, carpeta, test, timeout) for test in TESTS]
            return {"id": etiqueta, "compila": True, "pruebas": pruebas}
        finally:
            # Este worktree es temporal, exclusivo de G3. No contiene cambios del usuario.
            checked(["git", "worktree", "remove", "--force", str(worktree)], repo)


def resumir(resultados, specs, salida):
    control = resultados[0]
    if not control["compila"] or any(p["estado"] != "PASS" for p in control["pruebas"]):
        raise RuntimeError("El control sin mutación no superó todos los detectores. No se analizan mutantes")
    for resultado, spec in zip(resultados[1:], specs):
        pruebas = resultado["pruebas"]
        alcanzado = any(f"G3_ALCANZADO:{spec['id']}" in marca
                        for p in pruebas for marca in p["marcas_alcance"])
        detectores = [p["test_id"] for p in pruebas if p["estado"] != "PASS"]
        resultado["alcanzado"] = alcanzado
        resultado["detectores_que_fallaron"] = detectores
        if not resultado["compila"]:
            resultado["clasificacion"] = "NO_COMPILA"
        elif not alcanzado:
            resultado["clasificacion"] = "NO_ALCANZADO"
        elif detectores:
            resultado["clasificacion"] = "FALLO_OBSERVADO_REQUIERE_ATRIBUCION"
        else:
            resultado["clasificacion"] = "SUPERVIVIENTE_CON_ALCANCE"
    guardar(salida / "resultado_integral.json", json.dumps(resultados, ensure_ascii=False, indent=2) + "\n")
    with (salida / "matriz_detector_mutante.csv").open("w", newline="", encoding="utf-8") as stream:
        writer = csv.writer(stream)
        writer.writerow(["mutante", "categoria", "compila", "alcanzado", "detector", "grupo",
                         "resultado", "segundos", "veredicto_g2", "clasificacion_mutante"])
        for resultado in resultados:
            for prueba in resultado["pruebas"]:
                writer.writerow([resultado["id"], "control" if resultado["id"] == "CONTROL" else
                                 next(s["category"] for s in specs if s["id"] == resultado["id"]),
                                 resultado["compila"], resultado.get("alcanzado", "N/A"),
                                 prueba["test_id"], prueba["grupo"], prueba["estado"],
                                 prueba["segundos"], ";".join(prueba["veredictos_g2"]),
                                 resultado.get("clasificacion", "CONTROL_SANO")])
    lineas = ["G3, campaña exploratoria congelada", "Resultados fuera de la evidencia V1.",
              "El fallo de una prueba no atribuye por sí solo detección específica al algoritmo."]
    for r in resultados:
        if r["id"] != "CONTROL":
            lineas.append(f"{r['id']}: {r['clasificacion']} | {','.join(r['detectores_que_fallaron'])}")
    guardar(salida / "ESTADO_PRELIMINAR.txt", "\n".join(lineas) + "\n")


def manifestar(salida):
    lineas = []
    for archivo in sorted(salida.rglob("*")):
        if archivo.is_file() and archivo.name != "SHA256SUMS.txt":
            lineas.append(f"{checksum(archivo)}  {archivo.relative_to(salida)}")
    guardar(salida / "SHA256SUMS.txt", "\n".join(lineas) + "\n")


def main():
    parser = argparse.ArgumentParser(description="Ejecutar G3 en worktrees limpios y separados")
    parser.add_argument("--repo", required=True, type=Path)
    parser.add_argument("--out", required=True, type=Path)
    parser.add_argument("--timeout-prueba", type=int, default=70)
    parser.add_argument("--solo", help="ID de mutante para prueba piloto, no sustituye la campaña")
    args = parser.parse_args()
    repo, salida = args.repo.expanduser().resolve(), args.out.expanduser().resolve()
    conf = json.loads(CONFIG.read_text(encoding="utf-8"))
    if conf["source_commit"] != BASELINE or conf["branch_local"] != BRANCH:
        raise RuntimeError("El pre-registro fue alterado; detener y crear versión nueva")
    if not JAVA_PROBE.exists() or not (repo / ".git").exists() and not (repo / ".git").is_file():
        raise RuntimeError("No hay repositorio Git o falta la sonda")
    if salida == repo or repo in salida.parents:
        raise RuntimeError("Las evidencias deben quedar fuera del repositorio")
    if salida.exists():
        raise RuntimeError("El directorio de evidencia ya existe; no se sobrescriben campañas")
    if checked(["git", "branch", "--show-current"], repo) != BRANCH:
        raise RuntimeError(f"Cambie a la rama local obligatoria: {BRANCH}")
    checked(["git", "show-ref", "--verify", "refs/heads/" + BRANCH], repo)
    checked(["git", "cat-file", "-e", BASELINE + "^{commit}"], repo)
    if shell(["git", "merge-base", "--is-ancestor", BASELINE, "HEAD"], repo).returncode != 0:
        raise RuntimeError("La rama de trabajo no desciende del baseline G3")
    if args.timeout_prueba <= 0:
        raise RuntimeError("Timeout inválido")
    specs = conf["mutation_rules"]
    if len({s["id"] for s in specs}) != len(specs):
        raise RuntimeError("Identificadores de mutantes duplicados")
    if args.solo:
        specs = [s for s in specs if s["id"] == args.solo]
        if not specs:
            raise RuntimeError("Mutante fuera del pre-registro")
    salida.mkdir(parents=True)
    ambiente = {
        "fecha_inicio": time.strftime("%Y-%m-%dT%H:%M:%S%z"),
        "repositorio": str(repo), "rama_local": BRANCH,
        "HEAD_local": checked(["git", "rev-parse", "HEAD"], repo),
        "cambios_locales_no_incluidos": checked(["git", "status", "--porcelain", "--untracked-files=normal"], repo),
        "commit_fuente_worktree": BASELINE,
        "config_sha256": checksum(CONFIG), "sonda_sha256": checksum(JAVA_PROBE),
        "plataforma": platform.platform(), "python": sys.version,
        "java": shell(["java", "-version"], repo).stderr.strip(),
        "javac": shell(["javac", "-version"], repo).stdout.strip(),
        "modo": "piloto" if args.solo else "campana_pre_registrada"
    }
    guardar(salida / "AMBIENTE.json", json.dumps(ambiente, ensure_ascii=False, indent=2) + "\n")
    guardar(salida / "G3_PRE_REGISTRO_COPIA.json", CONFIG.read_text(encoding="utf-8"))
    resultados = []
    try:
        print("G3 control sin mutación")
        resultados.append(probar(repo, BASELINE, None, salida, args.timeout_prueba))
        if not resultados[0]["compila"] or any(p["estado"] != "PASS" for p in resultados[0]["pruebas"]):
            guardar(salida / "ESTADO_PRELIMINAR.txt", "NO_GO: control sin mutación falló.\n")
            return 2
        for spec in specs:
            print("G3 mutante:", spec["id"], flush=True)
            resultados.append(probar(repo, BASELINE, spec, salida, args.timeout_prueba))
        resumir(resultados, specs, salida)
        return 0
    finally:
        if resultados:
            guardar(salida / "RESUMEN_BRUTO.json", json.dumps(resultados, ensure_ascii=False, indent=2) + "\n")
        manifestar(salida)


if __name__ == "__main__":
    try:
        sys.exit(main())
    except Exception as exc:
        print("ERROR_G3:", str(exc), file=sys.stderr)
        sys.exit(1)
