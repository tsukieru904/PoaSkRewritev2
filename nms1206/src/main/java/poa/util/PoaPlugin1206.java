package poa.util;

import org.bukkit.plugin.Plugin;

/**
 * Utility class to store and retrieve the plugin instance for NMS 1.20.6
 */
public class PoaPlugin1206 {
    private static Plugin plugin;
    
    /**
     * Set the plugin instance
     */
    public static void setPlugin(Plugin p) {
        plugin = p;
    }
    
    /**
     * Get the plugin instance
     */
    public static Plugin getPlugin() {
        return plugin;
    }
}
