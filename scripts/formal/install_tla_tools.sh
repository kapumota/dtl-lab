#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
# shellcheck source=tool_versions.env
source "$ROOT_DIR/scripts/formal/tool_versions.env"

TOOLS_ROOT="${FORMAL_TOOLS_ROOT:-$ROOT_DIR/$FORMAL_TOOLS_DIR}"
INSTALL_DIR="$TOOLS_ROOT/tla/$TLA_TOOLS_VERSION"
JAR_PATH="$INSTALL_DIR/tla2tools.jar"
TEMP_PATH="$JAR_PATH.tmp"

calculateSha1() {
  local path="$1"
  if command -v sha1sum >/dev/null 2>&1; then
    sha1sum "$path" | awk '{print $1}'
  elif command -v shasum >/dev/null 2>&1; then
    shasum -a 1 "$path" | awk '{print $1}'
  else
    echo "No se encontro una herramienta para calcular SHA-1." >&2
    exit 1
  fi
}

verifyJar() {
  local path="$1"
  [[ -f "$path" ]] || return 1
  [[ "$(calculateSha1 "$path")" == "$TLA_TOOLS_SHA1" ]]
}

if ! command -v curl >/dev/null 2>&1; then
  echo "curl es obligatorio para instalar TLC." >&2
  exit 1
fi

mkdir -p "$INSTALL_DIR"

if verifyJar "$JAR_PATH"; then
  echo "TLC $TLA_TOOLS_VERSION ya esta instalado y verificado."
else
  rm -f "$JAR_PATH" "$TEMP_PATH"
  echo "Descargando TLC $TLA_TOOLS_VERSION."
  curl --fail --location --silent --show-error --retry 3 \
    --output "$TEMP_PATH" "$TLA_TOOLS_URL"
  actual_sha1="$(calculateSha1 "$TEMP_PATH")"
  if [[ "$actual_sha1" != "$TLA_TOOLS_SHA1" ]]; then
    echo "El checksum de TLC no coincide con la version fijada." >&2
    rm -f "$TEMP_PATH"
    exit 1
  fi
  mv "$TEMP_PATH" "$JAR_PATH"
fi

probe_output="$(java -cp "$JAR_PATH" tlc2.TLC -version 2>&1 || true)"
version_output="$(grep -m1 'TLC2 Version' <<<"$probe_output" || true)"
if [[ -z "$version_output" ]]; then
  echo "No se pudo identificar la version ejecutable de TLC." >&2
  exit 1
fi

printf '%s\n' "$JAR_PATH"
