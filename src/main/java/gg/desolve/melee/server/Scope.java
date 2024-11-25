package gg.desolve.melee.server;

import lombok.Getter;

@Getter
public class Scope {

    private final String value;

    public Scope(String value) {
        this.value = value;
    }

    public static Scope fromString(String source) {
        for (Server server : MeleeServerManager.getServers()) {
            if (source.equalsIgnoreCase("global")){
                return new Scope("global");
            } else if (server.getName().equalsIgnoreCase(source)) {
                return new Scope(server.getName());
            }
        }
        return null;
    }

}
