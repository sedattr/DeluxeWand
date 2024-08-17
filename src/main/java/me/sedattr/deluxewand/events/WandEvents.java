package me.sedattr.deluxewand.events;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import me.sedattr.deluxewand.DeluxeWand;
import me.sedattr.deluxewand.enums.ParticleShapeHidden;
import me.sedattr.deluxewand.helper.WorldGuardAPI;
import me.sedattr.deluxewand.items.Wand;
import me.sedattr.deluxewand.utilities.MessageUtil;
import org.apache.commons.lang.ArrayUtils;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.MaterialData;
import org.bukkit.plugin.Plugin;
import world.bentobox.bentobox.BentoBox;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.bentobox.lists.Flags;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

public class WandEvents implements Listener {
    private final HashMap<Block, List<Block>> blockSelection = new HashMap<>();
    private final HashMap<Block, List<Block>> tmpReplacements = new HashMap<>();
    private final List<Material> ignoreList = new ArrayList<>();
    private HashMap<Block, List<Block>> replacements = new HashMap<>();

    public WandEvents() {
        this.ignoreList.addAll(Arrays.asList(Material.LAVA, Material.WATER, Material.AIR, Material.CAVE_AIR, Material.VOID_AIR));

        startScheduler();
    }

    private void startScheduler() {
        int time = DeluxeWand.getInstance().getConfigFile().getInt("particle.render_time", 2);
        if (time <= 0)
            return;

        Set<Material> ignoreBlockTypes = new HashSet<>(Arrays.asList(Material.WATER, Material.LAVA, Material.AIR));

        Bukkit.getScheduler().runTaskTimer(DeluxeWand.getInstance(), () -> {
            this.blockSelection.clear();
            this.tmpReplacements.clear();

            for (Player player : Bukkit.getOnlinePlayers()) {
                ItemStack mainHand = player.getItemInHand();

                Wand wand = DeluxeWand.getInstance().getWandManager().getWand(mainHand);

                Block block;
                try {
                    block = player.getTargetBlock(ignoreBlockTypes, 5);
                } catch (Exception e) {
                    continue;
                }

                Material blockType = block.getType();
                Material blockAbove = player.getLocation().add(0, 1, 0).getBlock().getType();
                if (
                        this.ignoreList.contains(blockType)
                                || wand == null
                                || (!this.ignoreList.contains(blockAbove))
                ) {
                    continue;
                }

                List<Block> lastBlocks = player.getLastTwoTargetBlocks(ignoreBlockTypes, 5);
                if (lastBlocks.size() < 2) {
                    continue;
                }

                BlockFace blockFace = lastBlocks.get(1).getFace(lastBlocks.get(0));
                int itemCount = getItemCount(player, block, mainHand);

                this.blockSelection.put(block, new ArrayList<>());
                this.tmpReplacements.put(block, new ArrayList<>());

                setBlockSelection(player, blockFace, itemCount, block, block, wand);
                this.replacements = tmpReplacements;
                List<Block> selection = this.blockSelection.get(block);

                if (wand.isParticleEnabled()) {
                    for (Block selectionBlock : selection) {
                        renderBlockOutlines(blockFace, selectionBlock, selection, wand, player);
                    }
                }
            }
        }, 0L, time);
    }

    @EventHandler
    public void placeBlock(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        ItemStack mainHand = player.getItemInHand();
        Wand wand = DeluxeWand.getInstance().getWandManager().getWand(mainHand);
        if (wand == null)
            return;

        event.setCancelled(true);
    }

    @EventHandler
    public void playerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack mainHand = player.getItemInHand();
        Wand wand = DeluxeWand.getInstance().getWandManager().getWand(mainHand);

        if (wand == null || event.getAction() != Action.RIGHT_CLICK_BLOCK || event.getHand() != EquipmentSlot.HAND)
            return;

        Block against = event.getClickedBlock();
        List<Block> selection = this.replacements.get(against);
        if (selection == null)
            return;

        if (
                !player.hasPermission("buildersWand.use")
                        || (!player.hasPermission("buildersWand.bypass") && !isAllowedToBuildForExternalPlugins(player, selection))
                        || wand.hasPermission() && !player.hasPermission(wand.getPermission())
        ) {
            MessageUtil.sendMessage(player, "noPermissions");
            return;
        }

        Material blockType = against.getType();
        byte blockSubId = against.getData();
        ItemStack itemStack = new ItemStack(against.getType());
        MaterialData materialData = itemStack.getData();
        materialData.setData(blockSubId);
        itemStack.setData(materialData);
        event.setCancelled(true);

        Bukkit.getScheduler().runTaskLater(DeluxeWand.getInstance(), () -> {
            for (Block selectionBlock : selection) {
                selectionBlock.setType(blockType);
                selectionBlock.setBlockData(against.getBlockData());

                try {
                    Method m = Block.class.getMethod("setData", byte.class);
                    m.invoke(selectionBlock, blockSubId);
                } catch (NoSuchMethodException | IllegalAccessException
                         | InvocationTargetException ignored) {
                }
            }

        }, 1L);

        int amount = selection.size();
        if (wand.isConsumeItems()) {
            removeItemStack(itemStack, amount, player);
        }

        if (wand.isDurabilityEnabled() && amount >= 1) {
            removeDurability(mainHand, player, wand);
        }
    }

    @EventHandler
    private void craftItemEvent(CraftItemEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        ItemStack result = event.getRecipe().getResult();
        Wand wand = DeluxeWand.getInstance().getWandManager().getWand(result);
        if (wand == null) {
            return;
        }

        if (!player.hasPermission("buildersWand.craft")) {
            MessageUtil.sendMessage(player, "noPermissions");
            event.setCancelled(true);
        }

        Inventory inventory = event.getInventory();
        ItemStack itemStack = event.getInventory().getResult();
        if (itemStack == null)
            return;

        inventory.setItem(0, itemStack);
        player.updateInventory();
    }

    @EventHandler
    public void inventoryClickEvent(InventoryClickEvent event) {
        Inventory inventory = event.getView().getTopInventory();
        if (!(inventory instanceof CraftingInventory)) {
            return;
        }

        ItemStack itemStack = event.getCurrentItem();
        Wand wand = DeluxeWand.getInstance().getWandManager().getWand(itemStack);
        if (wand == null) {
            return;
        }

        ClickType clickType = event.getClick();
        if (clickType == ClickType.SHIFT_LEFT || clickType == ClickType.SHIFT_RIGHT) {
            event.setCancelled(true);
        }
    }

    private int getItemCount(Player player, Block block, ItemStack mainHand) {

        if (mainHand.getType() == Material.AIR)
            return 0;

        if (player.getGameMode() == GameMode.CREATIVE)
            return Integer.MAX_VALUE;

        int count = 0;
        Inventory inventory = player.getInventory();
        Material blockMaterial = block.getType();
        ItemStack[] inventoryContents = inventory.getContents();
        ItemStack helmet = inventory.getItem(39);

        if (helmet != null)
            inventoryContents = (ItemStack[]) ArrayUtils.removeElement(inventoryContents, helmet);

        for (ItemStack itemStack : inventoryContents) {
            if (itemStack == null)
                continue;
            Material itemMaterial = itemStack.getType();

            if (!itemMaterial.equals(blockMaterial) || block.getData() != itemStack.getData().getData())
                continue;

            count += itemStack.getAmount();
        }

        return count;
    }

    private void removeDurability(ItemStack wandItemStack, Player player, Wand wand) {
        Inventory inventory = player.getInventory();
        if (player.getGameMode() == GameMode.CREATIVE)
            return;

        int durability = getDurability(wandItemStack, wand);
        int newDurability = durability - 1;

        if (newDurability <= 0)
            inventory.removeItem(wandItemStack);

        ItemMeta itemMeta = wandItemStack.getItemMeta();
        List<String> lore = wand.getLore();

        List<String> newLore = new ArrayList<>();
        for (String line : lore)
            newLore.add(line.replace("%durability%", String.valueOf(newDurability)));

        itemMeta.setLore(newLore);
        wandItemStack.setItemMeta(itemMeta);
    }

    private void removeItemStack(ItemStack itemStack, int amount, Player player) {
        Inventory inventory = player.getInventory();
        Material material = itemStack.getType();
        ItemStack[] itemStacks = inventory.getContents();

        if (player.getGameMode() == GameMode.CREATIVE) {
            return;
        }

        for (ItemStack inventoryItemStack : itemStacks) {
            if (inventoryItemStack == null) {
                continue;
            }
            Material itemMaterial = inventoryItemStack.getType();
            if (!itemMaterial.equals(material) || itemStack.getData().getData() != inventoryItemStack.getData().getData()) {
                continue;
            }

            int itemAmount = inventoryItemStack.getAmount();
            if (amount >= itemAmount) {

                HashMap<Integer, ItemStack> didntRemovedItems = inventory.removeItem(inventoryItemStack);

                if (didntRemovedItems.size() == 1) {
                    player.getInventory().setItemInOffHand(null);
                }

                amount -= itemAmount;
                player.updateInventory();
            } else {
                inventoryItemStack.setAmount(itemAmount - amount);
                player.updateInventory();
                return;
            }
        }
    }

    private void setBlockSelection(Player player, BlockFace blockFace, int maxLocations, Block startBlock, Block blockToCheck, Wand wand) {
        if (!isValidBlockSelection(player, blockFace, maxLocations, startBlock, blockToCheck, wand)) {
            return;
        }

        List<Block> selection = blockSelection.get(startBlock);
        List<Block> replacementsList = tmpReplacements.get(startBlock);

        selection.add(blockToCheck);
        replacementsList.add(blockToCheck.getRelative(blockFace));

        for (BlockFace face : getRelevantBlockFaces(blockFace)) {
            setBlockSelection(player, blockFace, maxLocations, startBlock, blockToCheck.getRelative(face), wand);
        }
    }

    private boolean isValidBlockSelection(Player player, BlockFace blockFace, int maxLocations, Block startBlock, Block blockToCheck, Wand wand) {
        Material startMaterial = startBlock.getType();
        Material blockToCheckMaterial = blockToCheck.getType();
        Location startLocation = startBlock.getLocation();
        Location checkLocation = blockToCheck.getLocation();
        int blockToCheckData = blockToCheck.getData();
        int startBlockData = startBlock.getData();

        List<Block> selection = blockSelection.get(startBlock);
        List<String> blacklist = wand.getBlacklist();
        List<String> whitelist = wand.getWhitelist();

        return startLocation.distance(checkLocation) < wand.getMaxSize()
                && startMaterial.equals(blockToCheckMaterial)
                && !startMaterial.toString().endsWith("SLAB")
                && !startMaterial.toString().endsWith("STEP")
                && selection.size() < maxLocations
                && blockToCheckData == startBlockData
                && !selection.contains(blockToCheck)
                && ignoreList.contains(blockToCheck.getRelative(blockFace).getType())
                && (whitelist.isEmpty() || whitelist.contains(startMaterial.toString()))
                && (blacklist.isEmpty() || !blacklist.contains(startMaterial.toString()))
                && (isAllowedToBuildForExternalPlugins(player, checkLocation) || player.hasPermission("buildersWand.bypass"))
                && player.hasPermission("buildersWand.use")
                && (!wand.hasPermission() || player.hasPermission(wand.getPermission()));
    }

    private BlockFace[] getRelevantBlockFaces(BlockFace blockFace) {
        return switch (blockFace) {
            case UP, DOWN -> new BlockFace[]{BlockFace.EAST, BlockFace.WEST, BlockFace.NORTH, BlockFace.SOUTH};
            case EAST, WEST -> new BlockFace[]{BlockFace.NORTH, BlockFace.SOUTH, BlockFace.UP, BlockFace.DOWN};
            case SOUTH, NORTH -> new BlockFace[]{BlockFace.WEST, BlockFace.EAST, BlockFace.UP, BlockFace.DOWN};
            default -> new BlockFace[]{};
        };
    }

    private void renderBlockOutlines(BlockFace blockFace, Block selectionBlock, List<Block> selection, Wand wand, Player player) {
        List<ParticleShapeHidden> shapes = new ArrayList<>();

        BlockFace[] faces = {
                BlockFace.EAST, BlockFace.WEST, BlockFace.NORTH, BlockFace.SOUTH,
                BlockFace.UP, BlockFace.DOWN, BlockFace.NORTH_WEST, BlockFace.NORTH_EAST,
                BlockFace.SOUTH_EAST, BlockFace.SOUTH_WEST, BlockFace.SELF
        };

        for (BlockFace face : faces) {
            if (selection.contains(selectionBlock.getRelative(face))) {
                shapes.add(mapBlockFaceToShape(face));
            }
        }

        DeluxeWand.getInstance().getParticleUtil().drawBlockOutlines(blockFace, shapes, selectionBlock.getRelative(blockFace).getLocation(), wand, player);
    }

    private ParticleShapeHidden mapBlockFaceToShape(BlockFace face) {
        return switch (face) {
            case EAST -> ParticleShapeHidden.EAST;
            case WEST -> ParticleShapeHidden.WEST;
            case NORTH -> ParticleShapeHidden.NORTH;
            case SOUTH -> ParticleShapeHidden.SOUTH;
            case UP -> ParticleShapeHidden.UP;
            case DOWN -> ParticleShapeHidden.DOWN;
            case NORTH_WEST -> ParticleShapeHidden.NORTH_WEST;
            case NORTH_EAST -> ParticleShapeHidden.NORTH_EAST;
            case SOUTH_EAST -> ParticleShapeHidden.SOUTH_EAST;
            case SOUTH_WEST -> ParticleShapeHidden.SOUTH_WEST;
            default -> null;
        };
    }

    private boolean isAllowedToBuildForExternalPlugins(Player player, Location location) {
        Plugin worldGuardPlugin = getExternalPlugin("WorldGuard");
        if (worldGuardPlugin instanceof WorldGuardPlugin) {
            if (!WorldGuardAPI.getWorldGuardAPI().allows(player, location))
                return false;
        }

        Plugin bentoBox = getExternalPlugin("BentoBox");
        if (bentoBox != null) {
            BentoBox bentoBoxapi = BentoBox.getInstance();
            User user = User.getInstance(player);
            Optional<Island> island = bentoBoxapi.getIslands().getIslandAt(location);
            return island.isEmpty() || island.get().isAllowed(user, Flags.PLACE_BLOCKS);
        }

        return true;
    }

    private boolean isAllowedToBuildForExternalPlugins(Player player, List<Block> selection) {
        Plugin worldGuardPlugin = getExternalPlugin("WorldGuard");
        if (worldGuardPlugin instanceof WorldGuardPlugin) {
            for (Block selectionBlock : selection) {
                if (!WorldGuardAPI.getWorldGuardAPI().allows(player, selectionBlock.getLocation())) {
                    return false;
                }
            }
        }

        Plugin bentoBox = getExternalPlugin("BentoBox");
        if (bentoBox != null) {
            BentoBox bentoBoxapi = BentoBox.getInstance();
            User user = User.getInstance(player);
            for (Block selectionBlock : selection) {
                Optional<Island> island = bentoBoxapi.getIslands().getIslandAt(selectionBlock.getLocation());
                if (island.isPresent() && !island.get().isAllowed(user, Flags.PLACE_BLOCKS)) {
                    return false;
                }
            }
        }

        return true;
    }

    private Plugin getExternalPlugin(String name) {
        return DeluxeWand.getInstance().getServer().getPluginManager().getPlugin(name);
    }

    private int getDurability(ItemStack wandItemStack, Wand wand) {
        ItemMeta itemMeta = wandItemStack.getItemMeta();
        List<String> lore = itemMeta.getLore();
        if (lore == null) {
            return wand.getDurability();
        }
        String durabilityString = lore.get(0);
        durabilityString = ChatColor.stripColor(durabilityString);
        durabilityString = durabilityString.replaceAll("[^0-9]", "");

        return Integer.parseInt(durabilityString);
    }
}
