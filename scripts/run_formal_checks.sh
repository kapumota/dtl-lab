#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

bash "$ROOT_DIR/scripts/formal/check_structure.sh"

echo "Perfil educativo completado con validacion estructural."
echo "Para ejecutar TLC y Alloy obligatoriamente usa: make formal-research"
