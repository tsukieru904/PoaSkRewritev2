package poa.util;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

/** Scheduler bridge for Paper/Folia. */
public final class FoliaScheduler {
    private FoliaScheduler() {}
    
    public static BukkitTask async(Plugin plugin, Runnable task) { 
        return plugin.getServer().getScheduler().runTaskAsynchronously(plugin, task); 
    }
    
    public static BukkitTask asyncLater(Plugin plugin, Runnable task, long delayTicks) { 
        return plugin.getServer().getScheduler().runTaskLaterAsynchronously(plugin, task, delayTicks); 
    }
    
    public static BukkitTask asyncAtFixedRate(Plugin plugin, Runnable task, long initialDelayTicks, long periodTicks) { 
        return plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin, task, initialDelayTicks, periodTicks); 
    }
    
    public static BukkitTask entity(Plugin plugin, Entity entity, Runnable task) { 
        return plugin.getServer().getScheduler().runTask(plugin, task); 
    }
    
    public static BukkitTask entityLater(Plugin plugin, Entity entity, Runnable task, long delayTicks) { 
        return plugin.getServer().getScheduler().runTaskLater(plugin, task, delayTicks); 
    }
    
    public static BukkitTask entityAtFixedRate(Plugin plugin, Entity entity, Runnable task, long initialDelayTicks, long periodTicks) { 
        return plugin.getServer().getScheduler().runTaskTimer(plugin, task, initialDelayTicks, periodTicks); 
    }
    
    public static BukkitTask region(Plugin plugin, Location location, Runnable task) { 
        return plugin.getServer().getScheduler().runTask(plugin, task); 
    }
    
    public static BukkitTask regionLater(Plugin plugin, Location location, Runnable task, long delayTicks) { 
        return plugin.getServer().getScheduler().runTaskLater(plugin, task, delayTicks); 
    }
    
    public static BukkitTask global(Plugin plugin, Runnable task) { 
        return plugin.getServer().getScheduler().runTask(plugin, task); 
    }
    
    public static BukkitTask globalLater(Plugin plugin, Runnable task, long delayTicks) { 
        return plugin.getServer().getScheduler().runTaskLater(plugin, task, delayTicks); 
    }
}
