package gg.desolve.melee.player.profile;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOptions;
import gg.desolve.melee.Melee;
import lombok.Data;
import lombok.Getter;
import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.util.*;

@Data
public class Profile {

    @Getter
    private static MongoCollection<Document> mongoCollection = Melee.getMongoManager().getMongoDatabase().getCollection("profiles");

    @Getter
    private static Map<UUID, Profile> profiles = new HashMap<>();

    private final UUID uuid;
    private String username;
    private int logins;
    private Long firstSeen;
    private Long lastSeen;
    private String address;
    private boolean loaded;

    public Profile(UUID uuid, String username) {
        this.uuid = uuid;
        this.username = username;

        load();
    }

    public static Profile getProfile(UUID uuid) {
        return profiles.containsKey(uuid) ?
                profiles.get(uuid) :
                new Profile(uuid, null);
    }

    public static Profile getProfile(String username) {
        Profile profile = Optional.ofNullable(Bukkit.getPlayer(username))
                .map(player -> profiles.get(player.getUniqueId()))
                .orElse(null);

        if (profile == null) {
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(username);
            profile = offlinePlayer.hasPlayedBefore() && profiles.containsKey(offlinePlayer.getUniqueId())
                    ? profiles.get(offlinePlayer.getUniqueId())
                    : new Profile(offlinePlayer.getUniqueId(), offlinePlayer.getName());
        }

        return profile;
    }

    public void load() {
        try {
            Document document = mongoCollection.find(
                    Filters.eq(
                            "uuid",
                            uuid.toString())
            ).first();

            if (document != null) {
                username = document.getString("username");
                logins = document.getInteger("logins");
                firstSeen = document.getLong("firstSeen");
                lastSeen = document.getLong("lastSeen");
                address = document.getString("address");
            }

        } catch (Exception e) {
            Melee.getInstance().getLogger().warning("There was a problem loading " + username + "'s document.");
            e.printStackTrace();
        }
    }

    public void save() {
        try {
            Document document = new Document();
            document.put("uuid", uuid.toString());
            document.put("username", username);
            document.put("logins", logins);
            document.put("firstSeen", firstSeen);
            document.put("lastSeen", lastSeen);
            document.put("address", address);

            mongoCollection.replaceOne(
                    Filters.eq(
                            "uuid",
                            uuid.toString()),
                    document,
                    new ReplaceOptions().upsert(true)
            );

        } catch (Exception e) {
            Melee.getInstance().getLogger().warning("There was a problem saving " + username + "'s document.");
            e.printStackTrace();
        }
    }

}
