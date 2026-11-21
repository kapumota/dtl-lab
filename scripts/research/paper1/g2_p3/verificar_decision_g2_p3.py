#!/usr/bin/env python3
"""G2-P3: testigos Java y observador formal no intrusivo para TLC V2.2.

Este modulo NO modifica el modelo congelado, ni demuestra refinamiento
arbitrario. Clasifica la incompatibilidad de correspondencia interna.
"""
from __future__ import annotations

import argparse
import csv
import hashlib
import json
from pathlib import Path
import shutil
import subprocess

MODEL_SHA256 = "22aa41d3767d0ff42b9dfeae9b0f103a5baaa4bc66fe7934cbf440c04e9401e7"
TLC_SHA1 = "bee4a54f3ee3d4afc347c3240ec2d9e93b075104"
HOOKS = ["AFTER_RECEIPT_CONSUMED", "AFTER_SOURCE_DEBIT",
         "DURING_TARGET_CREDIT", "AFTER_TARGET_CREDIT"]
FIELDS = ["scenario", "result", "failure_point", "partial_debit",
          "decision_witness", "saw_debit", "saw_credit",
          "rollback_exact", "stages"]
MODULE = r'''---- MODULE G2P3Observer ----
EXTENDS MC

\* Observador de historia: no cambia acciones, guardas ni estados de V2.2.
VARIABLE seenDebited
ObservedVars == << vars, seenDebited >>
G2Init ==
    /\ Init
    /\ seenDebited = [t \in Transfers |-> FALSE]
G2Next ==
    /\ Next
    /\ seenDebited' = [t \in Transfers |->
          seenDebited[t] \/ srcState'[t] = "Debited"]
G2Spec == G2Init /\ [][G2Next]_ObservedVars

ObservedTypeOK == TypeOK /\ seenDebited \in [Transfers -> BOOLEAN]
NoReturnAfterDebit ==
    \A t \in Transfers : seenDebited[t] => srcState[t] = "Debited"
NoRevokeAfterDebit ==
    \A t \in Transfers : seenDebited[t] => decision[t] = "Commit"
\* Control de no-vacuidad: se espera violacion de esta invariante.
ReachDebited == ~(\E t \in Transfers : seenDebited[t])
====
'''


def digest(path: Path, algorithm: str = "sha256") -> str:
    h = hashlib.new(algorithm)
    with path.open("rb") as fd:
        for block in iter(lambda: fd.read(1024 * 1024), b""):
            h.update(block)
    return h.hexdigest()


def check_witnesses(path: Path) -> dict:
    with path.open("r", encoding="utf-8", newline="") as file:
        reader = csv.DictReader(file, delimiter="\t")
        if reader.fieldnames != FIELDS:
            raise ValueError(f"Columnas incorrectas: {reader.fieldnames}")
        rows = list(reader)
    expected = ["normal"] + ["rollback_" + x for x in HOOKS]
    if [x["scenario"] for x in rows] != expected:
        raise ValueError("Faltan o sobran testigos, o estan fuera de orden")
    for row in rows:
        name = row["scenario"]
        stages = row["stages"].split(",")
        if len(stages) != len(set(stages)) and name == "normal":
            # FAILURE_POINT_REACHED aparece varias veces normalmente.
            stages_no_hooks = [x for x in stages if x != "FAILURE_POINT_REACHED"]
            if len(stages_no_hooks) != len(set(stages_no_hooks)):
                raise ValueError("Etapas funcionales duplicadas")
        if row["decision_witness"] != "false":
            raise ValueError("Atribucion no justificada de decision irrevocable")
        if not stages or stages[0] != "BEGIN":
            raise ValueError("Falta comienzo de tentativa")
        if name == "normal":
            if not (row["result"] == "TRACE_COMMIT_VALIDADA"
                    and row["failure_point"] == "NONE"
                    and row["saw_debit"] == "true"
                    and row["saw_credit"] == "true"
                    and row["partial_debit"] == "true"
                    and row["rollback_exact"] == "false"
                    and stages[-1] == "COMMIT_COMPLETED"):
                raise ValueError("Testigo de commit no corresponde al journal G1")
        else:
            fault = name.removeprefix("rollback_")
            debit = fault != HOOKS[0]
            credit = fault == HOOKS[-1]
            if not (row["result"] == "TRACE_ROLLBACK_VALIDADA"
                    and row["failure_point"] == fault
                    and row["saw_debit"] == str(debit).lower()
                    and row["saw_credit"] == str(credit).lower()
                    and row["partial_debit"] == str(debit).lower()
                    and row["rollback_exact"] == "true"
                    and stages[-1] == "ROLLBACK_COMPLETED"
                    and "COMMIT_COMPLETED" not in stages):
                raise ValueError("Testigo de rollback incompatible: " + name)
    return {"count": len(rows), "commit": 1, "rollback": 4,
            "debit_then_rollback": 3, "witness_sha256": digest(path)}


def generate(workspace: Path, model_dir: Path) -> None:
    workspace.mkdir(parents=True, exist_ok=True)
    shutil.copyfile(model_dir / "CrossShardCommitV2.tla",
                    workspace / "CrossShardCommitV2.tla")
    shutil.copyfile(model_dir / "MC.tla", workspace / "MC.tla")
    (workspace / "G2P3Observer.tla").write_text(MODULE, encoding="utf-8")
    text = (model_dir / "correct_shared.cfg").read_text(encoding="utf-8")
    if "Mutation = \"Correct\"" not in text:
        raise ValueError("G2-P3 exige el modelo sin mutacion")
    prefix = text.split("SPECIFICATION Spec", maxsplit=1)[0]
    if "CONSTANTS" not in prefix or "Quorum =" not in prefix:
        raise ValueError("No se reconocen las constantes V2.2")
    safe = prefix + "SPECIFICATION G2Spec\n" + "\n".join([
        "INVARIANT ObservedTypeOK",
        "INVARIANT DecisionBeforeEffect",
        "INVARIANT NoReturnAfterDebit",
        "INVARIANT NoRevokeAfterDebit",
    ]) + "\n"
    reach = prefix + "SPECIFICATION G2Spec\n" + "\n".join([
        "INVARIANT ObservedTypeOK", "INVARIANT ReachDebited",
    ]) + "\n"
    (workspace / "G2P3Safety.cfg").write_text(safe, encoding="utf-8")
    (workspace / "G2P3Coverage.cfg").write_text(reach, encoding="utf-8")


def classify_tlc(log: str, status: int, case: str) -> bool:
    if case == "safety":
        return status == 0 and "Model checking completed. No error has been found." in log
    if case == "coverage":
        return status == 12 and "Invariant ReachDebited is violated" in log \
            and "Finished in" in log
    raise ValueError("Caso TLC desconocido")


def run_tlc(jar: Path, workspace: Path, out: Path, config: str, case: str) -> dict:
    command = ["java", "-XX:+UseParallelGC", "-cp", str(jar), "tlc2.TLC",
               "-workers", "1", "-deadlock", "-config", config, "G2P3Observer.tla"]
    try:
        proc = subprocess.run(command, cwd=workspace, text=True, capture_output=True,
                              timeout=240, check=False)
        logfile = proc.stdout + "\n" + proc.stderr
        code = proc.returncode
    except subprocess.TimeoutExpired as exc:
        logfile = f"TIMEOUT: {exc}"
        code = 124
    target = out / f"TLC_{case}.log"
    target.write_text(logfile, encoding="utf-8")
    good = classify_tlc(logfile, code, case)
    return {"case": case, "pass": good, "exit": code,
            "log_sha256": digest(target)}


def main() -> int:
    p = argparse.ArgumentParser()
    p.add_argument("--modelo", type=Path, required=True)
    p.add_argument("--testigos", type=Path, required=True)
    p.add_argument("--jar", type=Path, required=True)
    p.add_argument("--salida", type=Path, required=True)
    args = p.parse_args()
    args.salida.mkdir(parents=True, exist_ok=True)
    status_file = args.salida / "ESTADO.txt"
    status_file.write_text("INCOMPLETO G2-P3\n", encoding="utf-8")
    model = args.modelo / "CrossShardCommitV2.tla"
    if digest(model) != MODEL_SHA256:
        raise SystemExit("ERROR: SHA-256 del modelo V2.2 no coincide")
    if not args.jar.is_file() or digest(args.jar, "sha1") != TLC_SHA1:
        raise SystemExit("ERROR: TLC 1.7.4 no coincide con SHA-1 fijado")
    evidence = check_witnesses(args.testigos)
    workspace = args.salida / "modelo_temporal"
    generate(workspace, args.modelo)
    checks = [run_tlc(args.jar.absolute(), workspace, args.salida,
                      "G2P3Safety.cfg", "safety")]
    if checks[0]["pass"]:
        checks.append(run_tlc(args.jar.absolute(), workspace, args.salida,
                              "G2P3Coverage.cfg", "coverage"))
    good = len(checks) == 2 and all(x["pass"] for x in checks)
    result = {
        "status": "PASS_LIMITADO_G2_P3" if good else "FAIL_G2_P3",
        "model_sha256": MODEL_SHA256,
        "jar_sha1": TLC_SHA1,
        "java_witnesses": evidence,
        "tlc_checks": checks,
        "conclusion": (
            "Modelo V2.2 finito, sin mutacion: no vuelve al estado Locked despues de Debited; "
            "Java registra tres debitos parciales restaurados. No se acredita correspondencia "
            "interna uno-a-uno, solo un candidato de equivalencia en fronteras de operacion."
        ),
        "not_claimed": ["refinamiento completo", "linealizabilidad",
                        "ausencia de deadlocks", "recuperacion tras crash",
                        "atomicidad distribuida"],
    }
    (args.salida / "DICTAMEN_G2_P3.json").write_text(
        json.dumps(result, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    status_file.write_text(result["status"] + "\n", encoding="utf-8")
    print(result["status"])
    return 0 if good else 1


if __name__ == "__main__":
    raise SystemExit(main())
