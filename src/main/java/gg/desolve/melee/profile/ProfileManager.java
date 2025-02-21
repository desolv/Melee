package gg.desolve.melee.profile;

import com.google.gson.Gson;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOptions;
import gg.desolve.mithril.Mithril;
import lombok.Getter;
import org.bson.Document;
import org.bukkit.Bukkit;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Getter
public class ProfileManager {

    private final Map<UUID, Profile> records = new ConcurrentHashMap<>();
    private static final Gson gson = new Gson();

    public Profile retrieve(Object object) {
        UUID uuid = object instanceof UUID ? (UUID) object : Bukkit.getOfflinePlayer((String) object).getUniqueId();

        Profile profileCache = records.get(uuid);
        if (profileCache != null && System.currentTimeMillis() - profileCache.getTimestamp() < (1800 * 1000))
            return profileCache;

        return reclaim(uuid);
    }

    public Profile reclaim(Object object) {
        UUID uuid = object instanceof UUID ? (UUID) object : Bukkit.getOfflinePlayer((String) object).getUniqueId();

        Profile profile = Mithril.getInstance().getMongoManager().getMongoDatabase().getCollection("profiles")
                .find(Filters.eq("uuid", uuid.toString()))
                .map(doc -> gson.fromJson(doc.toJson(), Profile.class))
                .first();

        if (profile == null)
            return new Profile(uuid, null);

        profile.setSchedules(new HashMap<>());
        profile.refreshGrants();
        profile.setTimestamp(System.currentTimeMillis());

        records.put(uuid, profile);
        return profile;
    }

    public void publish(Profile profile) {
        Mithril.getInstance().getRedisManager().publish("Profile", profile.getUuid().toString());
    }

    public void save() {
        Bukkit.getOnlinePlayers().forEach(player -> save(retrieve(player.getUniqueId())));
    }

    public void save(Profile profile) {
        Mithril.getInstance().getMongoManager().getMongoDatabase().getCollection("profiles")
                .replaceOne(
                        Filters.eq("uuid", profile.getUuid().toString()),
                        Document.parse(gson.toJson(profile)),
                        new ReplaceOptions().upsert(true));
    }
}
