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
import gg.desolve.melee.player.punishment.Punishment;
import gg.desolve.melee.player.punishment.PunishmentStyle;
import gg.desolve.melee.player.punishment.PunishmentType;
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

    private final UUID uuid;
    private String username;
    private int logins;
    private Long firstSeen;
    private Long lastSeen;
    private Long playtime;
    private String server;
    private String address;
    private boolean loaded;
    private Grant grant;
    private List<Grant> grants;
    private List<Punishment> punishments;
    private List<Marker> markers;
    private List<Schedule> schedules;

    public Hunter(UUID uuid, String username) {
        this.uuid = uuid;
        this.username = username;
        this.schedules = new ArrayList<>();
        this.grants = new ArrayList<>();
        this.punishments = new ArrayList<>();
        this.markers = new ArrayList<>();

        load();
        evaluateGrants();
        refreshGrant();
        evaluatePunishments();
        save();
    }

    public static Hunter getHunter(UUID uuid) {
        return new Hunter(uuid, null);
    }

    public static Hunter getHunter(String username) {
        Player player = Bukkit.getPlayer(username);
        if (player != null) return new Hunter(player.getUniqueId(), player.getName());

        String hunterJson = Melee.getInstance().getRedisManager().get("hunter:" + Bukkit.getOfflinePlayer(username).getUniqueId());
        if (hunterJson != null)
            return new Hunter(Bukkit.getOfflinePlayer(username).getUniqueId(), username);

        Document hunterDoc = Melee.getInstance().getMongoManager().getMongoDatabase()
                .getCollection("hunters")
                .find(
                        Filters.eq(
                                "uuid",
                                Bukkit.getOfflinePlayer(username).getUniqueId().toString()))
                .first();

        return hunterDoc != null ?
                new Hunter(UUID.fromString(hunterDoc.getString("uuid")), hunterDoc.getString("username")) :
                null;
    }



    public String getUsernameColored() {
        return getGrant().getRank().getColor() + username;
    }

    public String generateGrantId() {
        String generateId;
        do {
            generateId = Converter.generateId();
        } while (hasGrant(generateId) != null);
        return generateId;
    }

    public String generatePunishmentId() {
        String generateId;
        do {
            generateId = Converter.generateId();
        } while (hasPunishment(generateId) != null);
        return generateId;
    }

    public Grant getGrant() {
        if (!grant.getRank().isVisible())
            refreshGrant();

        return grant;
    }

    public void refreshGrant() {
        grant = grants.stream()
                .filter(grant -> (grant.getType() == GrantType.ACTIVE && grant.getRank().isVisible())
                        && (grant.getScope().equalsIgnoreCase(Melee.getInstance().getConfig("language.yml").getString("server_name")) || grant.getScope().equalsIgnoreCase("global")))
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

    public Punishment hasPunishment(PunishmentStyle style) {
        return punishments.stream()
                .filter(p -> p.getStyle().equals(style) && p.getType().equals(PunishmentType.ACTIVE))
                .findFirst()
                .orElse(null);
    }

    public Punishment hasPunishment(String id) {
        return punishments.stream()
                .filter(punishment -> punishment.getId().equals(id) && punishment.getType().equals(PunishmentType.ACTIVE))
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
                        grant.setRemovedOrigin(Melee.getInstance().getConfig("language.yml").getString("server_name"));
                        grant.setType(GrantType.EXPIRED);
                        save();

                        Player player = Bukkit.getPlayer(uuid);
                        if (player != null)
                            Message.send(player, grant.getRank().getDisplayColored() + " <green>rank has expired.");

                    } else if (!grant.hasExpired()
                            && Bukkit.getPlayer(uuid) != null
                            && !hasSchedule(grant.getId() + grant.getRank().getName())
                            && !grant.isPermanent() && Converter.millisToHours(grant.getDuration() + 1000) <= 24
                            && (grant.getScope().equalsIgnoreCase(Melee.getInstance().getConfig("language.yml").getString("server_name")) || grant.getScope().equalsIgnoreCase("global"))
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
                            Melee.getInstance().getConfig("language.yml").getString("server_name"),
                            "global",
                            Integer.MAX_VALUE,
                            GrantType.ACTIVE
                    );
                    grants.add(grant);
                    return grant;
                });
    }

    public void evaluatePunishments() {
        punishments.stream()
                .filter(punishment ->
                        (punishment.getType() == PunishmentType.ACTIVE)
                                && !punishment.isPermanent()
                )
                .forEach(punishment -> {
                    if (punishment.hasExpired() && punishment.getType().equals(PunishmentType.ACTIVE)) {
                        punishment.setRemovedAt(System.currentTimeMillis());
                        punishment.setRemovedReason("Automatic");
                        punishment.setRemovedOrigin(Melee.getInstance().getConfig("language.yml").getString("server_name"));
                        punishment.setType(PunishmentType.EXPIRED);
                        save();
                    } else if (!punishment.hasExpired()
                            && Bukkit.getPlayer(uuid) != null
                            && !hasSchedule(punishment.getId() + punishment.getStyle().toString())
                            && !punishment.isPermanent() && Converter.millisToHours(punishment.getDuration() + 1000) <= 24
                            && (punishment.getScope().equalsIgnoreCase(Melee.getInstance().getConfig("language.yml").getString("server_name")) || punishment.getScope().equalsIgnoreCase("global"))
                    ) {
                        Runnable runnable = () -> {
                            if (username != null)
                                evaluatePunishments();
                        };

                        addSchedule(
                                punishment.getId() + punishment.getStyle().toString(),
                                runnable,
                                punishment.getDuration() + 1000
                        );
                    }
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
                        && (grant.getScope().equalsIgnoreCase(Melee.getInstance().getConfig("language.yml").getString("server_name")) || grant.getScope().equalsIgnoreCase("global")))
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

        String hunterJson = Melee.getInstance().getRedisManager().get("hunter:" + uuid.toString());
        if (hunterJson != null)
            hunter = Melee.getInstance().gson.fromJson(hunterJson, Hunter.class);

        if (hunter != null) {
            this.username = hunter.username;
            this.logins = hunter.logins;
            this.firstSeen = hunter.firstSeen;
            this.lastSeen = hunter.lastSeen;
            this.playtime = hunter.playtime;
            this.server = hunter.server;
            this.address = hunter.address;
            this.grants = hunter.grants != null ? hunter.grants : new ArrayList<>();
            this.punishments = hunter.punishments != null ? hunter.punishments : new ArrayList<>();
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
                playtime = document.getLong("playtime");
                server = document.getString("server");
                address = document.getString("address");
                Optional.ofNullable(document.getList("markers", Document.class))
                        .ifPresent(m -> m.forEach(marker -> markers.add(Marker.load(marker))));
                Optional.ofNullable(document.getList("grants", Document.class))
                        .ifPresent(g -> g.forEach(grant -> grants.add(Grant.load(grant))));
                Optional.ofNullable(document.getList("punishments", Document.class))
                        .ifPresent(p -> p.forEach(punishment -> punishments.add(Punishment.load(punishment))));
                save();
            }

        } catch (Exception e) {
            Melee.getInstance().getLogger().warning("There was a problem loading " + username + "'s document.");
            e.printStackTrace();
        }
    }

    public void save() {
        Melee.getInstance().getRedisManager().set(
                "hunter:" + uuid.toString(),
                Melee.getInstance().gson.toJson(this));
    }

    public static void saveAll() {
        Bukkit.getOnlinePlayers().forEach(player -> {
            try {
                Hunter hunter = Hunter.getHunter(player.getUniqueId());
                hunter.setPlaytime(hunter.getPlaytime() + (System.currentTimeMillis() - hunter.getLastSeen()));
                hunter.setLastSeen(System.currentTimeMillis());
                hunter.setLoaded(false);
                hunter.saveMongo();
            } catch (Exception e) {
                Melee.getInstance().getLogger().warning("There was a problem saving " + player.getName() + "' on disable.");
            }
        });
    }

    public void expire() {
        Melee.getInstance().getRedisManager().set(
                "hunter:" + uuid.toString(),
                Melee.getInstance().gson.toJson(this),
                300);
    }

    public void saveMongo() {
        try {
            Document document = new Document();
            document.put("uuid", uuid.toString());
            document.put("username", username);
            document.put("logins", logins);
            document.put("firstSeen", firstSeen);
            document.put("lastSeen", lastSeen);
            document.put("playtime", playtime);
            document.put("server", server);
            document.put("address", address);
            document.put("markers", markers.stream().map(Marker::save).collect(Collectors.toList()));
            document.put("grants", grants.stream().map(Grant::save).collect(Collectors.toList()));
            document.put("punishments", punishments.stream().map(Punishment::save).collect(Collectors.toList()));

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
