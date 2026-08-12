package poa.util;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;
import java.util.concurrent.TimeUnit;

/** Scheduler bridge for Paper/Folia. */
public final class FoliaScheduler {
    private FoliaScheduler() {}
    public static ScheduledTask async(Plugin plugin, Runnable task) { return plugin.getServer().getAsyncScheduler().runNow(plugin, t -> task.run()); }
    public static ScheduledTask asyncLater(Plugin plugin, Runnable task, long delayTicks) { return plugin.getServer().getAsyncScheduler().runDelayed(plugin, t -> task.run(), delayTicks * 50L, TimeUnit.MILLISECONDS); }
    public static ScheduledTask asyncAtFixedRate(Plugin plugin, Runnable task, long initialDelayTicks, long periodTicks) { return plugin.getServer().getAsyncScheduler().runAtFixedRate(plugin, t -> task.run(), initialDelayTicks * 50L, periodTicks * 50L, TimeUnit.MILLISECONDS); }
    public static ScheduledTask entity(Plugin plugin, Entity entity, Runnable task) { return entity.getScheduler().run(plugin, t -> task.run(), null); }
    public static ScheduledTask entityLater(Plugin plugin, Entity entity, Runnable task, long delayTicks) { return entity.getScheduler().runDelayed(plugin, t -> task.run(), null, delayTicks); }
    public static ScheduledTask entityAtFixedRate(Plugin plugin, Entity entity, Runnable task, long initialDelayTicks, long periodTicks) { return entity.getScheduler().runAtFixedRate(plugin, t -> task.run(), null, initialDelayTicks, periodTicks); }
    public static ScheduledTask region(Plugin plugin, Location location, Runnable task) { return plugin.getServer().getRegionScheduler().run(plugin, location, t -> task.run()); }
    public static ScheduledTask regionLater(Plugin plugin, Location location, Runnable task, long delayTicks) { return plugin.getServer().getRegionScheduler().runDelayed(plugin, location, t -> task.run(), delayTicks); }
    public static ScheduledTask global(Plugin plugin, Runnable task) { return plugin.getServer().getGlobalRegionScheduler().run(plugin, t -> task.run()); }
    public static ScheduledTask globalLater(Plugin plugin, Runnable task, long delayTicks) { return plugin.getServer().getGlobalRegionScheduler().runDelayed(plugin, t -> task.run(), delayTicks); }
}
