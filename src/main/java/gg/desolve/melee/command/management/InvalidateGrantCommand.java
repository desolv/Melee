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
import gg.desolve.mithril.relevance.Message;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandAlias("invalidategrant")
public class InvalidateGrantCommand extends BaseCommand {

    @Default
    @CommandCompletion("@players @ranks @nothing")
    @CommandPermission("melee.command.invalidategrant")
    @Syntax("(player) (rank) [reason]")
    @Description("Manually invalidate grants from players")
    public static void execute(CommandSender sender, Profile profile, Rank rank, @Optional @Default("Other") String reason) {
        ProfileManager profileManager = Melee.getInstance().getProfileManager();

        if ((sender instanceof Player) && !profile.hasPermission("melee.*")) {
            boolean cannotGrant = !rank.isGrantable();
            Profile granter = profileManager.retrieve(((Player) sender).getUniqueId());
            boolean isHigherRank = Melee.getInstance().getRankManager().compare(rank, granter.getGrant().getRank());

            if (cannotGrant || isHigherRank) {
                Message.send(sender, cannotGrant ?
                        "<red>You cannot invalidate this rank." :
                        "<red>You cannot invalidate ranks higher than yours.");
                return;
            }
        }

        if (!profile.hasGrant(rank)) {
            Message.send(sender, rank.getDisplayColored() + " <red>rank is not present for " + profile.getUsernameColored() + ".");
            return;
        }

        Grant grant = profile.getActiveGrant(rank);

        grant.setRemovedBy(sender instanceof Player ? ((Player) sender).getUniqueId() : null);
        grant.setRemovedAt(System.currentTimeMillis());
        grant.setRemovedOrigin(Mithril.getInstance().getInstanceManager().getInstance().getName());
        grant.setType(GrantType.REMOVED);

        profile.getGrants().removeIf(g -> g.getId().equals(grant.getId()));
        profile.getGrants().add(grant);

        profile.save();

        Message.send(sender, "<green>You've invalidated the rank% <green>rank from player%."
                .replace("rank%", rank.getDisplayColored())
                .replace("player%", profile.getUsernameColored())
                .replace("reason%", reason));

        String invalidateMessage = "<green>rank% <green>rank <green>has been removed."
                .replace("rank%", rank.getDisplayColored())
                .replace("reason%", reason);

        String broadcastMessage = "prefix% rank% <green>rank has been invalidated from player%."
                .replace("player%", profile.getUsernameColored())
                .replace("rank%", rank.getDisplayColored())
                .replace("reason%", reason);

        String redisMessage = String.join("&%$",
                String.valueOf(profile.getUuid()),
                invalidateMessage,
                broadcastMessage
        );

        Mithril.getInstance().getRedisManager().publish("InvalidateGrant", redisMessage);
    }
}
