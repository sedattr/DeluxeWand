package me.sedattr.deluxewand;

import lombok.Getter;
import me.sedattr.deluxewand.events.WandEvents;
import me.sedattr.deluxewand.manager.WandManager;
import me.sedattr.deluxewand.utilities.ParticleUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class DeluxeWand extends JavaPlugin {
    @Getter private static DeluxeWand instance;

    @Getter private ParticleUtil particleUtil;
    @Getter private WandManager wandManager;

    @Getter private YamlConfiguration messagesFile;
    @Getter private YamlConfiguration wandsFile;
    @Getter private FileConfiguration configFile;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        this.configFile = getConfig();
        this.messagesFile = getConfiguration("messages.yml");
        this.wandsFile = getConfiguration("wands.yml");

        this.wandManager = new WandManager();
        this.particleUtil = new ParticleUtil();
        this.wandManager.load();

        registerEvents();
        registerCommands();

        System.out.println("DELUXEWAND PLUGIN IS STARTED!");
    }

    public YamlConfiguration getConfiguration(String name) {
        File file = new File(getInstance().getDataFolder(), name);
        try {
            if (!file.exists())
                getInstance().saveResource(name, false);

            YamlConfiguration configuration = new YamlConfiguration();
            configuration.load(file);

            return configuration;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void registerCommands() {
        PluginCommand command = getCommand("deluxewand");
        if (command != null)
            command.setExecutor(new Commands());
    }

    private void registerEvents() {
        PluginManager pluginManager = Bukkit.getPluginManager();
        pluginManager.registerEvents(new WandEvents(), this);
    }
}
