package dltlab.verification;

/** Propiedad de seguridad que debe cumplirse en el ledger. */
public interface Invariant {
    VerificationResult check(LedgerState state);
}
