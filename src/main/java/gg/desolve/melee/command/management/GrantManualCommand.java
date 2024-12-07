package gg.desolve.melee.command.management;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import gg.desolve.melee.Melee;
import gg.desolve.melee.common.Converter;
import gg.desolve.melee.common.Duration;
import gg.desolve.melee.common.Message;
import gg.desolve.melee.configuration.MeleeConfigManager;
import gg.desolve.melee.player.grant.Grant;
import gg.desolve.melee.player.grant.GrantSubscriber;
import gg.desolve.melee.player.grant.GrantType;
import gg.desolve.melee.player.profile.Hunter;
import gg.desolve.melee.player.rank.Rank;
import gg.desolve.melee.server.Scope;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import redis.clients.jedis.Jedis;

import java.util.UUID;

@CommandAlias("grantmanual")
public class GrantManualCommand extends BaseCommand {

    @Default
    @CommandCompletion("@players @ranks @scopes @durations @reasons")
    @CommandPermission("melee.command.grantmanual")
    @Syntax("<player> <rank> <scope> <duration> [reason]")
    @Description("Manually grant to a player")
    public static void execute(CommandSender sender, Hunter hunter, Rank rank, Scope scope, Duration duration, @Optional @Default("Other") String reason) {
        if (rank.isBaseline()) {
            Message.send(sender, "<red>You cannot grant the default rank.");
            return;
        }

        if (sender instanceof Player) {
            Hunter granter = Hunter.getHunter(((Player) sender).getUniqueId());

            if (!granter.hasPermission("melee.*") && !rank.isGrantable()) {
                Message.send(sender, "<red>You cannot grant this rank.");
                return;
            }

            if (!granter.hasPermission("melee.*") && Rank.rankIsHigherThanRank(rank, granter.getGrant().getRank())) {
                Message.send(sender, "<red>You cannot grant ranks higher than yours.");
                return;
            }
        }

        if (hunter.hasGrant(rank) != null) {
            Message.send(sender, rank.getDisplayColored() + " <red>rank is present for " + hunter.getUsernameColored() + ".");
            return;
        }

        long durationValue = duration.getValue();
        String scopeValue = scope.getValue();
        UUID addedBy = sender instanceof Player ? ((Player) sender).getUniqueId() : null;

        Grant grant = new Grant(
                Converter.grantId(hunter),
                rank.getName(),
                addedBy,
                System.currentTimeMillis(),
                reason,
                MeleeConfigManager.lang.getString("server_name"),
                scopeValue,
                durationValue,
                GrantType.ACTIVE
        );

        hunter.getGrants().add(grant);
        hunter.refreshGrant();

        Player player = Bukkit.getPlayer(hunter.getUuid());

        if (player != null) hunter.refreshPermissions();
        hunter.save();

        Message.send(sender,
                "<green>You've granted the rank% <green>rank to player% <green>lasting <yellow>duration% <green>on scope <light_purple>scope%."
                        .replace("rank%", rank.getDisplayColored())
                        .replace("player%", hunter.getUsernameColored())
                        .replace("duration%",
                                (grant.isPermanent() ?
                                        "forever" :
                                        durationValue == Integer.MAX_VALUE ?
                                                "forever" :
                                                Converter.millisToTime(durationValue)))
                        .replace("scope%", scopeValue)
                        .replace("reason%", reason)
        );

        String message = "<green>You've been granted rank% <green>rank <green>lasting <yellow>duration% <green>on scope <light_purple>scope%."
                .replace("rank%", rank.getDisplayColored())
                .replace("duration%",
                        grant.isPermanent() ?
                                "forever" :
                                durationValue == Integer.MAX_VALUE ?
                                        "forever" :
                                        Converter.millisToTime(durationValue)
                )
                .replace("scope%", scopeValue);

        String redisMessage = String.join("&%$",
                hunter.getUuid().toString(),
                sender.getName(),
                String.valueOf(durationValue),
                grant.getId(),
                rank.getName(),
                scopeValue,
                message
        );

        try (Jedis jedis = Melee.getInstance().getRedisManager().getConnection()) {
            jedis.publish(GrantSubscriber.update, redisMessage);
        }

    }
}
