package dltlab.mev;

import java.util.List;

/** Resultado comparable de un escenario MEV. */
public record MEVScenarioResult(
        String scenarioName,
        MEVType primaryType,
        List<String> honestOrderLabels,
        List<String> mevOrderLabels,
        long honestFees,
        long mevFees,
        long extractedValue,
        long honestMinerRevenue,
        long mevMinerRevenue,
        String explanation
) {
    public long revenueDelta() {
        return mevMinerRevenue - honestMinerRevenue;
    }

    public String render() {
        StringBuilder sb = new StringBuilder();
        sb.append("Escenario MEV: ").append(scenarioName).append('\n');
        sb.append("  Tipo: ").append(primaryType.spanishName()).append('\n');
        sb.append("  Orden honesto: ").append(honestOrderLabels).append('\n');
        sb.append("  Orden MEV: ").append(mevOrderLabels).append('\n');
        sb.append("  Fees con orden honesto: ").append(honestFees).append('\n');
        sb.append("  Fees con orden MEV: ").append(mevFees).append('\n');
        sb.append("  Valor MEV extraido: ").append(extractedValue).append('\n');
        sb.append("  Ingreso minero honesto: ").append(honestMinerRevenue).append('\n');
        sb.append("  Ingreso minero MEV-aware: ").append(mevMinerRevenue).append('\n');
        sb.append("  Diferencia de ingreso: ").append(revenueDelta()).append('\n');
        sb.append("  Explicacion: ").append(explanation).append('\n');
        return sb.toString();
    }
}
