package gg.desolve.melee.player.profile;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOptions;
import gg.desolve.melee.Melee;
import gg.desolve.melee.common.Converter;
import gg.desolve.melee.player.grant.Grant;
import gg.desolve.melee.player.grant.GrantType;
import gg.desolve.melee.rank.Rank;
import lombok.Data;
import lombok.Getter;
import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionAttachmentInfo;

import java.util.*;
import java.util.stream.Collectors;

@Data
public class Profile {

    @Getter
    private static MongoCollection<Document> mongoCollection = Melee.getInstance().getMongoManager().getMongoDatabase().getCollection("profiles");

    @Getter
    private static Map<UUID, Profile> profiles = new HashMap<>();

    private final UUID uuid;
    private String username;
    private int logins;
    private Long firstSeen;
    private Long lastSeen;
    private String address;
    private boolean loaded;
    private Grant grant;
    private List<Marker> markers;
    private List<Grant> grants;

    public Profile(UUID uuid, String username) {
        this.uuid = uuid;
        this.username = username;
        this.markers = new ArrayList<>();
        this.grants = new ArrayList<>();

        load();
        evaluateGrants();
        refreshGrants();
        save();
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

    public String getUsernameColored() {
        return grant.getRank().getColor() + username;
    }

    public boolean hasPermission(String permission) {
        Player player = Bukkit.getPlayer(uuid);

        if (player != null) {
            return player.hasPermission("melee.*") || Arrays.stream(permission.split("\\|"))
                    .anyMatch(player::hasPermission);
        }

        return Arrays.stream(permission.split("\\|"))
                .anyMatch(perm -> grants.stream()
                        .filter(grant -> grant.getType() == GrantType.ACTIVE)
                        .anyMatch(grant -> grant.getRank().getPermissionsAndInherited().contains("melee.*") ||
                                grant.getRank().getPermissionsAndInherited().contains(perm)));
    }

    public Grant hasGrant(Rank rank) {
        return grants.stream()
                .filter(grant -> grant.getRank().equals(rank) && grant.getType().equals(GrantType.ACTIVE))
                .findFirst()
                .orElse(null);
    }

    public void refreshGrants() {
        grant = grants.stream()
                .filter(grant -> grant.getType() == GrantType.ACTIVE)
                .sorted(Comparator.comparingInt(grant -> -grant.getRank().getPriority()))
                .findFirst().get();
        refreshPermissions();
    }

    public void evaluateGrants() {
        if (grant != null && !grant.getType().equals(GrantType.ACTIVE)) {
            refreshGrants();
            return;
        }

        Optional<Grant> missing = grants.stream()
                .filter(grant -> grant.getRank().equals(Rank.getDefault()))
                .findFirst();

        if (!missing.isPresent()) {
            grants.add(
                    new Grant(
                            Converter.generateId(),
                            Rank.getDefault(),
                            null,
                            System.currentTimeMillis(),
                            "Automatic",
                            Bukkit.getServerName(),
                            Integer.MAX_VALUE,
                            GrantType.ACTIVE
                    )
            );
        }
    }

    public void refreshPermissions() {
        Player player = Bukkit.getPlayer(uuid);
        if (player == null) return;

        Set<PermissionAttachmentInfo> attachmentInfo = player.getEffectivePermissions();

        // Filter out only the permissions attached by the plugin
        attachmentInfo.stream()
                .filter(info -> info.getAttachment() != null &&
                        info.getAttachment().getPlugin() != null &&
                        info.getAttachment().getPlugin().equals(Melee.getInstance()))
                .map(PermissionAttachmentInfo::getAttachment)
                .forEach(PermissionAttachment::remove);

        PermissionAttachment attachment = player.addAttachment(Melee.getInstance());

        grants.stream()
                .filter(grant -> grant.getType() == GrantType.ACTIVE)
                .forEach(grant -> grant.getRank().getPermissionsAndInherited().forEach(permission -> {
                            if (!attachment.getPermissions().containsKey(permission))
                                attachment.setPermission(permission, true);
                        })
                );

        player.recalculatePermissions();
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
                Optional.ofNullable(document.getList("markers", Document.class))
                        .ifPresent(m -> m.forEach(markerDoc -> markers.add(Marker.load(markerDoc))));
                Optional.ofNullable(document.getList("grants", Document.class))
                        .ifPresent(m -> m.forEach(grantDoc -> grants.add(Grant.load(grantDoc))));
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
            document.put("markers", markers.stream().map(Marker::save).collect(Collectors.toList()));
            document.put("grants", grants.stream().map(Grant::save).collect(Collectors.toList()));

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
