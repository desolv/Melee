package gg.desolve.melee.command.management;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import gg.desolve.melee.common.Converter;
import gg.desolve.melee.common.Duration;
import gg.desolve.melee.common.Message;
import gg.desolve.melee.player.grant.Grant;
import gg.desolve.melee.player.grant.GrantType;
import gg.desolve.melee.player.profile.Profile;
import gg.desolve.melee.rank.Rank;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

@CommandAlias("grantmanual")
public class GrantManualCommand extends BaseCommand {

    @Default
    @CommandCompletion("@players @ranks @durations @reasons")
    @CommandPermission("melee.command.grantmanual")
    @Syntax("<player> <rank> <duration> [reason]")
    @Description("Manually grant to a player")
    public static void execute(CommandSender sender, Profile profile, Rank rank, Duration duration, @Optional @Default("Promoted") String reason) {
        if (rank.isBaseline()) {
            Message.send(sender, "&cYou cannot grant the default rank.");
            return;
        }

        if (sender instanceof Player) {
            Profile granter = Profile.getProfile(((Player) sender).getUniqueId());

            if (!granter.hasPermission("melee.*") && !rank.isGrantable()) {
                Message.send(sender, "&cYou cannot grant this rank.");
                return;
            }

            if (!granter.hasPermission("melee.*") && Rank.rankIsHigherThanRank(rank, granter.getPriorityGrant().getRank())) {
                Message.send(sender, "&cYou cannot grant ranks higher than yours.");
                return;
            }
        }

        if (profile.hasGrant(rank) != null) {
            Message.send(sender, rank.getDisplayColored() + " &crank is present for " + profile.getUsernameColored() + ".");
            return;
        }

        long durationValue = duration.getValue();
        UUID addedBy = sender instanceof Player ? ((Player) sender).getUniqueId() : null;

        Grant grant = new Grant(
                Converter.generateId(),
                rank,
                addedBy,
                System.currentTimeMillis(),
                reason,
                Bukkit.getServerName(),
                durationValue,
                GrantType.ACTIVE
        );

        profile.getGrants().add(grant);
        profile.refreshGrant();
        profile.refreshPermissions();
        profile.save();

        Player player = Bukkit.getPlayer(profile.getUuid());

        if (durationValue != Integer.MAX_VALUE && Converter.millisToHours(durationValue) <= 48 && (player != null && player.isOnline())) {
            Runnable runnable = () -> {
                if (profile.getUsername() != null)
                    profile.evaluateGrants();
            };

            profile.addSchedule(
                    grant.getId() + rank.getName(),
                    runnable,
                    (durationValue + 1000)
            );
        }

        Message.send(sender,
                "&aGranted rank% &arank to player% &afor &7duration%."
                        .replace("rank%", rank.getDisplayColored())
                        .replace("player%", profile.getUsernameColored())
                        .replace("duration%",
                                grant.isPermanent() ?
                                        "forever" :
                                        durationValue == Integer.MAX_VALUE ?
                                                "forever" :
                                                Converter.millisToTime(durationValue)
                        )
        );

        if (player != null && (sender != player))
            Message.send(player,
                    "&aYou've been granted rank% &arank &afor &7duration%."
                            .replace("rank%", rank.getDisplayColored())
                            .replace("duration%",
                                    grant.isPermanent() ?
                                            "forever" :
                                            durationValue == Integer.MAX_VALUE ?
                                                    "forever" :
                                                    Converter.millisToTime(durationValue)
                            )
            );

    }
}
