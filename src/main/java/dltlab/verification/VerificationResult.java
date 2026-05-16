package dltlab.verification;

/** Resultado de un invariante. */
public record VerificationResult(String name, boolean passed, String detail) {}
