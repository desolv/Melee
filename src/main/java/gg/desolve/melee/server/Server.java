package gg.desolve.melee.server;

import lombok.Data;

@Data
public class Server {

    private final String id;
    private final String name;
    private final String version;
    private final String melee;
    private final long booting;
    private long heartbeat;

    public Server(String id, String name, String version, String melee, long booting) {
        this.id = id;
        this.name = name;
        this.version = version;
        this.melee = melee;
        this.booting = booting;
        this.heartbeat = System.currentTimeMillis();
    }
}
