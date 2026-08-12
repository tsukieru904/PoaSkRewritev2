package poa.util;

import org.bukkit.plugin.Plugin;

/**
 * Plugin instance holder for NMS 1.20.2 module.
 */
public class PoaPlugin1202 {
    private static Plugin instance;

    private PoaPlugin1202() {}

    /**
     * Set the plugin instance.
     *
     * @param plugin the plugin instance
     */
    public static void setPlugin(Plugin plugin) {
        instance = plugin;
    }

    /**
     * Get the plugin instance.
     *
     * @return the plugin instance
     */
    public static Plugin getPlugin() {
        return instance;
    }
}
