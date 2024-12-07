package gg.desolve.melee.player.grant;

import gg.desolve.melee.configuration.MeleeConfigManager;
import gg.desolve.melee.player.rank.Rank;
import lombok.Data;
import org.bson.Document;

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
        return Rank.getRank(rank);
    }

    public boolean isPermanent() {
        return duration == Integer.MAX_VALUE;
    }

    public boolean hasExpired() {
        return (!isPermanent()) && (System.currentTimeMillis() >= (addedAt + duration));
    }

    public static Grant load(Document document) {
        Rank rank = Rank.getRank(document.getString("rank"));

        Grant grant = new Grant(
                document.getString("id"),
                rank == null ? document.getString("rank") : rank.getName(),
                document.get("addedBy", UUID.class),
                document.getLong("addedAt"),
                document.getString("addedReason"),
                document.getString("addedOrigin"),
                document.getString("scope"),
                document.getLong("duration"),
                rank == null ? GrantType.REMOVED : GrantType.string(document.getString("type"))
        );

        if ((grant.getScope().equalsIgnoreCase("global") && grant.getScope().equalsIgnoreCase(MeleeConfigManager.lang.getString("server_name")))
                && grant.getType().equals(GrantType.ACTIVE)
                && Rank.getRank(document.getString("rank")) == null) {
            grant.setType(GrantType.REMOVED);
            grant.setRemovedBy(null);
            grant.setRemovedAt(System.currentTimeMillis());
            grant.setRemovedReason("Automatic Deletion");
            grant.setRemovedOrigin(MeleeConfigManager.lang.getString("server_name"));
            return grant;
        }

        if (document.get("removedBy") != null) grant.setRemovedBy(document.get("removedBy", UUID.class));
        if (document.get("removedAt") != null) grant.setRemovedAt(document.getLong("removedAt"));
        if (document.get("removedReason") != null) grant.setRemovedReason(document.getString("removedReason"));
        if (document.get("removedOrigin") != null) grant.setRemovedOrigin(document.getString("removedOrigin"));

        return grant;
    }

    public Document save() {
        return new Document()
                .append("id", id)
                .append("rank", rank)
                .append("addedBy", addedBy)
                .append("addedAt", addedAt)
                .append("addedReason", addedReason)
                .append("addedOrigin", addedOrigin)
                .append("scope", scope)
                .append("duration", duration)
                .append("removedBy", removedBy)
                .append("removedAt", removedAt)
                .append("removedReason", removedReason)
                .append("removedOrigin", removedOrigin)
                .append("type", type);
    }

}
