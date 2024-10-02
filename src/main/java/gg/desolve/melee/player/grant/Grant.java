package gg.desolve.melee.player.grant;

import gg.desolve.melee.rank.Rank;
import lombok.Data;
import org.bson.Document;
import org.bukkit.Bukkit;

import java.util.UUID;

@Data
public class Grant {

    private final String id;
    private final Rank rank;
    private UUID addedBy;
    private long addedAt;
    private String addedReason;
    private String addedOrigin;
    private long duration;
    private UUID removedBy;
    private long removedAt;
    private String removedReason;
    private String removedOrigin;
    private GrantType type;

    public Grant(String id, Rank rank, UUID addedBy, long addedAt, String addedReason, String addedOrigin, long duration, GrantType type) {
        this.id = id;
        this.rank = rank;
        this.addedBy = addedBy;
        this.addedAt = addedAt;
        this.addedReason = addedReason;
        this.addedOrigin = addedOrigin;
        this.duration = duration;
        this.type = type;
    }

    public static Grant load(Document document) {
        Rank rank = Rank.getRank(document.getString("rank"));

        Grant grant = new Grant(
                document.getString("id"),
                rank == null ? new Rank(document.getString("rank")) : rank,
                document.get("addedBy", UUID.class),
                document.getLong("addedAt"),
                document.getString("addedReason"),
                document.getString("addedOrigin"),
                document.getLong("duration"),
                rank == null ? GrantType.REMOVED : GrantType.string(document.getString("type"))
        );

        if (grant.getType().equals(GrantType.ACTIVE) && Rank.getRank(document.getString("rank")) == null) {
            grant.setType(GrantType.REMOVED);
            grant.setRemovedBy(null);
            grant.setRemovedAt(System.currentTimeMillis());
            grant.setRemovedReason("Automatic Deletion");
            grant.setRemovedOrigin(Bukkit.getServerName());
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
                .append("rank", rank.getName())
                .append("addedBy", addedBy)
                .append("addedAt", addedAt)
                .append("addedReason", addedReason)
                .append("addedOrigin", addedOrigin)
                .append("duration", duration)
                .append("removedBy", removedBy)
                .append("removedAt", removedAt)
                .append("removedReason", removedReason)
                .append("removedOrigin", removedOrigin)
                .append("type", type);
    }

}
