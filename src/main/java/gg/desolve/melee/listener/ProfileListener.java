package gg.desolve.melee.listener;

import gg.desolve.melee.Melee;
import gg.desolve.melee.common.Message;
import gg.desolve.melee.player.profile.Marker;
import gg.desolve.melee.player.profile.Profile;
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
                            "&cYou attempted to login shortly after disconnecting"
                                    + "\nPlease try again in a few seconds"
                    ));
            instance.getServer().getScheduler().runTask(
                    instance, () -> player.kickPlayer(Message.translate("&cDuplicate login :?")));
            return;
        }

        Profile profile = null;

        try {
            profile = new Profile(event.getUniqueId(), event.getName());
            profile.setUsername(event.getName());

            if (profile.getFirstSeen() == null)
                profile.setFirstSeen(System.currentTimeMillis());

            profile.setLastSeen(System.currentTimeMillis());

            if (profile.getAddress() == null || !profile.getAddress().equalsIgnoreCase(event.getAddress().getHostAddress()))
                profile.setAddress(event.getAddress().getHostAddress());

            Optional<Marker> markerMissing = profile.getMarkers().stream()
                    .filter(address -> address.getAddress().equalsIgnoreCase(event.getAddress().getHostAddress()))
                    .findFirst();

            if (!markerMissing.isPresent()) {
                profile.getMarkers().add(
                        new Marker(
                                event.getAddress().getHostAddress(),
                                0,
                                System.currentTimeMillis(),
                                System.currentTimeMillis()
                        ));
            }

            profile.setLoaded(true);
        } catch (Exception e) {
            instance.getLogger().warning("There was a problem loading " + event.getName() + "'s profile.");
            e.printStackTrace();
        }

        if (profile == null || !profile.isLoaded()) {
            event.setKickMessage(
                    Message.translate(
                            "&cThere was a problem loading your account"
                                    + "\nPlease contact an administrator"
                    ));
            event.setLoginResult(AsyncPlayerPreLoginEvent.Result.KICK_OTHER);
            return;
        }

        Profile.getProfiles().put(profile.getUuid(), profile);
        profile.save();
    }

    @EventHandler(
            priority = EventPriority.HIGHEST
    )
    public void onPlayerJoin(PlayerJoinEvent event) {
        event.setJoinMessage(null);
        Profile profile = Profile.getProfiles().get(event.getPlayer().getUniqueId());

        if (profile == null || !profile.isLoaded()) {
            event.getPlayer().kickPlayer(
                    Message.translate(
                            "&cYou attempted to login while booting"
                                    + "\nPlease try again in a few seconds"
                    ));
            return;
        }

        profile.setLogins(profile.getLogins() + 1);
        profile.getMarkers().stream()
                .filter(marker -> marker.getAddress().equalsIgnoreCase(profile.getAddress()))
                .findFirst()
                .ifPresent(marker -> marker.setLogins(marker.getLogins() + 1));
    }

    @EventHandler(
            priority = EventPriority.HIGHEST,
            ignoreCancelled = true
    )
    public void onPlayerQuit(PlayerQuitEvent event) {
        event.setQuitMessage(null);
        Profile profile = Profile.getProfiles().get(event.getPlayer().getUniqueId());

        if (profile != null) {
            profile.setLastSeen(System.currentTimeMillis());
            profile.save();
            Profile.getProfiles().remove(event.getPlayer().getUniqueId());
        }
    }

}
