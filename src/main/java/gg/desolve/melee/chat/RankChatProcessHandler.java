package gg.desolve.melee.chat;

import gg.desolve.melee.Melee;
import gg.desolve.melee.inventory.rank.RankModifyInheritsInventory;
import gg.desolve.melee.inventory.rank.RankModifyInventory;
import gg.desolve.melee.inventory.rank.RankModifyPermissionsInventory;
import gg.desolve.melee.profile.Profile;
import gg.desolve.melee.rank.Rank;
import gg.desolve.mithril.relevance.Message;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class RankChatProcessHandler {

    private final Profile profile;
    private final Rank rank;
    private final String process;

    public RankChatProcessHandler(Profile profile, Rank rank, String process) {
        this.profile = profile;
        this.rank = rank;
        this.process = process;
    }

    public void process(Player player, String message) {
        if (message.equalsIgnoreCase("cancel")) {
            Message.send(player, "<red>Modification process cancelled.");
            return;
        }

        switch (process) {
            case "display":
                rank.setDisplay(message);
                break;
            case "priority":
                try {
                    int priority = Integer.parseInt(message);
                    rank.setPriority(priority);
                } catch (NumberFormatException ex) {
                    Message.send(player, "<red>Invalid number entered for priority.");
                    return;
                }
                break;
            case "prefix":
                rank.setPrefix(message);
                break;
            case "color":
                if (message.contains(" ") && (!message.contains("<") || !message.contains(">"))) {
                    Message.send(player, "<red>Color must be a valid color format.");
                    return;
                }
                rank.setColor(message);
                break;
            case "grantable":
                rank.setGrantable(Boolean.parseBoolean(message));
                break;
            case "visible":
                rank.setVisible(Boolean.parseBoolean(message));
                break;
            case "permission":
                if (message.contains(" ")) {
                    Message.send(player, "<red>Permission cannot contain spaces.");
                    return;
                }

                List<String> permissions = new ArrayList<>(rank.getPermissions());
                if (permissions.contains(message.toLowerCase())) {
                    Message.send(player, "<red>Permission is present.");
                    return;
                }

                permissions.add(message.toLowerCase());
                rank.setPermissions(permissions);
                break;
            case "inherit":
                Rank inherit = Melee.getInstance().getRankManager().retrieve(message);
                if (inherit == null || inherit.getName().equalsIgnoreCase(rank.getName())) {
                    Message.send(player, "<red>Invalid inheritance rank.");
                    return;
                }

                List<String> inherits = new ArrayList<>(rank.getInherits());
                if (inherits.contains(inherit.getName().toLowerCase())) {
                    Message.send(player, inherit.getDisplayColored() + " <red>rank is present.");
                    return;
                }

                if (Melee.getInstance().getRankManager().compare(inherit, rank)) {
                    Message.send(player, inherit.getDisplayColored() + " <red>rank is higher than " + rank.getDisplayColored() + " <red>rank.");
                    return;
                }

                inherits.add(inherit.getName());
                rank.setInherits(inherits);
                break;
            default:
                Message.send(player, "<red>Unknown modification type.");
                return;
        }

        rank.save();
        Message.send(player, "<green>Updated " + rank.getNameColored() + " <green>rank " + process + ".");

        switch (process) {
            case "permission":
                RankModifyPermissionsInventory.getInventory(profile, rank).open(player);
                break;
            case "inherit":
                RankModifyInheritsInventory.getInventory(profile, rank).open(player);
                break;
            default:
                RankModifyInventory.getInventory(profile, rank).open(player);
                break;
        }
    }
}
