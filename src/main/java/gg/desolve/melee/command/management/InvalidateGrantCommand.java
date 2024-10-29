package gg.desolve.melee.command.management;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import gg.desolve.melee.common.Message;
import gg.desolve.melee.player.grant.Grant;
import gg.desolve.melee.player.grant.GrantType;
import gg.desolve.melee.player.profile.Hunter;
import gg.desolve.melee.player.rank.Rank;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandAlias("invalidategrant")
public class InvalidateGrantCommand extends BaseCommand {

    @Default
    @CommandCompletion("@players @ranks @reasons")
    @CommandPermission("melee.command.invalidategrant")
    @Syntax("<player> <rank> [reason]")
    @Description("Manually invalidate grant from a player")
    public static void execute(CommandSender sender, Hunter hunter, Rank rank, @Optional @Default("Demoted") String reason) {
        if (rank.isBaseline()) {
            Message.send(sender, "&cYou cannot invalidate the default rank.");
            return;
        }

        if (sender instanceof Player) {
            Hunter granter = Hunter.getHunter(((Player) sender).getUniqueId());

            if (!granter.hasPermission("melee.*") && !rank.isGrantable()) {
                Message.send(sender, "&cYou cannot invalidate this rank.");
                return;
            }

            if (!granter.hasPermission("melee.*") && Rank.rankIsHigherThanRank(rank, granter.getGrant().getRank())) {
                Message.send(sender, "&cYou cannot invalidate ranks higher than yours.");
                return;
            }
        }

        Grant grant = hunter.hasGrant(rank);

        if (grant == null) {
            Message.send(sender, rank.getDisplayColored() + " &crank is not present for " + hunter.getUsernameColored() + ".");
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
        hunter.refreshPermissions();
        hunter.save();

        Player player = Bukkit.getPlayer(hunter.getUuid());

        Message.send(sender,
                "&aRemoved rank% &arank from player% &afor &7reason%."
                        .replace("rank%", rank.getDisplayColored())
                        .replace("player%", hunter.getUsernameColored())
                        .replace("reason%", reason)
        );

        if (player != null && (sender != player))
            Message.send(player,
                    "rank% &arank has been removed &afor &7reason%."
                            .replace("rank%", rank.getDisplayColored())
                            .replace("reason%", reason)
            );

    }
}
