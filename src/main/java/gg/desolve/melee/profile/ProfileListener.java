package gg.desolve.melee.profile;

import gg.desolve.melee.Melee;
import gg.desolve.melee.socket.Socket;
import gg.desolve.mithril.Mithril;
import gg.desolve.mithril.relevance.Message;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class ProfileListener implements Listener {

    @EventHandler(
            priority = EventPriority.HIGHEST,
            ignoreCancelled = true
    )
    public void onAsyncPlayerPreLogin(AsyncPlayerPreLoginEvent event) {
        Player player = Bukkit.getPlayer(event.getUniqueId());

        if (player != null && player.isOnline()) {
            event.setLoginResult(AsyncPlayerPreLoginEvent.Result.KICK_OTHER);
            event.setKickMessage(Message.translate("<red>You attempted to login shortly after disconnecting<newline>Please try again in a few seconds"));
            Melee.getInstance().getServer().getScheduler().runTask(Melee.getInstance(), () ->
                    player.kickPlayer(Message.translate("<red>Duplicate login :?")));
            return;
        }

        Profile profile;

        try {
            profile = Melee.getInstance().getProfileManager().retrieve(event.getUniqueId());
            profile.setUsername(event.getName());

            if (profile.getFirstSeen() == null)
                profile.setFirstSeen(System.currentTimeMillis());

            profile.setLastSeen(System.currentTimeMillis());

            if (profile.getAddress() == null || !profile.getAddress().equalsIgnoreCase(event.getAddress().getHostAddress()))
                profile.setAddress(event.getAddress().getHostAddress());

            profile.getSockets().stream()
                    .filter(s -> s.getAddress().equalsIgnoreCase(event.getAddress().getHostAddress()))
                    .findFirst()
                    .ifPresentOrElse(
                            s -> {},
                            () -> profile.getSockets().add(new Socket(
                                    event.getAddress().getHostAddress(),
                                    0,
                                    System.currentTimeMillis(),
                                    System.currentTimeMillis()))
                    );

            profile.setServer(Mithril.getInstance().getInstanceManager().getInstance().getId());

            profile.setLoaded(true);
            profile.save();
        } catch (Exception e) {
            Melee.getInstance().getLogger().warning("There was a problem loading " + event.getName() + "'s profile.");
            event.setKickMessage(Message.translate("<red>There was a problem loading your account<newline>Please contact an administrator"));
            event.setLoginResult(AsyncPlayerPreLoginEvent.Result.KICK_OTHER);
            e.printStackTrace();
        }
    }

    @EventHandler(
            priority = EventPriority.HIGHEST
    )
    public void onPlayerJoin(PlayerJoinEvent event) {
        Profile profile = Melee.getInstance().getProfileManager().retrieve(event.getPlayer().getUniqueId());

        if (!profile.isLoaded()) {
            event.getPlayer().kickPlayer(Message.translate("<red>You attempted to login while booting<newline>Please try again in a few seconds"));
            return;
        }

        profile.setLogins(profile.getLogins() + 1);
        profile.getSockets().stream()
                .filter(s -> s.getAddress().equalsIgnoreCase(profile.getAddress()))
                .findFirst()
                .ifPresent(s -> s.setLogins(s.getLogins() + 1));
        profile.setServer(Mithril.getInstance().getInstanceManager().getInstance().getId());

        profile.save();
    }

    @EventHandler(
            priority = EventPriority.HIGHEST,
            ignoreCancelled = true
    )
    public void onPlayerQuit(PlayerQuitEvent event) {
        Profile profile = Melee.getInstance().getProfileManager().retrieve(event.getPlayer().getUniqueId());

        profile.setLastSeen(System.currentTimeMillis());
        profile.setServer(Mithril.getInstance().getInstanceManager().getInstance().getId());
        profile.setLoaded(false);
        profile.removeSchedules();
        profile.save();

        Melee.getInstance().getProfileManager().getRecords().remove(profile.getUuid());
    }
}
