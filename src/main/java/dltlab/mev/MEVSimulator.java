package dltlab.mev;

import dltlab.transaction.FeeCalculator;
import dltlab.transaction.Transaction;
import dltlab.transaction.TxValidator;
import dltlab.transaction.UTXOPool;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Ejecuta escenarios MEV abstractos y calcula ingresos comparables para mineros. */
public class MEVSimulator {
    public MEVScenarioResult evaluate(MEVScenario scenario) {
        List<String> honestOrder = scenario.honestOrderLabels();
        List<String> mevOrder = buildMevOrder(scenario);
        long honestFees = validFeeTotal(honestOrder, scenario.transactionsByLabel(), scenario.initialPool());
        long mevFees = validFeeTotal(mevOrder, scenario.transactionsByLabel(), scenario.initialPool());
        long extractedValue = scenario.opportunities().stream().mapToLong(MEVOpportunity::extractableValue).sum();
        MEVType primaryType = scenario.opportunities().isEmpty()
                ? MEVType.FRONT_RUNNING
                : scenario.opportunities().get(0).type();
        String explanation = scenario.opportunities().isEmpty()
                ? "No hay oportunidad MEV registrada."
                : scenario.opportunities().get(0).description();

        return new MEVScenarioResult(
                scenario.name(),
                primaryType,
                honestOrder,
                mevOrder,
                honestFees,
                mevFees,
                extractedValue,
                honestFees,
                mevFees + extractedValue,
                explanation
        );
    }

    private List<String> buildMevOrder(MEVScenario scenario) {
        Set<String> ordered = new LinkedHashSet<>();
        for (String label : scenario.honestOrderLabels()) {
            List<MEVOpportunity> matching = opportunitiesForVictim(scenario.opportunities(), label);
            for (MEVOpportunity opportunity : matching) {
                if ((opportunity.type() == MEVType.FRONT_RUNNING || opportunity.type() == MEVType.SANDWICH)
                        && opportunity.beforeLabel() != null) {
                    ordered.add(opportunity.beforeLabel());
                }
            }

            ordered.add(label);

            for (MEVOpportunity opportunity : matching) {
                if ((opportunity.type() == MEVType.BACK_RUNNING || opportunity.type() == MEVType.SANDWICH)
                        && opportunity.afterLabel() != null) {
                    ordered.add(opportunity.afterLabel());
                }
            }
        }

        // Incluye transacciones que no estaban en el orden honesto principal, por si el escenario las agrega.
        ordered.addAll(scenario.transactionsByLabel().keySet());
        return new ArrayList<>(ordered);
    }

    private List<MEVOpportunity> opportunitiesForVictim(List<MEVOpportunity> opportunities, String victimLabel) {
        List<MEVOpportunity> result = new ArrayList<>();
        for (MEVOpportunity opportunity : opportunities) {
            if (opportunity.victimLabel().equals(victimLabel)) {
                result.add(opportunity);
            }
        }
        return result;
    }

    private long validFeeTotal(List<String> order, Map<String, Transaction> txByLabel, UTXOPool initialPool) {
        TxValidator validator = new TxValidator(initialPool);
        long total = 0L;
        for (String label : order) {
            Transaction tx = txByLabel.get(label);
            if (tx == null) {
                continue;
            }
            long fee = FeeCalculator.fee(tx, validator.getUtxoPool());
            if (fee != Long.MIN_VALUE && validator.isValidTx(tx)) {
                total += fee;
                validator.applyTransaction(tx);
            }
        }
        return total;
    }
}
