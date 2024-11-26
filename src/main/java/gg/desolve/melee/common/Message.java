package gg.desolve.melee.common;

import gg.desolve.melee.Melee;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Message {

    public static String translate(String message) {
        return ChatColor.translateAlternateColorCodes('&',
                LegacyComponentSerializer.legacyAmpersand().serialize(
                        MiniMessage.miniMessage().deserialize(message)
                ));
    }

    public static void send(Player player, String message) {
        Melee.getInstance().getAdventure().player(player)
                .sendMessage(MiniMessage.miniMessage().deserialize(message));
    }

    public static void send(CommandSender sender, String message) {
        if (sender instanceof Player) {
            send((Player) sender, message);
            return;
        }

        message = message.replace("<newline>", "\n");
        sender.sendMessage(MiniMessage.miniMessage().stripTags(message));
    }

}
