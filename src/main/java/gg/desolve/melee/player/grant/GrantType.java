package gg.desolve.melee.player.grant;

import java.util.Arrays;

public enum GrantType {

    ACTIVE,
    REMOVED,
    EXPIRED;

    public String toString() {
        return this.name();
    }

    public static GrantType string(String value) {
        return Arrays.stream(GrantType.values())
                .filter(grantType -> grantType.name().equalsIgnoreCase(value))
                .findFirst()
                .orElse(null);
    }

}
