package me.sedattr.deluxewand.manager;

import me.sedattr.deluxewand.DeluxeWand;
import me.sedattr.deluxewand.items.Wand;
import me.sedattr.deluxewand.utilities.MessageUtil;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class WandManager {
    private final List<Wand> wandList = new ArrayList<>();

    private void loadWands() {
        this.wandList.clear();

        for (String key : DeluxeWand.getInstance().getWandsFile().getKeys(false)) {
            this.wandList.add(getWand(key));
        }
    }

    private Wand getWand(String key) {
        String configPrefix = key + ".";
        Wand wand = new Wand();
        wand.setName(MessageUtil.colorize(DeluxeWand.getInstance().getWandsFile().getString(configPrefix + "name")));
        wand.setMaterial(Material.valueOf(DeluxeWand.getInstance().getWandsFile().getString(configPrefix + "material")));
        wand.setMaxSize(DeluxeWand.getInstance().getWandsFile().getInt(configPrefix + "maxSize"));
        wand.setConsumeItems(DeluxeWand.getInstance().getWandsFile().getBoolean(configPrefix + "consumeItems"));
        wand.setDurability(DeluxeWand.getInstance().getWandsFile().getInt(configPrefix + "durability.amount"));
        wand.setDurabilityEnabled(DeluxeWand.getInstance().getWandsFile().getBoolean(configPrefix + "durability.enabled"));
        wand.setDurabilityText(DeluxeWand.getInstance().getWandsFile().getString(configPrefix + "durability.text"));
        wand.setBlacklist(DeluxeWand.getInstance().getWandsFile().getStringList(configPrefix + "blacklist"));
        wand.setWhitelist(DeluxeWand.getInstance().getWandsFile().getStringList(configPrefix + "whitelist"));
        wand.setParticleEnabled(DeluxeWand.getInstance().getWandsFile().getBoolean(configPrefix + "particles.enabled"));
        wand.setParticle(DeluxeWand.getInstance().getWandsFile().getString(configPrefix + "particles.type"));
        wand.setParticleCount(DeluxeWand.getInstance().getWandsFile().getInt(configPrefix + "particles.count"));

        List<String> lore = DeluxeWand.getInstance().getWandsFile().getStringList(configPrefix + "lore");
        if (!lore.isEmpty()) {
            List<String> newLore = new ArrayList<>();
            for (String line : lore) {
                newLore.add(MessageUtil.colorize(line));
            }

            wand.setLore(newLore);
        }

        if (DeluxeWand.getInstance().getWandsFile().isSet(configPrefix + "permission"))
            wand.setPermission(DeluxeWand.getInstance().getWandsFile().getString(configPrefix + "permission"));

        return wand;
    }

    public Wand getWand(ItemStack itemStack) {
        for (Wand wand : wandList) {
            if (itemStack == null) {
                return null;
            }

            Material material = itemStack.getType();
            ItemMeta itemMeta = itemStack.getItemMeta();

            if (itemMeta == null) {
                return null;
            }

            String name = itemMeta.getDisplayName();
            if (wand.getName().equals(name) && material == wand.getMaterial()) {
                return wand;
            }
        }
        return null;
    }

    public void load() {
        loadWands();
    }

    public Wand getWandTier(int tier) {
        if (DeluxeWand.getInstance().getWandsFile().isSet(String.valueOf(tier))) {
            return getWand(String.valueOf(tier));
        }

        return null;
    }
}
