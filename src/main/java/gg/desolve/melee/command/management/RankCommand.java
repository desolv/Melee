package gg.desolve.melee.command.management;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandHelp;
import co.aikar.commands.annotation.*;
import gg.desolve.melee.common.Message;
import gg.desolve.melee.player.profile.Profile;
import gg.desolve.melee.rank.Rank;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@CommandAlias("rank")
public class RankCommand extends BaseCommand {

    @HelpCommand
    @CommandPermission("melee.command.rank|melee.command.rank.help")
    @Syntax("[page]")
    @Description("Shows list of commands")
    public static void onHelp(CommandHelp help) {
        help.setPerPage(6);
        help.showHelp();
    }

    @Subcommand("list")
    @CommandPermission("melee.command.rank|melee.command.rank.list")
    @Description("Shows list of all ranks")
    public static void onList(CommandSender sender) {
        Message.send(sender, "\n&bLOADED RANKS &7(" + Rank.getRanks().size() + "&7)");
        Rank.getSortedRanks().forEach(rank ->
                Message.send(sender,
                        (rank.isVisible() ? "&f" : "&f*")
                                + rank.getNameColored()
                                + " &f(Priority: " + rank.getPriority() + ")"
                ));
    }

    @Subcommand("info")
    @CommandCompletion("@ranks")
    @CommandPermission("melee.command.rank|melee.command.rank.info")
    @Syntax("<rank>")
    @Description("Shows information of a rank")
    public static void onInfo(CommandSender sender, Rank rank) {
        List<String> inherits = rank.getInherits()
                .stream()
                .map(inheritedRank -> Rank.getRanks().get(inheritedRank).getNameColored())
                .collect(Collectors.toList());

        List<String> permissions = new ArrayList<>(rank.getPermissions());

        Message.send(sender,
                "\n&bRANK INFORMATION &7(" + rank.getNameColored() + "&7)"
                        + "\n&fPriority: &b" + rank.getPriority()
                        + "\n&fPrefix: " + rank.getPrefix() + "You &7(" + rank.getColor() + "this&7)"
                        + "\n&fDisplay Name: " + rank.getDisplayColored()
                        + "\n&fVisible: " + (rank.isVisible() ? "&aTrue" : "&cFalse")
                        + "\n&fGrantable: " + (rank.isGrantable() ? "&aTrue" : "&cFalse")
                        + "\n&fInherits (" + inherits.size() + "): &f" + (inherits.isEmpty() ? "&cNone" : String.join("&f, &f", inherits))
                        + "\n&fPermissions (" + permissions.size() + "): &f" + (permissions.isEmpty() ? "&cNone" : String.join("&f, &f", permissions))
        );
    }

    @Subcommand("priority")
    @CommandCompletion("@ranks")
    @CommandPermission("melee.command.rank|melee.command.rank.priority")
    @Syntax("<rank> <priority>")
    @Description("Changes priority of a rank")
    public static void onPriority(CommandSender sender, Rank rank, int priority) {
        int priorityBefore = rank.getPriority();
        rank.setPriority(priority);
        rank.save();

        Message.send(sender, "&aYou've changed " + rank.getNameColored() + " &arank priority from &e" + priorityBefore + " &ato &e" + priority + ".");
    }

    @Subcommand("prefix")
    @CommandCompletion("@ranks")
    @CommandPermission("melee.command.rank|melee.command.rank.prefix")
    @Syntax("<rank> <prefix>")
    @Description("Changes prefix of a rank")
    public static void onPrefix(CommandSender sender, Rank rank, String prefix) {
        String prefixBefore = rank.getPrefix();
        rank.setPrefix(prefix);
        rank.save();

        Message.send(sender, "&aYou've changed " + rank.getNameColored() + " &arank prefix from &f" + prefixBefore + " &ato &f" + prefix + ".");
    }

    @Subcommand("display")
    @CommandCompletion("@ranks")
    @CommandPermission("melee.command.rank|melee.command.rank.display")
    @Syntax("<rank> <display>")
    @Description("Changes display name of a rank")
    public static void onDisplay(CommandSender sender, Rank rank, @Single String display) {
        String displayBefore = rank.getDisplayColored();
        rank.setDisplay(display);
        rank.save();

        Message.send(sender, "&aYou've changed " + rank.getNameColored() + " &arank prefix from &f" + displayBefore + " &ato &f" + rank.getDisplayColored() + ".");
    }

    @Subcommand("color")
    @CommandCompletion("@ranks @colors")
    @CommandPermission("melee.command.rank|melee.command.rank.color")
    @Syntax("<rank> <color>")
    @Description("Changes color of a rank")
    public static void onColor(CommandSender sender, Rank rank, @Single String color) {
        String colorBefore = rank.getColor();
        rank.setColor(color);
        rank.save();

        Message.send(sender, "&aYou've changed " + rank.getNameColored() + " &arank color from &f" + colorBefore + "this &ato &f" + rank.getColor() + "this.");
    }

    @Subcommand("grantable")
    @CommandCompletion("@ranks true|false")
    @CommandPermission("melee.command.rank|melee.command.rank.grantable")
    @Syntax("<rank> <true|false>")
    @Description("Changes grantability of a rank")
    public static void onGrantable(CommandSender sender, Rank rank, boolean option) {
        if (rank.isBaseline() || (rank.getPermissions().contains("melee.*") && !sender.hasPermission("melee.*"))) {
            Message.send(sender, "&cYou cannot change this rank.");
            return;
        }

        rank.setGrantable(option);
        rank.save();

        Message.send(sender, "&aYou've changed " + rank.getNameColored() + " &arank grantability to &e" + (option ? "true" : "false") + ".");
    }

    @Subcommand("visible")
    @CommandCompletion("@ranks true|false")
    @CommandPermission("melee.command.rank|melee.command.rank.visible")
    @Syntax("<rank> <true|false>")
    @Description("Changes visibility of a rank")
    public static void onVisible(CommandSender sender, Rank rank, boolean option) {
        rank.setVisible(option);
        rank.save();

        Message.send(sender, "&aYou've changed " + rank.getNameColored() + " &arank visibility to &e" + (option ? "true" : "false") + ".");

        long updated = Bukkit.getOnlinePlayers().stream()
                .map(player -> Profile.getProfile(player.getUniqueId()))
                .filter(profile -> profile.hasGrant(rank) != null)
                .peek(Profile::refreshGrant)
                .count();

        Message.send(sender, "&aUpdated &e" + updated + " &aplayers visibility.");
    }

}
