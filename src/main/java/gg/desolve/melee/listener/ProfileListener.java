package gg.desolve.melee.listener;

import gg.desolve.melee.Melee;
import gg.desolve.melee.common.Converter;
import gg.desolve.melee.common.Message;
import gg.desolve.melee.configuration.MeleeConfigManager;
import gg.desolve.melee.player.profile.Hunter;
import gg.desolve.melee.player.profile.Marker;
import gg.desolve.melee.player.punishment.Punishment;
import gg.desolve.melee.player.punishment.PunishmentStyle;
import gg.desolve.melee.player.punishment.PunishmentType;
import gg.desolve.melee.server.MeleeServerManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Optional;

public class ProfileListener implements Listener {

    @EventHandler(
            priority = EventPriority.HIGHEST,
            ignoreCancelled = true
    )
    public void onAsyncPlayerPreLogin(AsyncPlayerPreLoginEvent event) {
        Melee instance = Melee.getInstance();
        Player player = Bukkit.getPlayer(event.getUniqueId());

        if (player != null && player.isOnline()) {
            event.setLoginResult(AsyncPlayerPreLoginEvent.Result.KICK_OTHER);
            event.setKickMessage(
                    Message.translate(
                            "<red>You attempted to login shortly after disconnecting"
                                    + "<newline>Please try again in a few seconds"
                    ));
            instance.getServer().getScheduler().runTask(
                    instance, () -> player.kickPlayer(Message.translate("<red>Duplicate login :?")));
            return;
        }

        Hunter hunter = null;

        try {
            hunter = new Hunter(event.getUniqueId(), event.getName());
            hunter.setUsername(event.getName());

            if (hunter.getFirstSeen() == null)
                hunter.setFirstSeen(System.currentTimeMillis());

            hunter.setLastSeen(System.currentTimeMillis());

            if (hunter.getPlaytime() == null)
                hunter.setPlaytime(0L);

            if (hunter.getAddress() == null || !hunter.getAddress().equalsIgnoreCase(event.getAddress().getHostAddress()))
                hunter.setAddress(event.getAddress().getHostAddress());

            Optional<Marker> markerMissing = hunter.getMarkers().stream()
                    .filter(marker -> marker.getAddress() != null && marker.getAddress().equalsIgnoreCase(event.getAddress().getHostAddress()))
                    .findFirst();

            if (!markerMissing.isPresent()) {
                hunter.getMarkers().add(
                        new Marker(
                                event.getAddress().getHostAddress(),
                                0,
                                System.currentTimeMillis(),
                                System.currentTimeMillis()
                        ));
            }

            Optional<Punishment> punishment = hunter.getPunishments().stream()
                    .filter(p -> p.getType().equals(PunishmentType.ACTIVE)
                            && p.getStyle() != PunishmentStyle.MUTE
                            && p.getStyle() != PunishmentStyle.WARN
                            && p.getStyle() != PunishmentStyle.KICK)
                    .findFirst();

            if (punishment.isPresent()) {
                if (punishment.get().getScope().equalsIgnoreCase(MeleeConfigManager.lang.getString("server_name")) || punishment.get().getScope().equalsIgnoreCase("global")) {
                    event.setLoginResult(AsyncPlayerPreLoginEvent.Result.KICK_OTHER);
                    event.setKickMessage(punishment.get().getMessage());
                    return;
                }
            }

            hunter.setLoaded(true);
        } catch (Exception e) {
            instance.getLogger().warning("There was a problem loading " + event.getName() + "'s profile.");
            e.printStackTrace();
        }

        if (hunter == null || !hunter.isLoaded()) {
            event.setKickMessage(
                    Message.translate(
                            "<red>There was a problem loading your account"
                                    + "<newline>Please contact an administrator"
                    ));
            event.setLoginResult(AsyncPlayerPreLoginEvent.Result.KICK_OTHER);
            return;
        }

        hunter.save();
        hunter.saveMongo();
    }

    @EventHandler(
            priority = EventPriority.HIGHEST
    )
    public void onPlayerJoin(PlayerJoinEvent event) {
        event.setJoinMessage(null);
        Hunter hunter = Hunter.getHunter(event.getPlayer().getUniqueId());

        if (!hunter.isLoaded()) {
            event.getPlayer().kickPlayer(
                    Message.translate(
                            "<red>You attempted to login while booting"
                                    + "<newline>Please try again in a few seconds"
                    ));
            return;
        }

        hunter.setLogins(hunter.getLogins() + 1);
        hunter.getMarkers().stream()
                .filter(marker -> marker.getAddress() != null && marker.getAddress().equalsIgnoreCase(hunter.getAddress()))
                .findFirst()
                .ifPresent(marker -> marker.setLogins(marker.getLogins() + 1));
        hunter.setServer(MeleeConfigManager.lang.getString("server_name"));
        hunter.save();
        hunter.saveMongo();

        if (MeleeServerManager.getReboot().getDelay() > 0) {
            Message.staff(event.getPlayer(), "melee.admin",
                    ((Converter.millisToSeconds(
                            (MeleeServerManager.getReboot().getAddedAt() + MeleeServerManager.getReboot().getDelay())
                                    - System.currentTimeMillis()) <= 60) ? "<red>" : "<green>") +
                    "Instance scheduled to reboot in schedule%."
                    .replace("schedule%", Converter.millisToTime(
                            (MeleeServerManager.getReboot().getAddedAt() + MeleeServerManager.getReboot().getDelay())
                                    - System.currentTimeMillis())));
        } else {
            Message.staff(event.getPlayer(), "melee.admin",
                    "<red>Instance was invalidated to reboot time% ago."
                            .replace("time%", Converter.millisToTime(System.currentTimeMillis() - MeleeServerManager.getReboot().getRemovedAt())));
        }

        Message.staff(event.getPlayer(), "melee.admin",
                "<gray>Instance has been online for time%."
                        .replace("time%", Converter.millisToTime(System.currentTimeMillis() - Melee.getInstance().getBooting())));
    }

    @EventHandler(
            priority = EventPriority.HIGHEST,
            ignoreCancelled = true
    )
    public void onPlayerQuit(PlayerQuitEvent event) {
        event.setQuitMessage(null);
        Hunter hunter = Hunter.getHunter(event.getPlayer().getUniqueId());

        hunter.setPlaytime(hunter.getPlaytime() + (System.currentTimeMillis() - hunter.getLastSeen()));
        hunter.setLastSeen(System.currentTimeMillis());
        hunter.setLoaded(false);
        hunter.expire();
        hunter.saveMongo();
        hunter.cancelSchedules();
    }

}
