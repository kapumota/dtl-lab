package dltlab.visualization;

import dltlab.blockchain.Block;
import dltlab.blockchain.BlockChain;
import dltlab.crypto.Hashing;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Genera visualizaciones ASCII y DOT del arbol de forks. */
public class ForkTreeVisualizer {
    public String renderAscii(BlockChain chain) {
        Map<String, Block> blocks = new HashMap<>();
        Map<String, List<Block>> children = new HashMap<>();
        String rootId = null;

        for (Block block : chain.getKnownBlocks()) {
            String id = Hashing.hex(block.getHash());
            blocks.put(id, block);
            if (block.getPreviousBlockHash() == null) {
                rootId = id;
            } else {
                String parentId = Hashing.hex(block.getPreviousBlockHash());
                children.computeIfAbsent(parentId, ignored -> new ArrayList<>()).add(block);
            }
        }

        if (rootId == null) {
            return "No hay bloque genesis disponible para visualizar.";
        }

        for (List<Block> list : children.values()) {
            list.sort(Comparator.comparing(Block::shortId));
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Arbol de forks\n");
        renderNode(sb, blocks.get(rootId), children, chain, "", true);
        return sb.toString();
    }

    private void renderNode(StringBuilder sb,
                            Block block,
                            Map<String, List<Block>> children,
                            BlockChain chain,
                            String prefix,
                            boolean last) {
        boolean isMax = Hashing.hex(block.getHash()).equals(Hashing.hex(chain.getMaxHeightBlock().getHash()));
        sb.append(prefix)
                .append(last ? "`-- " : "+-- ")
                .append(block.shortId())
                .append(" h=").append(block.getDeclaredHeight())
                .append(block.getPreviousBlockHash() == null ? " genesis" : "")
                .append(isMax ? "  <- cabeza actual" : "")
                .append('\n');

        List<Block> blockChildren = children.getOrDefault(Hashing.hex(block.getHash()), List.of());
        for (int i = 0; i < blockChildren.size(); i++) {
            renderNode(sb, blockChildren.get(i), children, chain, prefix + (last ? "    " : "|   "), i == blockChildren.size() - 1);
        }
    }

    public String renderDot(BlockChain chain) {
        StringBuilder sb = new StringBuilder();
        sb.append("digraph forks {\n");
        sb.append("  rankdir=TB;\n");
        sb.append("  node [shape=box];\n");
        String maxId = Hashing.hex(chain.getMaxHeightBlock().getHash());
        for (Block block : chain.getKnownBlocks()) {
            String id = Hashing.hex(block.getHash());
            String label = block.shortId() + "\\nh=" + block.getDeclaredHeight();
            String style = id.equals(maxId) ? ", style=filled" : "";
            sb.append("  \"").append(id).append("\" [label=\"").append(label).append("\"").append(style).append("];\n");
            if (block.getPreviousBlockHash() != null) {
                sb.append("  \"").append(Hashing.hex(block.getPreviousBlockHash())).append("\" -> \"").append(id).append("\";\n");
            }
        }
        sb.append("}\n");
        return sb.toString();
    }
}
