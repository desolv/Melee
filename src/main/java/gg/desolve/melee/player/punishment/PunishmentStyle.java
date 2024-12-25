package gg.desolve.melee.player.punishment;

import java.util.Arrays;

public enum PunishmentStyle {

    BLACKLIST,
    BAN,
    MUTE,
    WARN,
    KICK;

    public String toString() {
        return this.name();
    }

    public static PunishmentStyle string(String value) {
        return Arrays.stream(PunishmentStyle.values())
                .filter(punishmentStyle -> punishmentStyle.name().equalsIgnoreCase(value))
                .findFirst()
                .orElse(null);
    }

}
