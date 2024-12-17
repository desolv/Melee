package gg.desolve.melee.command.management;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import gg.desolve.melee.Melee;
import gg.desolve.melee.common.Converter;
import gg.desolve.melee.common.Message;
import gg.desolve.melee.configuration.MeleeConfigManager;
import gg.desolve.melee.server.BroadcastSubscriber;
import gg.desolve.melee.server.Scope;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import redis.clients.jedis.Jedis;

@CommandAlias("announce")
public class AnnounceCommand extends BaseCommand {

    @Default
    @CommandCompletion("@scopes @nothing")
    @CommandPermission("melee.command.announce")
    @Syntax("[scope] <message>")
    @Description("Announce a message to the server")
    public static void execute(CommandSender sender, @Single Scope scope, String message) {
        String redisMessage = String.join("&%$",
                scope.getServer(),
                "message",
                message,
                "global"
        );

        try (Jedis jedis = Melee.getInstance().getRedisManager().getConnection()) {
            jedis.publish(BroadcastSubscriber.update, redisMessage);
        }
    }

}
