package dltlab.mev;

import dltlab.defi.AmmPool;
import dltlab.defi.SwapOrder;

/** Escenario MEV economico basado en un AMM realista. */
public record DeFiMEVScenario(
        String name,
        AmmPool initialPool,
        SwapOrder victimOrder,
        SwapOrder attackerFrontRunOrder,
        double builderPaymentRatio
) {
    public DeFiMEVScenario {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("El nombre del escenario es obligatorio.");
        if (initialPool == null) throw new IllegalArgumentException("El pool inicial es obligatorio.");
        if (victimOrder == null) throw new IllegalArgumentException("La orden victima es obligatoria.");
        if (attackerFrontRunOrder == null) throw new IllegalArgumentException("La orden frontal del atacante es obligatoria.");
        if (builderPaymentRatio < 0.0 || builderPaymentRatio > 1.0) {
            throw new IllegalArgumentException("El ratio de pago al productor debe estar entre 0 y 1.");
        }
        initialPool = initialPool.copy();
    }
}
