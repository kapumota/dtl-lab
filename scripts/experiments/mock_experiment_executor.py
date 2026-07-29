#!/usr/bin/env python3
"""Executor determinista usado solo por el gate de infraestructura."""

from __future__ import annotations

import argparse
import json
from pathlib import Path


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(
        description="Ejecuta una tarea simulada para validar la infraestructura."
    )
    parser.add_argument("--task", required=True)
    parser.add_argument("--output-dir", required=True)
    return parser.parse_args()


def main() -> None:
    args = parse_args()
    task_path = Path(args.task)
    output_dir = Path(args.output_dir)
    task = json.loads(task_path.read_text(encoding="utf-8"))

    payload = {
        "schema_version": 1,
        "status": "completed",
        "expectation_met": True,
        "task_id": task["task_id"],
        "message": "La ejecución simulada terminó correctamente.",
        "metrics": {
            "states_generated": 1,
            "distinct_states": 1,
            "depth": 1,
        },
    }
    destination = output_dir / "executor-result.json"
    destination.write_text(
        json.dumps(payload, ensure_ascii=False, indent=2) + "\n",
        encoding="utf-8",
    )
    print("La ejecución simulada terminó correctamente.")


if __name__ == "__main__":
    main()
