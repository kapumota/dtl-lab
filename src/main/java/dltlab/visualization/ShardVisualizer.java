package dltlab.visualization;

import dltlab.sharding.CrossShardSession;
import dltlab.sharding.Shard;
import dltlab.sharding.ShardManager;

/** Genera visualizaciones ASCII y DOT del estado de los shards. */
public class ShardVisualizer {
    public String renderAscii(ShardManager manager) {
        StringBuilder sb = new StringBuilder();
        sb.append("Mapa de shards\n");
        sb.append("Ronda logica: ").append(manager.getCurrentRound()).append('\n');
        sb.append("Quorum por shard: ").append(manager.getQuorum()).append('\n');
        for (Shard shard : manager.getShards()) {
            sb.append("+-- Shard ").append(shard.getId()).append('\n');
            sb.append("|  UTXOs disponibles: ").append(shard.getUtxoPool().size()).append('\n');
            sb.append("|  Valor total local: ").append(shard.getUtxoPool().totalValue()).append('\n');
            sb.append("|  UTXOs bloqueados: ").append(shard.getLockedUtxos().size()).append('\n');
            sb.append("|  Recibos consumidos: ").append(shard.getConsumedReceipts().size()).append('\n');
            sb.append("`  Validadores online: ").append(shard.onlineValidators())
                    .append('/').append(shard.getValidators().size()).append('\n');
        }
        if (!manager.getSessions().isEmpty()) {
            sb.append("Transferencias cross-shard\n");
            for (CrossShardSession session : manager.getSessions()) {
                sb.append("+-- ").append(session.transfer().id(), 0, Math.min(10, session.transfer().id().length()))
                        .append(" ").append(session.transfer().sourceShardId())
                        .append(" -> ").append(session.transfer().targetShardId())
                        .append(" monto=").append(session.transfer().amount())
                        .append(" estado=").append(session.status())
                        .append(" timeout=").append(session.timeoutRound())
                        .append('\n');
                sb.append("`  ").append(session.reason()).append('\n');
            }
        }
        return sb.toString();
    }

    public String renderDot(ShardManager manager) {
        StringBuilder sb = new StringBuilder();
        sb.append("digraph shards {\n");
        sb.append("  rankdir=LR;\n");
        sb.append("  node [shape=record];\n");
        for (Shard shard : manager.getShards()) {
            sb.append("  shard").append(shard.getId())
                    .append(" [label=\"{Shard ").append(shard.getId())
                    .append("|UTXOs: ").append(shard.getUtxoPool().size())
                    .append("|Valor: ").append(shard.getUtxoPool().totalValue())
                    .append("|Bloqueados: ").append(shard.getLockedUtxos().size())
                    .append("|Recibos: ").append(shard.getConsumedReceipts().size())
                    .append("|Validadores online: ").append(shard.onlineValidators()).append('/').append(shard.getValidators().size())
                    .append("}\"];\n");
        }
        for (CrossShardSession session : manager.getSessions()) {
            String color = switch (session.status()) {
                case COMMITTED -> "green";
                case TIMED_OUT, ABORTED, FAILED_VALIDATION -> "red";
                case PENDING -> "orange";
            };
            sb.append("  shard").append(session.transfer().sourceShardId())
                    .append(" -> shard").append(session.transfer().targetShardId())
                    .append(" [label=\"").append(session.status()).append(" monto=").append(session.transfer().amount())
                    .append("\", color=\"").append(color).append("\"];\n");
        }
        sb.append("}\n");
        return sb.toString();
    }
}
