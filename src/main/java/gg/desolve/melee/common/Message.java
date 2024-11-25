package gg.desolve.melee.common;

import gg.desolve.melee.Melee;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Message {

    public static String translate(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    public static void send(Player player, String message) {
        Melee.getInstance().getAdventure().player(player)
                .sendMessage(MiniMessage.miniMessage().deserialize(message));
    }

    public static void send(CommandSender sender, String message) {
        Melee.getInstance().getAdventure().player((Player) sender)
                .sendMessage(MiniMessage.miniMessage().deserialize(message));
    }

}
