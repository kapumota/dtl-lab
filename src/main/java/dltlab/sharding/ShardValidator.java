package dltlab.sharding;

/** Validador educativo asociado a un shard. */
public class ShardValidator {
    private final String id;
    private final int shardId;
    private final long weight;
    private boolean online;
    private boolean honest;

    public ShardValidator(String id, int shardId, long weight, boolean online, boolean honest) {
        this.id = id;
        this.shardId = shardId;
        this.weight = weight;
        this.online = online;
        this.honest = honest;
    }

    public String id() { return id; }
    public int shardId() { return shardId; }
    public long weight() { return weight; }
    public boolean online() { return online; }
    public boolean honest() { return honest; }

    public void setOnline(boolean online) { this.online = online; }
    public void setHonest(boolean honest) { this.honest = honest; }

    /** En esta version, un validador vota si esta online y no esta marcado como malicioso. */
    public boolean approves() {
        return online && honest;
    }
}
