package gg.desolve.melee.common;

import lombok.Getter;

@Getter
public class Reason {

    private final String reason;
    private final boolean silent;

    public Reason(String reason, boolean silent) {
        this.reason = reason;
        this.silent = silent;
    }

    public static Reason fromString(String reason) {
        String[] parts = reason.split("\\s+");
        String silent = "-s";

        if (parts.length > 1 && parts[parts.length - 1].startsWith("-")) {
            silent = parts[parts.length - 1];
            reason = reason.substring(0, reason.lastIndexOf(silent)).trim();
        }

        return new Reason(reason, silent.equalsIgnoreCase("-s") || silent.equalsIgnoreCase("-silent"));
    }
}
