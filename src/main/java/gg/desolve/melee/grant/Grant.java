package gg.desolve.melee.grant;

import gg.desolve.melee.Melee;
import gg.desolve.melee.rank.Rank;
import lombok.Data;

import java.util.UUID;

@Data
public class Grant {

    private final String id;
    private final String rank;
    private UUID addedBy;
    private long addedAt;
    private String addedReason;
    private String addedOrigin;
    private String scope;
    private long duration;
    private UUID removedBy;
    private long removedAt;
    private String removedReason;
    private String removedOrigin;
    private GrantType type;

    public Grant(String id, String rank, UUID addedBy, long addedAt, String addedReason, String addedOrigin, String scope, long duration, GrantType type) {
        this.id = id;
        this.rank = rank;
        this.addedBy = addedBy;
        this.addedAt = addedAt;
        this.addedReason = addedReason;
        this.addedOrigin = addedOrigin;
        this.scope = scope;
        this.duration = duration;
        this.type = type;
    }

    public Rank getRank() {
        return Melee.getInstance().getRankManager().retrieve(rank);
    }

    public String getSimple() {
        return rank;
    }

    public boolean isPermanent() {
        return duration == Integer.MAX_VALUE;
    }

    public boolean hasExpired() {
        return (!isPermanent()) && (System.currentTimeMillis() >= (addedAt + duration));
    }

}
