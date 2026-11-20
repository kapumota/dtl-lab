### Modelo TLA+ V2.2, copia de fuentes para Paper 1

Estas fuentes se copiaron byte por byte desde `DTL_Lab_V2_2_Modelo_Apendice_Reproduccion.zip`, directorio `DTL_Lab_V2_Artifact_v2.2/v2/`, sin modificar la revisión original de V1. La copia contiene exclusivamente los archivos `.tla`, `.cfg` y `matrix.csv` del directorio superior del modelo; **no** contiene los logs TLC originales ni reproduce automáticamente la campaña completa de 52 casos.

SHA-256 de `CrossShardCommitV2.tla`: `22aa41d3767d0ff42b9dfeae9b0f103a5baaa4bc66fe7934cbf440c04e9401e7`.

Comprobar integridad:

```bash
cd specs/tla/research/paper1_v2_2
sha256sum -c SHA256SUMS.txt
```

Para reproducir la matriz completa debe utilizarse el paquete integral V2.2 y el ejecutor G0-B, incluido su protocolo de clasificación de contraejemplos esperados; no basta con invocar TLC sobre cada archivo `.cfg` y declarar éxito por código de salida. No cambiar el modelo para hacer coincidir los testigos Java.
