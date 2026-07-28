#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
# shellcheck source=tool_versions.env
source "$ROOT_DIR/scripts/formal/tool_versions.env"

TOOLS_ROOT="${FORMAL_TOOLS_ROOT:-$ROOT_DIR/$FORMAL_TOOLS_DIR}"
INSTALL_DIR="$TOOLS_ROOT/alloy/$ALLOY_VERSION"
JAR_PATH="$INSTALL_DIR/org.alloytools.alloy.dist-$ALLOY_VERSION.jar"
JAR_TEMP_PATH="$JAR_PATH.tmp"
CHECKSUM_PATH="$INSTALL_DIR/alloy.sha1"
CHECKSUM_TEMP_PATH="$CHECKSUM_PATH.tmp"

calculateSha1() {
  local path="$1"
  if command -v sha1sum >/dev/null 2>&1; then
    sha1sum "$path" | awk '{print tolower($1)}'
  elif command -v shasum >/dev/null 2>&1; then
    shasum -a 1 "$path" | awk '{print tolower($1)}'
  else
    echo "No se encontro una herramienta para calcular SHA-1." >&2
    exit 1
  fi
}

readExpectedSha1() {
  local path="$1"
  tr -d '[:space:]' < "$path" | tr '[:upper:]' '[:lower:]'
}

verifyJar() {
  local jar_path="$1"
  local checksum_path="$2"
  [[ -f "$jar_path" && -s "$checksum_path" ]] || return 1
  grep -Eq '^[0-9a-fA-F]{40}[[:space:]]*$' "$checksum_path" || return 1
  [[ "$(calculateSha1 "$jar_path")" == "$(readExpectedSha1 "$checksum_path")" ]] || return 1
  jar tf "$jar_path" >/dev/null 2>&1
}

downloadVerifiedArtifact() {
  rm -f "$JAR_TEMP_PATH" "$CHECKSUM_TEMP_PATH"

  curl --fail --location --silent --show-error --retry 3 \
    --output "$CHECKSUM_TEMP_PATH" "$ALLOY_SHA1_URL"
  curl --fail --location --silent --show-error --retry 3 \
    --output "$JAR_TEMP_PATH" "$ALLOY_URL"

  if ! grep -Eq '^[0-9a-fA-F]{40}[[:space:]]*$' "$CHECKSUM_TEMP_PATH"; then
    echo "No se pudo obtener el checksum publicado de Alloy." >&2
    rm -f "$JAR_TEMP_PATH" "$CHECKSUM_TEMP_PATH"
    exit 1
  fi

  local actual_sha1
  local expected_sha1
  actual_sha1="$(calculateSha1 "$JAR_TEMP_PATH")"
  expected_sha1="$(readExpectedSha1 "$CHECKSUM_TEMP_PATH")"

  if [[ "$actual_sha1" != "$expected_sha1" ]]; then
    echo "El checksum de Alloy no coincide con el artefacto publicado." >&2
    echo "Checksum esperado: $expected_sha1" >&2
    echo "Checksum obtenido: $actual_sha1" >&2
    echo "Bytes descargados: $(wc -c < "$JAR_TEMP_PATH")" >&2
    rm -f "$JAR_TEMP_PATH" "$CHECKSUM_TEMP_PATH"
    exit 1
  fi

  if ! jar tf "$JAR_TEMP_PATH" >/dev/null 2>&1; then
    echo "El archivo descargado no es una distribucion JAR valida de Alloy." >&2
    rm -f "$JAR_TEMP_PATH" "$CHECKSUM_TEMP_PATH"
    exit 1
  fi

  mv "$JAR_TEMP_PATH" "$JAR_PATH"
  mv "$CHECKSUM_TEMP_PATH" "$CHECKSUM_PATH"
}

if ! command -v curl >/dev/null 2>&1; then
  echo "curl es obligatorio para instalar Alloy." >&2
  exit 1
fi

if ! command -v jar >/dev/null 2>&1; then
  echo "La herramienta jar del JDK es obligatoria para instalar Alloy." >&2
  exit 1
fi

mkdir -p "$INSTALL_DIR"

if verifyJar "$JAR_PATH" "$CHECKSUM_PATH"; then
  echo "Alloy $ALLOY_VERSION ya esta instalado y verificado."
else
  rm -f "$JAR_PATH" "$CHECKSUM_PATH"
  echo "Descargando Alloy $ALLOY_VERSION desde Maven Central."
  downloadVerifiedArtifact
fi

version_output="$(java -jar "$JAR_PATH" version --full 2>&1 || true)"
if [[ -z "$version_output" ]] || ! grep -Eq "^${ALLOY_VERSION}([.-]|$)" <<<"$version_output"; then
  echo "No se pudo identificar la version ejecutable de Alloy." >&2
  echo "$version_output" >&2
  exit 1
fi

printf '%s\n' "$JAR_PATH"
