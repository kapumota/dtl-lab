package dltlab.metrics;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/** Utilidad para escribir archivos de reporte de la demo. */
public final class ReportFiles {
    private ReportFiles() {}

    public static Path write(Path file, String content) {
        try {
            Files.createDirectories(file.getParent());
            Files.writeString(file, content, StandardCharsets.UTF_8);
            return file;
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo escribir el reporte: " + file, e);
        }
    }
}
