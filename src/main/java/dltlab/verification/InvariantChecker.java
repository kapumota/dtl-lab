package dltlab.verification;

import java.util.ArrayList;
import java.util.List;

/** Ejecuta una coleccion de invariantes sobre el estado del ledger. */
public class InvariantChecker {
    public VerificationReport check(LedgerState state, List<Invariant> invariants) {
        List<VerificationResult> results = new ArrayList<>();
        for (Invariant invariant : invariants) {
            results.add(invariant.check(state));
        }
        return new VerificationReport(results);
    }
}
