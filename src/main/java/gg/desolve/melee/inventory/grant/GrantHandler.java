package gg.desolve.melee.inventory.grant;

import gg.desolve.melee.profile.Profile;
import gg.desolve.melee.rank.Rank;
import gg.desolve.mithril.relevance.Converter;
import gg.desolve.mithril.relevance.Duration;
import gg.desolve.mithril.relevance.Message;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class GrantHandler {

    private final Profile profile;
    private final Rank rank;
    private final String duration;
    private final String scope;
    private final String process;

    public GrantHandler(Profile profile, Rank rank, String duration, String scope, String process) {
        this.profile = profile;
        this.rank = rank;
        this.duration = duration;
        this.scope = scope;
        this.process = process;
    }

    public void process(Player player, String message) {
        switch (process) {
            case "duration":
                Duration timing = Duration.duration(message);

                if (timing.duration() < 0) {
                    Message.send(player, "<red>Invalid duration entered.");
                    return;
                }
                GrantScopeInventory.getInventory(profile, rank, message).open(player);
                break;
            case "scope":
                GrantReasonInventory.getInventory(profile, rank, duration, message).open(player);
                break;
            case "reason":
                GrantConfirmInventory.getInventory(profile, rank, duration, scope, message).open(player);
                break;
        }

    }
}
