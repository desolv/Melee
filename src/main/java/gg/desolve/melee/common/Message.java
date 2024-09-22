package gg.desolve.melee.common;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Message {

    public static String translate(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    public static void send(Player player, String message) {
        player.sendMessage(translate(message));
    }

    public static void send(CommandSender sender, String message) {
        sender.sendMessage(translate(message));
    }

}
