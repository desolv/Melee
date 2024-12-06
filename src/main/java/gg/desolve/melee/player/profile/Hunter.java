package gg.desolve.melee.player.profile;

import com.google.gson.Gson;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOptions;
import gg.desolve.melee.Melee;
import gg.desolve.melee.common.Converter;
import gg.desolve.melee.common.Message;
import gg.desolve.melee.common.Schedule;
import gg.desolve.melee.player.grant.Grant;
import gg.desolve.melee.player.grant.GrantType;
import gg.desolve.melee.player.rank.Rank;
import lombok.Data;
import lombok.Getter;
import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionAttachmentInfo;
import redis.clients.jedis.Jedis;

import java.util.*;
import java.util.stream.Collectors;

@Data
public class Hunter {

    @Getter
    private transient final MongoCollection<Document> hunter_collections = Melee.getInstance().getMongoManager().getMongoDatabase().getCollection("hunters");

    private transient final Gson gson = new Gson();

    private final UUID uuid;
    private String username;
    private int logins;
    private Long firstSeen;
    private Long lastSeen;
    private String server;
    private String address;
    private boolean loaded;
    private Grant grant;
    private List<Grant> grants;
    private List<Marker> markers;
    private List<Schedule> schedules;

    public Hunter(UUID uuid, String username) {
        this.uuid = uuid;
        this.username = username;
        this.schedules = new ArrayList<>();
        this.grants = new ArrayList<>();
        this.markers = new ArrayList<>();

        load();
        evaluateGrants();
        refreshGrant();
        save();
    }

    public static Hunter getHunter(UUID uuid) {
        return new Hunter(uuid, null);
    }

    public static Hunter getHunter(String username) {
        Player player = Bukkit.getPlayer(username);
        if (player != null) return new Hunter(player.getUniqueId(), player.getName());

        try (Jedis jedis = Melee.getInstance().getRedisManager().getConnection()) {
            String hunterJson = jedis.get("hunter:username:" + username);
            if (hunterJson != null) {
                return new Hunter(Bukkit.getOfflinePlayer(username).getUniqueId(), username);
            }
        }

        Document hunterDoc = Melee.getInstance().getMongoManager().getMongoDatabase()
                .getCollection("hunters")
                .find(Filters.eq("username", username))
                .first();

        return hunterDoc != null ?
                new Hunter(UUID.fromString(hunterDoc.getString("uuid")), hunterDoc.getString("username")) :
                null;
    }



    public String getUsernameColored() {
        return getGrant().getRank().getColor() + username;
    }

    public Grant getGrant() {
        if (!grant.getRank().isVisible())
            refreshGrant();

        return grant;
    }

    public void refreshGrant() {
        grant = grants.stream()
                .filter(grant -> (grant.getType() == GrantType.ACTIVE && grant.getRank().isVisible())
                        && (grant.getScope().equalsIgnoreCase(Bukkit.getServerName()) || grant.getScope().equalsIgnoreCase("global")))
                .sorted(Comparator.comparingInt(grant -> -grant.getRank().getPriority()))
                .findFirst().get();
        if (!Melee.getInstance().isDisabling())
            refreshPermissions();
    }

    public Grant hasGrant(Rank rank) {
        return grants.stream()
                .filter(grant -> grant.getRank().equals(rank) && grant.getType().equals(GrantType.ACTIVE))
                .findFirst()
                .orElse(null);
    }

    public Grant hasGrant(String id) {
        return grants.stream()
                .filter(grant -> grant.getId().equals(id) && grant.getType().equals(GrantType.ACTIVE))
                .findFirst()
                .orElse(null);
    }

    public void evaluateGrants() {
        grants.stream()
                .filter(grant ->
                        (grant.getType() == GrantType.ACTIVE)
                        && !grant.isPermanent()
                )
                .forEach(grant -> {
                    if (grant.hasExpired() && grant.getType().equals(GrantType.ACTIVE)) {
                        grant.setRemovedAt(System.currentTimeMillis());
                        grant.setRemovedReason("Automatic");
                        grant.setRemovedOrigin(Bukkit.getServerName());
                        grant.setType(GrantType.EXPIRED);
                        save();

                        Player player = Bukkit.getPlayer(uuid);
                        if (player != null)
                            Message.send(player, grant.getRank().getDisplayColored() + " <green>rank has expired.");

                    } else if (!grant.hasExpired()
                            && Bukkit.getPlayer(uuid) != null
                            && !hasSchedule(grant.getId() + grant.getRank().getName())
                            && !grant.isPermanent() && Converter.millisToHours(grant.getDuration() + 1000) <= 24
                            && (grant.getScope().equalsIgnoreCase(Bukkit.getServerName()) || grant.getScope().equalsIgnoreCase("global"))
                    ) {
                        Runnable runnable = () -> {
                            if (username != null)
                                evaluateGrants();
                        };

                        addSchedule(
                                grant.getId() + grant.getRank().getName(),
                                runnable,
                                grant.getDuration() + 1000
                        );
                    }
                });

        if (grant != null && !grant.getType().equals(GrantType.ACTIVE)) {
            refreshGrant();
            return;
        }

        grants.stream()
                .filter(grant -> grant.getRank().equals(Rank.getDefault()))
                .findFirst()
                .orElseGet(() -> {
                    Grant grant = new Grant(
                            Converter.generateId(),
                            Rank.getDefault().getName(),
                            null,
                            System.currentTimeMillis(),
                            "Automatic",
                            Bukkit.getServerName(),
                            "global",
                            Integer.MAX_VALUE,
                            GrantType.ACTIVE
                    );
                    grants.add(grant);
                    return grant;
                });
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
                .filter(grant -> (grant.getType() == GrantType.ACTIVE)
                        && (grant.getScope().equalsIgnoreCase(Bukkit.getServerName()) || grant.getScope().equalsIgnoreCase("global")))
                .forEach(grant -> grant.getRank().getPermissionsAndInherited().forEach(p -> {

                    String permission = p.startsWith("-") ? p.substring(1) : p;
                    boolean negative = !p.startsWith("-");

                    if (!attachment.getPermissions().containsKey(permission))
                        attachment.setPermission(permission, negative);
                }));

        player.recalculatePermissions();
    }

    public void addSchedule(String identity, Runnable runnable, long millis) {
        if (!hasSchedule(identity)) {
            Schedule schedule = new Schedule(identity, runnable, millis);
            schedules.add(schedule);
            schedule.start();
            save();
        }
    }

    public boolean hasSchedule(String identity) {
        return schedules.stream().anyMatch(schedule -> schedule.getIdentity().equals(identity));
    }

    public void cancelSchedule(String identity) {
        schedules.stream()
                .filter(schedule -> schedule.getIdentity().equals(identity))
                .findFirst()
                .ifPresent(schedule -> {
                    schedule.cancel();
                    schedules.remove(schedule);
                });
    }

    public void cancelSchedules() {
        schedules.forEach(Schedule::cancel);
        schedules.clear();
    }

    public void load() {
        Hunter hunter = null;

        try (Jedis jedis = Melee.getInstance().getRedisManager().getConnection()) {
            String hunterJson = jedis.get("hunter:" + uuid.toString());
            if (hunterJson != null) {
                hunter = gson.fromJson(hunterJson, Hunter.class);
            }
        } catch (Exception e) {
            Melee.getInstance().getLogger().warning("There was a problem loading " + username + "'s redis.");
            e.printStackTrace();
        }

        if (hunter != null) {
            this.username = hunter.username;
            this.logins = hunter.logins;
            this.firstSeen = hunter.firstSeen;
            this.lastSeen = hunter.lastSeen;
            this.server = hunter.server;
            this.address = hunter.address;
            this.grants = hunter.grants != null ? hunter.grants : new ArrayList<>();
            this.markers = hunter.markers != null ? hunter.markers : new ArrayList<>();
            this.schedules = hunter.schedules != null ? hunter.schedules : new ArrayList<>();
            this.loaded = hunter.loaded;
            return;
        }

        try {
            Document document = hunter_collections.find(
                    Filters.eq(
                            "uuid",
                            uuid.toString())
            ).first();

            if (document != null) {
                username = document.getString("username");
                logins = document.getInteger("logins");
                firstSeen = document.getLong("firstSeen");
                lastSeen = document.getLong("lastSeen");
                server = document.getString("server");
                address = document.getString("address");
                Optional.ofNullable(document.getList("markers", Document.class))
                        .ifPresent(m -> m.forEach(marker -> markers.add(Marker.load(marker))));
                Optional.ofNullable(document.getList("grants", Document.class))
                        .ifPresent(g -> g.forEach(grant -> grants.add(Grant.load(grant))));
                save();
            }

        } catch (Exception e) {
            Melee.getInstance().getLogger().warning("There was a problem loading " + username + "'s document.");
            e.printStackTrace();
        }
    }

    public void save() {
        try (Jedis jedis = Melee.getInstance().getRedisManager().getConnection()) {
            String hunterJson = gson.toJson(this);
            jedis.set("hunter:" + uuid.toString(), hunterJson);
        } catch (Exception e) {
            Melee.getInstance().getLogger().warning("There was a problem saving " + username + "'s redis.");
            e.printStackTrace();
        }
    }

    public void expire() {
        try (Jedis jedis = Melee.getInstance().getRedisManager().getConnection()) {
            String hunterJson = gson.toJson(this);
            String key = "hunter:" + uuid.toString();
            jedis.set(key, hunterJson);
            jedis.expire(key, 300);
        } catch (Exception e) {
            Melee.getInstance().getLogger().warning("There was a problem expiring " + username + "'s redis.");
            e.printStackTrace();
        }
    }

    public void saveMongo() {
        try {
            Document document = new Document();
            document.put("uuid", uuid.toString());
            document.put("username", username);
            document.put("logins", logins);
            document.put("firstSeen", firstSeen);
            document.put("lastSeen", lastSeen);
            document.put("server", server);
            document.put("address", address);
            document.put("markers", markers.stream().map(Marker::save).collect(Collectors.toList()));
            document.put("grants", grants.stream().map(Grant::save).collect(Collectors.toList()));

            hunter_collections.replaceOne(
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
