package gg.desolve.melee.profile;

import gg.desolve.melee.Melee;
import gg.desolve.melee.grant.Grant;
import gg.desolve.melee.grant.GrantType;
import gg.desolve.melee.rank.Rank;
import gg.desolve.melee.socket.Socket;
import gg.desolve.mithril.Mithril;
import gg.desolve.mithril.relevance.Converter;
import gg.desolve.mithril.relevance.Message;
import gg.desolve.mithril.relevance.Schedule;
import lombok.Data;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.plugin.Plugin;

import java.util.*;

@Data
public class Profile {

    private final UUID uuid;
    private String username;
    private int logins;
    private Long firstSeen;
    private Long lastSeen;
    private String server;
    private String address;
    private boolean loaded;
    private transient long timestamp;
    private transient String process;
    private Grant grant;
    private List<Grant> grants;
    private List<Socket> sockets;
    private transient Map<String, Schedule> schedules;

    public Profile(UUID uuid, String username) {
        this.uuid = uuid;
        this.username = username;
        this.grants = new ArrayList<>();
        this.sockets = new ArrayList<>();
        this.schedules = new HashMap<>();
    }

    public String getUsernameColored() {
        return grant.getRank().getColor() + username;
    }

    public boolean hasScope(String scope) {
        List<String> scopes = Arrays.asList(scope.split("\\|"));

        return scopes.contains("global") ||
                scopes.contains(Mithril.getInstance().getInstanceManager().getInstance().getName());
    }

    public boolean hasGrant(Rank rank) {
        return grants.stream()
                .anyMatch(grant -> grant.getRank().getName().equals(rank.getName())
                        && grant.getType().equals(GrantType.ACTIVE));
    }

    public void setRankProcess(Rank rank, String process) {
        Player player = Bukkit.getPlayer(uuid);
        player.closeInventory();

        Message.send(player, "<yellow>Enter a new " + process + " for " + rank.getNameColored() + " <yellow>rank (Type 'cancel' to abort)");
        this.process = "rank:" + rank.getName() + ":" + process;
    }

    public void setRankCreateProcess() {
        Player player = Bukkit.getPlayer(uuid);
        player.closeInventory();

        Message.send(player, "<yellow>Enter a name for the new rank (Type 'cancel' to abort)");
        this.process = "create";
    }

    public Grant getActiveGrant(Rank rank) {
        return grants.stream()
                .filter(grant -> grant.getRank().getName().equals(rank.getName())
                        && grant.getType().equals(GrantType.ACTIVE))
                .findFirst()
                .orElse(null);
    }

    public List<String> getAllPermissions() {
        return grants.stream()
                .filter(grant -> grant.getType() == GrantType.ACTIVE)
                .flatMap(grant -> grant.getRank().getPermissions().stream())
                .distinct()
                .toList();
    }

    public boolean hasPermission(String permission) {
        Player player = Bukkit.getPlayer(uuid);

        if (player != null)
            return player.hasPermission("melee.*") || Arrays.stream(permission.split("\\|"))
                    .anyMatch(player::hasPermission);

        return Arrays.stream(permission.split("\\|"))
                .anyMatch(perm -> grants.stream()
                        .filter(grant -> grant.getType() == GrantType.ACTIVE)
                        .anyMatch(grant -> grant.getRank().hasPermission("melee.*|" + perm)));
    }

    public void addSchedule(String identity, Runnable runnable, long delay, Plugin plugin) {
        if (schedules.containsKey(identity))
            return;

        Runnable wrappedRunnable = () -> {
            try {
                runnable.run();
            } finally {
                removeSchedule(identity);
            }
        };

        Schedule schedule = new Schedule(identity, wrappedRunnable, delay, plugin);
        schedule.start();
        schedules.put(identity, schedule);
    }

    public boolean hasSchedule(String identity) {
        return schedules.containsKey(identity);
    }

    public void removeSchedule(String identity) {
        if (schedules.containsKey(identity)) {
            schedules.get(identity).cancel();
            schedules.remove(identity);
        }
    }

    public void removeSchedules() {
        schedules.values().forEach(Schedule::cancel);
        schedules.clear();
    }

    public void refreshPermissions() {
        if (!Melee.getInstance().isEnabled())
            return;

        Player player = Bukkit.getPlayer(uuid);
        if (player == null)
            return;

        player.getEffectivePermissions().stream()
                .map(PermissionAttachmentInfo::getAttachment)
                .filter(Objects::nonNull)
                .filter(att -> att.getPlugin() != null && att.getPlugin().equals(Melee.getInstance()))
                .forEach(att -> {
                    if (player.getEffectivePermissions().stream().anyMatch(info -> info.getAttachment() == att))
                        player.removeAttachment(att);
                });

        PermissionAttachment attachment = player.addAttachment(Melee.getInstance());

        grants.stream()
                .filter(grant -> (grant.getType() == GrantType.ACTIVE) && hasScope(grant.getScope()))
                .map(grant -> grant.getRank().getPermissions())
                .flatMap(Collection::stream)
                .distinct() // Avoid duplicates
                .forEach(permission -> {
                    boolean isNegative = permission.startsWith("-");
                    String cleanedPermission = isNegative ? permission.substring(1) : permission;
                    attachment.setPermission(cleanedPermission, !isNegative);
                });

        player.recalculatePermissions();
    }

    public void refreshGrants() {
        Player player = Bukkit.getPlayer(uuid);

        if (!hasGrant(Melee.getInstance().getRankManager().primary()))
            grants.add(new Grant(
                    Converter.generateId(),
                    Melee.getInstance().getRankManager().primary().getName(),
                    null,
                    System.currentTimeMillis(),
                    "Automatic",
                    Mithril.getInstance().getInstanceManager().getInstance().getName(),
                    "global",
                    Integer.MAX_VALUE,
                    GrantType.ACTIVE));

        grants.stream()
                .filter(grant -> {
                    if (grant.getType().equals(GrantType.EXPIRED) || grant.getType().equals(GrantType.REMOVED)) return false;

                    boolean isPermanent = grant.isPermanent();
                    boolean hasExpired = grant.hasExpired();
                    boolean hasRank = grant.getRank() != null;
                    boolean hasSchedule = hasSchedule(grant.getId() + grant.getRank().getName());

                    if ((!isPermanent && hasExpired) || !hasRank) {
                        grant.setRemovedAt(System.currentTimeMillis());
                        grant.setRemovedReason("Automatic");
                        grant.setRemovedOrigin(Mithril.getInstance().getInstanceManager().getInstance().getName());
                        grant.setType(GrantType.EXPIRED);

                        String rankMessage = hasRank
                                ? grant.getRank().getDisplayColored() + " <green>rank has expired."
                                : "<white>" + grant.getSimple() + " <green>rank has been removed.";

                        Message.send(player, rankMessage);
                        return false;
                    }

                    return !hasSchedule
                            && !isPermanent
                            && Converter.hours(grant.getDuration() + 1000) <= 24
                            && hasScope(grant.getScope());
                })
                .forEach(grant -> {
                        if (!hasSchedule(grant.getId() + grant.getRank().getName()))
                            addSchedule(
                                    grant.getId() + grant.getRank().getName(),
                                    () -> Melee.getInstance().getProfileManager().retrieve(uuid)
                                            .refreshGrants(),
                                    grant.getDuration() + 1000,
                                    Melee.getInstance());
                });

        grant = grants.stream()
                .filter(grant ->
                        (grant.getType() == GrantType.ACTIVE && grant.getRank().isVisible())
                        && hasScope(grant.getScope()))
                .sorted(Comparator.comparingInt(grant -> -grant.getRank().getPriority()))
                .findFirst()
                .get();

        refreshPermissions();
        save();
    }

    public void save() {
        ProfileManager profileManager = Melee.getInstance().getProfileManager();
        profileManager.save(this);

        if (!server.equals(Mithril.getInstance().getInstanceManager().getInstance().getId()))
            profileManager.publish(this);
    }
}
