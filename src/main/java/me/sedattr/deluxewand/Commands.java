package me.sedattr.deluxewand;

import me.sedattr.deluxewand.items.Wand;
import me.sedattr.deluxewand.utilities.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class Commands implements CommandExecutor {
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        sender.sendMessage("Builder's Wand Command");

        if (args.length < 1) {
            helpCommand(sender);
            return true;
        }

        switch (args[0]) {
            case "reload":
                reloadCommand(sender);
                break;
            case "give":
                giveCommand(sender, args);
                break;
            default:
                helpCommand(sender);
        }

        return true;
    }

    private void reloadCommand(CommandSender player) {
        if (player instanceof Player && !player.hasPermission("buildersWand.reload")) {
            MessageUtil.sendMessage(player, "noPermissions");
            return;
        }

        MessageUtil.sendMessage(player, "reload");
        DeluxeWand.getInstance().getWandManager().load();
    }

    private void giveCommand(CommandSender player, String[] args) {
        boolean isPlayerInstance = player instanceof Player;
        if (isPlayerInstance && !player.hasPermission("buildersWand.give")) {
            MessageUtil.sendMessage(player, "noPermissions");
            return;
        }
        Wand wand;
        Player destPlayer;

        if (args.length < 1) {
            helpCommand(player);
            return;
        } else if (args.length == 1 && isPlayerInstance) {
            wand = DeluxeWand.getInstance().getWandManager().getWandTier(1);
            destPlayer = (Player) player;

        } else if (args.length == 2) {
            destPlayer = Bukkit.getPlayer(args[1]);
            wand = DeluxeWand.getInstance().getWandManager().getWandTier(1);
        } else {
            wand = DeluxeWand.getInstance().getWandManager().getWandTier(Integer.parseInt(args[2]));
            destPlayer = Bukkit.getPlayer(args[1]);
        }

        if (destPlayer == null) {
            MessageUtil.sendMessage(player, "playerNotFound");
            return;
        } else if (wand == null) {
            MessageUtil.sendMessage(player, "wandNotFound");
            return;
        }

        ItemStack itemStack = wand.getRecipeResult();
        destPlayer.getInventory().addItem(itemStack);
    }

    private void helpCommand(CommandSender player) {
        MessageUtil.sendSeparator(player);
        MessageUtil.sendRawMessage(player, "             &b&lBuildersWand help");
        player.sendMessage("");
        MessageUtil.sendRawMessage(player, "&e&l»&r&e /bw reload &7- Reloads the config file.");
        MessageUtil.sendRawMessage(player, "&e&l»&r&e /bw give <player> &7- Give the builderswand tier 1 to a player.");
        MessageUtil.sendRawMessage(player, "&e&l»&r&e /bw give <player> <tier> &7- Give the builderswand tier X to a player.");
        MessageUtil.sendSeparator(player);
    }
}
