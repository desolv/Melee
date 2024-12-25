package gg.desolve.melee.command.moderation;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import gg.desolve.melee.Melee;
import gg.desolve.melee.common.Converter;
import gg.desolve.melee.common.Duration;
import gg.desolve.melee.common.Message;
import gg.desolve.melee.common.Reason;
import gg.desolve.melee.player.profile.Hunter;
import gg.desolve.melee.player.punishment.Punishment;
import gg.desolve.melee.player.punishment.PunishmentStyle;
import gg.desolve.melee.player.punishment.PunishmentSubscriber;
import gg.desolve.melee.player.punishment.PunishmentType;
import gg.desolve.melee.player.rank.Rank;
import gg.desolve.melee.server.BroadcastSubscriber;
import gg.desolve.melee.server.Scope;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import redis.clients.jedis.Jedis;

import java.util.UUID;

public class BanCommand extends BaseCommand {

    @CommandAlias("ban")
    @CommandCompletion("@players @durations @scopes @type")
    @CommandPermission("melee.command.ban")
    @Syntax("<player> <duration> <scope> <reason> [-s, -p]")
    @Description("Punish by banning a player")
    public static void execute(CommandSender sender, Hunter hunter, Duration duration, Scope scope, String reason) {
        if (sender instanceof Player) {
            Hunter granter = Hunter.getHunter(((Player) sender).getUniqueId());

            if (granter.getUuid().equals(hunter.getUuid())) {
                Message.send(sender, "<red>You cannot punish your self.");
                return;
            }

            if (!granter.hasPermission("melee.*") && Rank.rankIsHigherThanRank(hunter.getGrant().getRank(), granter.getGrant().getRank())) {
                Message.send(sender, "<red>You cannot punish " + hunter.getUsernameColored() + ".");
                return;
            }
        }

        if (hunter.hasPunishment(PunishmentStyle.BAN) != null) {
            Message.send(sender, "<red>Punishment is present for " + hunter.getUsernameColored() + ".");
            return;
        }

        long durations = duration.getDuration();
        Reason reasons = Reason.fromString(reason);
        String server = scope.getServer();
        UUID addedBy = sender instanceof Player ? ((Player) sender).getUniqueId() : null;

        Punishment punishment = new Punishment(
                hunter.generatePunishmentId(),
                PunishmentStyle.BAN,
                hunter.getAddress(),
                addedBy,
                System.currentTimeMillis(),
                reasons.getReason(),
                Melee.getInstance().getConfig("language.yml").getString("server_name"),
                server,
                durations,
                PunishmentType.ACTIVE
        );

        hunter.getPunishments().add(punishment);
        hunter.save();

        Message.send(sender, ("<hover:show_text:'<green>On scope scope%" +
                "<newline><yellow>With reason of reason%'>")
                .replace("reason%", reasons.getReason())
                .replace("scope%", server) +
                "<green>You've <yellow>duration% <green>banned player% <gray>for time%."
                        .replace("player%", hunter.getUsernameColored())
                        .replace("duration%", punishment.isPermanent() ? "permanently" : "temporarily")
                        .replace("time%",
                                (punishment.isPermanent() ?
                                        "forever" :
                                        duration.isPermanent() ?
                                                "forever" :
                                                Converter.millisToTime(durations))));

        String message = ("<hover:show_text:'<green>On scope scope%" +
                "<newline><yellow>With reason of reason%" +
                "<newline><light_purple>For time%'>")
                .replace("reason%", reasons.getReason())
                .replace("scope%", server)
                .replace("time%",
                        (punishment.isPermanent() ?
                                "forever" :
                                duration.isPermanent() ?
                                        "forever" :
                                        Converter.millisToTime(durations))) +
                "<gray>silent% player% <green>has been <yellow>duration% <green>banned by <red>staff%."
                        .replace("player%", hunter.getUsernameColored())
                        .replace("duration%", punishment.isPermanent() ? "permanently" : "temporarily")
                        .replace("silent%", reasons.isSilent() ? "[Silent]" : "")
                        .replace("staff%", addedBy == null ? "Console" : Hunter.getHunter(addedBy).getUsernameColored());


        String kickMessage = String.join("&%$",
                hunter.getUuid().toString(),
                server,
                punishment.getMessage()
        );

        String chatMessage = String.join("&%$",
                scope.getServer(),
                "message",
                message,
                reasons.isSilent() ? "melee.staff" : "global",
                reasons.isSilent() ? "true" : "false"
        );

        try (Jedis jedis = Melee.getInstance().getRedisManager().getConnection()) {
            jedis.publish(PunishmentSubscriber.update, kickMessage);
            jedis.publish(BroadcastSubscriber.update, chatMessage);
        }
    }

}
