package gg.desolve.melee.player.punishment;

import java.util.Arrays;

public enum PunishmentType {

    ACTIVE,
    REMOVED,
    EXPIRED;

    public String toString() {
        return this.name();
    }

    public static PunishmentType string(String value) {
        return Arrays.stream(PunishmentType.values())
                .filter(punishmentType -> punishmentType.name().equalsIgnoreCase(value))
                .findFirst()
                .orElse(null);
    }

}
