package gg.desolve.melee.command.management;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandHelp;
import co.aikar.commands.annotation.*;
import gg.desolve.melee.grant.GrantType;
import gg.desolve.melee.profile.Profile;
import gg.desolve.mithril.relevance.Converter;
import gg.desolve.mithril.relevance.Message;
import org.bukkit.command.CommandSender;

@CommandAlias("user")
public class UserCommand extends BaseCommand {

    @HelpCommand
    @CommandPermission("melee.command.user|melee.command.user.help")
    @Syntax("[page]")
    public static void onHelp(CommandHelp help) {
        help.setPerPage(6);
        help.showHelp();
    }

    @Subcommand("simple")
    @CommandCompletion("@players")
    @CommandPermission("melee.command.user|melee.command.user.simple")
    @Syntax("(player)")
    @Description("Retrieve user information")
    public static void onSimple(CommandSender sender, Profile profile) {
        Message.send(sender,
                "<newline>" + profile.getUsernameColored() + "<bold><aqua>'s User information</bold>" +
                        "<newline><white>UUID: <dark_gray>" + profile.getUuid() +
                        "<newline><white>Logins: <aqua>" + profile.getLogins() +
                        "<newline><white>First Seen: <aqua>" + Converter.date(profile.getFirstSeen()) +
                        "<newline><white>Last Seen: <aqua>" + Converter.time(System.currentTimeMillis() - profile.getLastSeen()) + " ago" +
                        "<newline><white>Grants: <aqua>" + String.join("<white>, <white>", profile.getGrants().stream()
                                .filter(grant -> grant.getType() == GrantType.ACTIVE)
                                .map(grant -> (!grant.isPermanent() ? "<white>*" : "") + grant.getRank().getNameColored()).toList()) +
                        "<newline><white>Sockets: <aqua>" + profile.getSockets().size());
    }


}
