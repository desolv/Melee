package gg.desolve.melee.socket;

import lombok.Data;

@Data
public class Socket {

    private String address;
    private int logins;
    private Long firstSeen;
    private Long lastSeen;

    public Socket(String address, int logins, Long firstSeen, Long lastSeen) {
        this.address = address;
        this.logins = logins;
        this.firstSeen = firstSeen;
        this.lastSeen = lastSeen;
    }
}
