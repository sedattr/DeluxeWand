package me.sedattr.deluxewand.items;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
public class Wand {
    private String name;
    private Material material;
    private List<String> lore;

    private List<String> blacklist;
    private List<String> whitelist;

    private boolean particleEnabled;
    private String particle;
    private int particleCount;

    private boolean consumeItems;
    private int maxSize;

    private boolean durabilityEnabled;
    private int durability;
    private String durabilityText;

    private String permission = "";

    public ItemStack getRecipeResult() {
        ItemStack buildersWand = new ItemStack(getMaterial());
        ItemMeta itemMeta = buildersWand.getItemMeta();
        itemMeta.setDisplayName(getName());

        List<String> newLore = new ArrayList<>();
        for (String line : this.lore) {
            newLore.add(line.replace("%durability%", String.valueOf(this.durability)));
        }
        itemMeta.setLore(newLore);

        buildersWand.setItemMeta(itemMeta);

        return buildersWand;
    }

    public boolean hasPermission() {
        return !getPermission().isEmpty();
    }
}
