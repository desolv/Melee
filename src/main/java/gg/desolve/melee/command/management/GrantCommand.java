package gg.desolve.melee.command.management;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import gg.desolve.melee.Melee;
import gg.desolve.melee.grant.Grant;
import gg.desolve.melee.grant.GrantType;
import gg.desolve.melee.inventory.grant.GrantInventory;
import gg.desolve.melee.profile.Profile;
import gg.desolve.melee.profile.ProfileManager;
import gg.desolve.melee.rank.Rank;
import gg.desolve.mithril.Mithril;
import gg.desolve.mithril.config.Config;
import gg.desolve.mithril.relevance.Converter;
import gg.desolve.mithril.relevance.Duration;
import gg.desolve.mithril.relevance.Message;
import gg.desolve.mithril.relevance.Scope;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

@CommandAlias("grant")
public class GrantCommand extends BaseCommand {

    @Default
    @CommandCompletion("@players")
    @CommandPermission("melee.command.grant")
    @Syntax("(player)")
    @Description("Grant a rank to players")
    public static void execute(Player player, Profile profile) {
        GrantInventory.getInventory(profile).open(player);
    }

}
