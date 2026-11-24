#!/usr/bin/env bash
set -euo pipefail

# Ejecución de solo lectura en la máquina que conserva el archivo original.
# Uso: bash localizar_o3.sh /home/project
raiz="${1:-$HOME}"
archivo="paper1-q3-v1-reproduction.tar.gz"

if [[ ! -d "$raiz" ]]; then
  printf 'ERROR: no existe el directorio de búsqueda %s\n' "$raiz" >&2
  exit 2
fi
printf 'Buscando el bundle histórico en %s\n' "$raiz"
mapfile -d '' hallazgos < <(find "$raiz" -type f -name "$archivo" -print0 2>/dev/null)
if [[ "${#hallazgos[@]}" -eq 0 ]]; then
  printf 'SIN_HALLAZGOS: no se localizó el archivo histórico.\n'
  exit 3
fi
for ruta in "${hallazgos[@]}"; do
  printf '\nRuta original: %s\n' "$ruta"
  stat -c 'Tamaño: %s bytes; fecha modificación: %y' "$ruta"
  sha256sum "$ruta"
  printf 'Integridad gzip y estructura TAR:\n'
  gzip -t "$ruta"
  tar -tzf "$ruta" | sed -n '1,25p'
  printf 'FIN_INSPECCION: no se extrajo ni modificó el archivo original.\n'
done
