package dltlab.mempool;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

/** Descarta primero las transacciones con menor fee rate. */
public class LowestFeeRateEvictionPolicy implements EvictionPolicy {
    @Override
    public List<MempoolEntry> chooseEvictions(Collection<MempoolEntry> entries, long targetVBytes) {
        long currentVBytes = 0L;
        for (MempoolEntry entry : entries) {
            currentVBytes += entry.virtualSize();
        }

        List<MempoolEntry> ordered = new ArrayList<>(entries);
        ordered.sort(Comparator
                .comparing(MempoolEntry::feeRate)
                .thenComparingLong(MempoolEntry::arrivalOrder));

        List<MempoolEntry> evicted = new ArrayList<>();
        for (MempoolEntry entry : ordered) {
            if (currentVBytes <= targetVBytes) {
                break;
            }
            evicted.add(entry);
            currentVBytes -= entry.virtualSize();
        }
        return evicted;
    }
}
