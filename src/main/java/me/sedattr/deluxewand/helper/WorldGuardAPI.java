package me.sedattr.deluxewand.helper;

import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.session.SessionManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Method;

public class WorldGuardAPI {
    private static WorldGuardAPI instance;

    public static WorldGuardAPI getWorldGuardAPI() {
        if (instance == null) {
            instance = new WorldGuardAPI();
            final Plugin p = Bukkit.getPluginManager().getPlugin("WorldGuard");
        }
        return instance;
    }

    public boolean allows(Player player, Location l) {
        if (hasBypass(player, l))
            return true;
        final com.sk89q.worldguard.protection.regions.RegionContainer c = com.sk89q.worldguard.WorldGuard.getInstance().getPlatform().getRegionContainer();
        final com.sk89q.worldguard.protection.regions.RegionQuery q = c.createQuery();
        final ApplicableRegionSet s = q.getApplicableRegions(com.sk89q.worldedit.bukkit.BukkitAdapter.adapt(l));

        try {
            return s.testState(getLocalPlayer(player), (StateFlag) Flags.class.getDeclaredField("BUILD").get(null));
        } catch (IllegalAccessException | NoSuchFieldException e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean hasBypass(Player player, Location l) {
        final World w = l.getWorld();
        final com.sk89q.worldedit.world.World wew = com.sk89q.worldedit.bukkit.BukkitAdapter.adapt(w);
        final SessionManager m = com.sk89q.worldguard.WorldGuard.getInstance().getPlatform().getSessionManager();
        try {
            final Method method = m.getClass().getMethod("hasBypass", LocalPlayer.class, com.sk89q.worldedit.world.World.class);
            return (Boolean) method.invoke(m, getLocalPlayer(player), wew);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private LocalPlayer getLocalPlayer(Player player) {
        return player != null ? WorldGuardPlugin.inst().wrapPlayer(player) : null;
    }
}