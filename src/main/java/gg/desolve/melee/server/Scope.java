package gg.desolve.melee.server;

import lombok.Getter;

@Getter
public class Scope {

    private final String source;
    private final String server;

    public Scope(String source, String server) {
        this.source = source;
        this.server = server;
    }

    public static Scope fromString(String source) {
        for (Server server : MeleeServerManager.getServers()) {
            if (source.equalsIgnoreCase("global")){
                return new Scope(source, "global");
            } else if (server.getName().equalsIgnoreCase(source)) {
                return new Scope(source, server.getName());
            }
        }
        return null;
    }

}
