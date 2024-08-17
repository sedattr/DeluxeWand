package me.sedattr.deluxewand.utilities;

import me.sedattr.deluxewand.DeluxeWand;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class MessageUtil {
    public static void sendMessage(CommandSender player, String messagePath) {
        player.sendMessage(colorize(getMessage(messagePath)));
    }

    public static void sendRawMessage(CommandSender player, String messagePath) {
        player.sendMessage(colorize(messagePath));
    }

    private static String getMessage(String messagePath) {
        return DeluxeWand.getInstance().getMessagesFile().getString(messagePath);
    }

    public static String colorize(String string) {
        return ChatColor.translateAlternateColorCodes('&', string);
    }

    public static void sendSeparator(CommandSender player) {
        player.sendMessage("");
        player.sendMessage("");
        player.sendMessage("");
    }
}
