package gg.desolve.melee.player.profile;

import lombok.Data;
import org.bson.Document;

@Data
public class Marker {

    private String address;
    private int logins;
    private Long firstSeen;
    private Long lastSeen;

    public Marker(String address, int logins, Long firstSeen, Long lastSeen) {
        this.address = address;
        this.logins = logins;
        this.firstSeen = firstSeen;
        this.lastSeen = lastSeen;
    }

    public static Marker load(Document document) {
        return new Marker(
                document.getString("address"),
                document.getInteger("logins"),
                document.getLong("firstSeen"),
                document.getLong("lastSeen")
        );
    }

    public Document save() {
        return new Document()
                .append("address", address)
                .append("logins", logins)
                .append("firstSeen", firstSeen)
                .append("lastSeen", lastSeen);
    }
}
