package dltlab.mempool;

import java.util.Collection;
import java.util.List;

/** Politica de descarte cuando la mempool supera su capacidad en vBytes. */
public interface EvictionPolicy {
    List<MempoolEntry> chooseEvictions(Collection<MempoolEntry> entries, long targetVBytes);
}
