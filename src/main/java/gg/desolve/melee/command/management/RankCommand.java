package gg.desolve.melee.command.management;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandHelp;
import co.aikar.commands.annotation.*;
import gg.desolve.melee.common.Message;
import gg.desolve.melee.player.profile.Hunter;
import gg.desolve.melee.player.rank.Rank;
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
        Message.send(sender, "<newline><aqua>Loaded Ranks <gray>(" + Rank.getRanks().size() + "<gray>)");
        Rank.getSortedRanks().forEach(rank ->
                Message.send(sender,
                        (rank.isVisible() ? "<white>" : "<white>*")
                                + rank.getNameColored()
                                + " <white>(Priority: " + rank.getPriority() + ")"
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
                "<newline><aqua>Rank Information <gray>(" + rank.getNameColored() + "<gray>)"
                        + "<newline><white>Priority: <aqua>" + rank.getPriority()
                        + "<newline><white>Prefix: " + rank.getPrefix() + "You <gray>(" + rank.getColor() + "this<gray>)"
                        + "<newline><white>Display Name: " + rank.getDisplayColored()
                        + "<newline><white>Visible: " + (rank.isVisible() ? "<green>True" : "<red>False")
                        + "<newline><white>Grantable: " + (rank.isGrantable() ? "<green>True" : "<red>False")
                        + "<newline><white>Inherits (" + inherits.size() + "): <white>" + (inherits.isEmpty() ? "<red>None" : String.join("<white>, <white>", inherits))
                        + "<newline><white>Permissions (" + permissions.size() + "): <white>" + (permissions.isEmpty() ? "<red>None" : String.join("<white>, <white>", permissions))
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

        Message.send(sender, "<green>You've changed " + rank.getNameColored() + " <green>rank priority from <yellow>" + priorityBefore + " <green>to <yellow>" + priority + ".");
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

        Message.send(sender, "<green>You've changed " + rank.getNameColored() + " <green>rank prefix from <white>" + prefixBefore + " <green>to <white>" + prefix + ".");
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

        Message.send(sender, "<green>You've changed " + rank.getNameColored() + " <green>rank prefix from <white>" + displayBefore + " <green>to <white>" + rank.getDisplayColored() + ".");
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

        Message.send(sender, "<green>You've changed " + rank.getNameColored() + " <green>rank color from <white>" + colorBefore + "this <green>to <white>" + rank.getColor() + "this.");
    }

    @Subcommand("grantable")
    @CommandCompletion("@ranks true|false")
    @CommandPermission("melee.command.rank|melee.command.rank.grantable")
    @Syntax("<rank> <true|false>")
    @Description("Changes grantability of a rank")
    public static void onGrantable(CommandSender sender, Rank rank, boolean option) {
        if (rank.isBaseline() || (rank.getPermissions().contains("melee.*") && !sender.hasPermission("melee.*"))) {
            Message.send(sender, "<red>You cannot change this rank.");
            return;
        }

        rank.setGrantable(option);
        rank.save();

        Message.send(sender, "<green>You've changed " + rank.getNameColored() + " <green>rank grantability to <yellow>" + (option ? "true" : "false") + ".");
    }

    @Subcommand("visible")
    @CommandCompletion("@ranks true|false")
    @CommandPermission("melee.command.rank|melee.command.rank.visible")
    @Syntax("<rank> <true|false>")
    @Description("Changes visibility of a rank")
    public static void onVisible(CommandSender sender, Rank rank, boolean option) {
        rank.setVisible(option);
        rank.save();

        Message.send(sender, "<green>You've changed " + rank.getNameColored() + " <green>rank visibility to <yellow>" + (option ? "true" : "false") + ".");

        long updated = Bukkit.getOnlinePlayers().stream()
                .map(player -> Hunter.getHunter(player.getUniqueId()))
                .filter(profile -> profile.hasGrant(rank) != null)
                .peek(Hunter::refreshGrant)
                .count();

        Message.send(sender, "<green>Updated <yellow>" + updated + " <green>players visibility.");
    }

    @Subcommand("permission add")
    @CommandCompletion("@ranks")
    @CommandPermission("melee.command.rank|melee.command.rank.permission")
    @Syntax("<rank> <permission>")
    @Description("Changes permission of a rank")
    public static void onPermissionAdd(CommandSender sender, Rank rank, @Single String permission) {
        if (permission.equalsIgnoreCase("melee.*") && !sender.hasPermission("melee.*")) {
            Message.send(sender, "<red>You cannot change this rank.");
            return;
        }

        if (rank.getPermissions().contains(permission)) {
            Message.send(sender, "<yellow>" + permission + " <red>is already present.");
            return;
        }

        rank.getPermissions().add(permission);
        rank.save();

        Message.send(sender, "<green>You've changed " + rank.getDisplayColored() + " <green>rank by adding <yellow>" + permission + " <green>permission.");
    }

    @Subcommand("permission remove")
    @CommandCompletion("@ranks")
    @CommandPermission("melee.command.rank|melee.command.rank.permission")
    @Syntax("<rank> <permission>")
    @Description("Changes permission of a rank")
    public static void onPermissionRemove(CommandSender sender, Rank rank, @Single String permission) {
        if (permission.equalsIgnoreCase("melee.*") && !sender.hasPermission("melee.*")) {
            Message.send(sender, "<red>You cannot change this rank.");
            return;
        }

        if (!rank.getPermissions().contains(permission)) {
            Message.send(sender, "<yellow>" + permission + " <red>is not present.");
            return;
        }

        rank.getPermissions().remove(permission);
        rank.save();

        Message.send(sender, "<green>You've changed " + rank.getDisplayColored() + " <green>rank by removing <yellow>" + permission + " <green>permission.");
    }

    @Subcommand("inherit add")
    @CommandCompletion("@ranks @ranks")
    @CommandPermission("melee.command.rank|melee.command.rank.inherit")
    @Syntax("<rank> <inherit-rank>")
    @Description("Changes inherits of a rank")
    public static void onInheritAdd(CommandSender sender, Rank rank, Rank inheritRank) {
        if (inheritRank.getPermissions().contains("melee.*") && !sender.hasPermission("melee.*")) {
            Message.send(sender, "<red>You cannot change this rank.");
            return;
        }

        if (rank.equals(inheritRank)) {
            Message.send(sender, "<red>You are not allowed to add the same rank to it self.");
            return;
        }

        if (rank.getInherits().contains(inheritRank.getName())) {
            Message.send(sender, inheritRank.getNameColored() + " <red>is already present.");
            return;
        }

        rank.getInherits().add(inheritRank.getName());
        rank.save();

        Message.send(sender, "<green>You've changed " + rank.getDisplayColored() + " <green>rank by adding " + inheritRank.getDisplayColored() + " <green>rank.");
    }

    @Subcommand("inherit remove")
    @CommandCompletion("@ranks @ranks")
    @CommandPermission("melee.command.rank|melee.command.rank.inherit")
    @Syntax("<rank> <inherit-rank>")
    @Description("Changes inherits of a rank")
    public static void onInheritRemove(CommandSender sender, Rank rank, Rank inheritRank) {
        if (inheritRank.getPermissions().contains("melee.*") && !sender.hasPermission("melee.*")) {
            Message.send(sender, "<red>You cannot change this rank.");
            return;
        }

        if (rank.equals(inheritRank)) {
            Message.send(sender, "<red>You are not allowed to remove the same rank from it self.");
            return;
        }

        if (!rank.getInherits().contains(inheritRank.getName())) {
            Message.send(sender, inheritRank.getNameColored() + " <red>is not present.");
            return;
        }

        rank.getInherits().remove(inheritRank.getName());
        rank.save();

        Message.send(sender, "<green>You've changed " + rank.getDisplayColored() + " <green>rank by removing " + inheritRank.getDisplayColored() + " <green>rank.");
    }

}
