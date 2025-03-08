package gg.desolve.melee.command.management;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import gg.desolve.melee.Melee;
import gg.desolve.melee.grant.Grant;
import gg.desolve.melee.grant.GrantType;
import gg.desolve.melee.profile.Profile;
import gg.desolve.melee.profile.ProfileManager;
import gg.desolve.melee.rank.Rank;
import gg.desolve.mithril.Mithril;
import gg.desolve.mithril.relevance.Converter;
import gg.desolve.mithril.relevance.Duration;
import gg.desolve.mithril.relevance.Message;
import gg.desolve.mithril.relevance.Scope;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

@CommandAlias("grantmanual")
public class GrantManualCommand extends BaseCommand {

    @Default
    @CommandCompletion("@players @ranks @durations @scopes @nothing")
    @CommandPermission("melee.command.grantmanual")
    @Syntax("(player) (rank) (duration) (scope) [reason]")
    @Description("Manually grant to players")
    public static void execute(CommandSender sender, Profile profile, Rank rank, Duration duration, @Optional @Default("global") Scope scope, @Optional @Default("Other") String reason) {
        ProfileManager profileManager = Melee.getInstance().getProfileManager();

        if ((sender instanceof Player)) {
            boolean cannotGrant = !rank.isGrantable();
            Profile granter = profileManager.retrieve(((Player) sender).getUniqueId());
            boolean isHigherRank = Melee.getInstance().getRankManager().compare(rank, granter.getGrant().getRank());

            if (cannotGrant || isHigherRank && !granter.hasPermission("melee.*")) {
                Message.send(sender, cannotGrant ?
                        "<red>You cannot grant this rank." :
                        "<red>You cannot grant ranks higher than yours.");
                return;
            }
        }

        if (profile.hasGrant(rank)) {
            Message.send(sender, rank.getDisplayColored() + " <red>rank is present for " + profile.getUsernameColored() + ".");
            return;
        }

        UUID addedBy = sender instanceof Player ? ((Player) sender).getUniqueId() : null;

        Grant grant = new Grant(
                Converter.generateId(),
                rank.getName(),
                addedBy,
                System.currentTimeMillis(),
                reason,
                Mithril.getInstance().getInstanceManager().getInstance().getName(),
                scope.getUnformatted(),
                duration.duration(),
                GrantType.ACTIVE
        );

        profile.getGrants().add(grant);
        profile.save();

        Message.send(sender, "<green>You've granted the rank% <green>rank to player% <green>on scope <light_purple>scope%."
                        .replace("rank%", rank.getDisplayColored())
                        .replace("player%", profile.getUsernameColored())
                        .replace("duration%", (grant.isPermanent() ? "forever" : Converter.time(duration.duration())))
                        .replace("scope%", scope.getFormat())
                        .replace("reason%", reason));

        String grantedMessage = "<green>You've been granted the rank% <green>rank <gray>for duration%."
                .replace("rank%", rank.getDisplayColored())
                .replace("duration%", (grant.isPermanent() ? "forever" : Converter.time(duration.duration())))
                .replace("scope%", scope.getFormat())
                .replace("reason%", reason);

        String broadcastMessage = "prefix% rank% <green>rank has been granted to player% <green>on scope <light_purple>scope%."
                .replace("player%", profile.getUsernameColored())
                .replace("rank%", rank.getDisplayColored())
                .replace("duration%", (grant.isPermanent() ? "forever" : Converter.time(duration.duration())))
                .replace("scope%", scope.getFormat())
                .replace("reason%", reason);

        String redisMessage = String.join("&%$",
                String.valueOf(profile.getUuid()),
                scope.getUnformatted(),
                grantedMessage,
                broadcastMessage
        );

        Mithril.getInstance().getRedisManager().publish("Grant", redisMessage);
    }

}
