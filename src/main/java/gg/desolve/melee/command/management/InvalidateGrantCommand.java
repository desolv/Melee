package gg.desolve.melee.command.management;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import gg.desolve.melee.Melee;
import gg.desolve.melee.common.Message;
import gg.desolve.melee.player.grant.Grant;
import gg.desolve.melee.player.grant.GrantSubscriber;
import gg.desolve.melee.player.grant.GrantType;
import gg.desolve.melee.player.grant.InvalidateGrantSubscriber;
import gg.desolve.melee.player.profile.Hunter;
import gg.desolve.melee.player.rank.Rank;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import redis.clients.jedis.Jedis;

@CommandAlias("invalidategrant")
public class InvalidateGrantCommand extends BaseCommand {

    @Default
    @CommandCompletion("@players @ranks @reasons")
    @CommandPermission("melee.command.invalidategrant")
    @Syntax("<player> <rank> [reason]")
    @Description("Manually invalidate grant from a player")
    public static void execute(CommandSender sender, Hunter hunter, Rank rank, @Optional @Default("Other") String reason) {
        if (rank.isBaseline()) {
            Message.send(sender, "<red>You cannot invalidate the default rank.");
            return;
        }

        if (sender instanceof Player) {
            Hunter granter = Hunter.getHunter(((Player) sender).getUniqueId());

            if (!granter.hasPermission("melee.*") && !rank.isGrantable()) {
                Message.send(sender, "<red>You cannot invalidate this rank.");
                return;
            }

            if (!granter.hasPermission("melee.*") && Rank.rankIsHigherThanRank(rank, granter.getGrant().getRank())) {
                Message.send(sender, "<red>You cannot invalidate ranks higher than yours.");
                return;
            }
        }

        Grant grant = hunter.hasGrant(rank);

        if (grant == null) {
            Message.send(sender, rank.getDisplayColored() + " <red>rank is not present for " + hunter.getUsernameColored() + ".");
            return;
        }

        grant.setRemovedAt(System.currentTimeMillis());
        grant.setRemovedBy(sender instanceof Player ? ((Player) sender).getUniqueId() : null);
        grant.setRemovedReason(reason);
        grant.setRemovedOrigin(Bukkit.getServerName());
        grant.setType(GrantType.REMOVED);

        if (!grant.isPermanent())
            hunter.cancelSchedule(grant.getId() + grant.getRank().getName());

        hunter.refreshGrant();

        Player player = Bukkit.getPlayer(hunter.getUuid());

        if (player != null) hunter.refreshPermissions();
        hunter.save();


        Message.send(sender,
                "<green>Removed rank% <green>rank from player% <green>for <gray>reason%."
                        .replace("rank%", rank.getDisplayColored())
                        .replace("player%", hunter.getUsernameColored())
                        .replace("reason%", reason)
        );

        String message = "rank% <green>rank has been removed <green>for <gray>reason%."
                .replace("rank%", rank.getDisplayColored())
                .replace("reason%", reason);

        String redisMessage = String.join("&%$",
                hunter.getUsername(),
                sender.getName(),
                message
        );

        try (Jedis jedis = Melee.getInstance().getRedisManager().getConnection()) {
            jedis.publish(InvalidateGrantSubscriber.update, redisMessage);
        }

    }
}
