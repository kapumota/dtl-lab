package dltlab.trace;

import java.io.IOException;
import java.nio.file.Path;

/** Contrato para serializar trazas concretas sin dependencias externas. */
public interface TraceExporter {
    byte[] export(TraceExecution execution);

    void export(TraceExecution execution, Path output) throws IOException;

    String contentHash(TraceExecution execution);

    String fileHash(TraceExecution execution);
}
