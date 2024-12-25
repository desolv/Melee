package gg.desolve.melee.player.punishment;

import gg.desolve.melee.common.Converter;
import gg.desolve.melee.common.Message;
import gg.desolve.melee.configuration.MeleeConfigManager;
import gg.desolve.melee.player.grant.Grant;
import gg.desolve.melee.player.grant.GrantType;
import gg.desolve.melee.player.rank.Rank;
import gg.desolve.melee.server.MeleeServerManager;
import lombok.Data;
import org.bson.Document;

import java.util.UUID;

@Data
public class Punishment {

    private final String id;
    private final PunishmentStyle style;
    private final String address;
    private UUID addedBy;
    final private long addedAt;
    private final String addedReason;
    private final String addedOrigin;
    private String scope;
    final private long duration;
    private UUID removedBy;
    private long removedAt;
    private String removedReason;
    private String removedOrigin;
    private PunishmentType type;

    public Punishment(String id, PunishmentStyle style, String address, UUID addedBy, long addedAt, String addedReason, String addedOrigin, String scope, long duration, PunishmentType type) {
        this.id = id;
        this.style = style;
        this.address = address;
        this.addedBy = addedBy;
        this.addedAt = addedAt;
        this.addedReason = addedReason;
        this.addedOrigin = addedOrigin;
        this.scope = scope;
        this.duration = duration;
        this.type = type;
    }

    public String getRemaining() {
        return Converter.millisToTime((duration + addedAt + 1000) - System.currentTimeMillis());
    }

    public boolean isPermanent() {
        return duration == Integer.MAX_VALUE;
    }

    public boolean hasExpired() {
        return (!isPermanent()) && (System.currentTimeMillis() >= (addedAt + duration));
    }

    public String getMessage() {
        return (switch (style) {
            case BLACKLIST -> MeleeConfigManager.lang.getString("punishment.blacklist");
            case MUTE -> MeleeConfigManager.lang.getString("punishment.mute");
            case WARN -> MeleeConfigManager.lang.getString("punishment.warn");
            case KICK -> MeleeConfigManager.lang.getString("punishment.kick");
            case BAN -> isPermanent() ? MeleeConfigManager.lang.getString("punishment.ban-permanent")
                    : MeleeConfigManager.lang.getString("punishment.ban-temporary");
        })
                .replace("appeal%", MeleeConfigManager.lang.getString("punishment.appeal"))
                .replace("reason%", addedReason)
                .replace("duration%", isPermanent() ? "permanently" : "temporarily")
                .replace("time%", getRemaining());
    }

    public static Punishment load(Document document) {
        Punishment punishment = new Punishment(
                document.getString("id"),
                PunishmentStyle.string(document.getString("style")),
                document.getString("address"),
                document.get("addedBy", UUID.class),
                document.getLong("addedAt"),
                document.getString("addedReason"),
                document.getString("addedOrigin"),
                document.getString("scope"),
                document.getLong("duration"),
                PunishmentType.string(document.getString("type"))
        );

        if (document.get("removedBy") != null) punishment.setRemovedBy(document.get("removedBy", UUID.class));
        if (document.get("removedAt") != null) punishment.setRemovedAt(document.getLong("removedAt"));
        if (document.get("removedReason") != null) punishment.setRemovedReason(document.getString("removedReason"));
        if (document.get("removedOrigin") != null) punishment.setRemovedOrigin(document.getString("removedOrigin"));

        return punishment;
    }

    public Document save() {
        return new Document()
                .append("id", id)
                .append("style", style)
                .append("address", address)
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
