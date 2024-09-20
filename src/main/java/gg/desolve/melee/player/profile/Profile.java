package gg.desolve.melee.player.profile;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class Profile {

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
}
