package poa.guardian;

import lombok.Getter;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;
import poa.util.FoliaScheduler;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;
import poa.packets.FakeEntity262;
import poa.packets.Metadata262;
import poa.packets.TeamPacket262;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class GuardianBeam262 {

   // public static Map<String, GuardianBeam121> dataMap = new HashMap<>();


    @Getter
    List<UUID> uuids = new ArrayList<>();
    Set<UUID> currentlySeeing = java.util.concurrent.ConcurrentHashMap.newKeySet();
    int guardianID;
    int squidID;
    UUID guardianUUID;
    UUID squidUUID;
    String beamID;
    Location guardianLoc;
    Location batLoc;
    String color;
    Plugin plugin;
    Map<UUID, ScheduledTask> tasks = new HashMap<>();


    public GuardianBeam262(List<Player> players, String id, Location startLoc, Location endLoc, String color, Plugin plugin) {
//        if(dataMap.containsKey(id)){
//            plugin.getLogger().log(Level.WARNING, "creation of guardian beam id " + id + " failed, already existing id");
//            return;
//        }
        this.color = color;

        this.plugin = plugin;

        for (Player player : players)
            this.uuids.add(player.getUniqueId());
        this.beamID = id;
        this.guardianLoc = startLoc;
        this.batLoc = getEndLocation(startLoc, endLoc);
        this.guardianUUID = UUID.randomUUID();
        this.squidUUID = UUID.randomUUID();

        final ThreadLocalRandom current = ThreadLocalRandom.current();
        final int maxValue = Integer.MAX_VALUE - 1;
        this.guardianID = current.nextInt(99999, maxValue);
        this.squidID = current.nextInt(99999, maxValue);


        for (UUID uuid : this.uuids) {
            final Player player = Bukkit.getPlayer(uuid);
            if (player == null)
                continue;

            runCheckAndShow((CraftPlayer) player);
        }


     //   dataMap.put(this.beamID, this);
    }


    public void runCheckAndShow(CraftPlayer player) {
        final UUID uuid = player.getUniqueId();
        if (player.getWorld() != this.guardianLoc.getWorld()) {
            currentlySeeing.remove(uuid);
            return;
        }

        final Location playerLocation = player.getLocation();
        if (playerLocation.distanceSquared(guardianLoc) > 22500 || playerLocation.distanceSquared(batLoc) > 22500) { //150 blocks
            currentlySeeing.remove(uuid);
            return;
        }

        if (currentlySeeing.contains(uuid))
            return;

        currentlySeeing.add(uuid);

        show(player);
    }

    public void show(CraftPlayer player) {
        try {
            final Object guardianPacket = FakeEntity262.fakeEntityPacket(this.guardianID, this.guardianLoc, "guardian", this.guardianUUID);
            final Object squidPacket = FakeEntity262.fakeEntityPacket(this.squidID, this.batLoc, "bat", this.squidUUID);

            final Object teamPacket = TeamPacket262.teamPacketForBeam(List.of(this.guardianUUID.toString(), this.squidUUID.toString()), this.color);

            final Metadata262 guardianMeta = new Metadata262(this.guardianID);
            guardianMeta.setInvisible(true);
            guardianMeta.setGuardianTarget(this.squidID);
            guardianMeta.setGuardianSpikes(false);

            if(!this.color.equalsIgnoreCase("white"))
                guardianMeta.setGlow(true);

            final Metadata262 squidMeta = new Metadata262(this.squidID);
            squidMeta.setInvisible(true);


            FoliaScheduler.entity(plugin, player, () -> {
                final ServerGamePacketListenerImpl connection = player.getHandle().connection;
                connection.send((Packet<?>) guardianPacket);
                connection.send((Packet<?>) squidPacket);
                connection.send((Packet<?>) teamPacket);
                connection.send((Packet<?>) guardianMeta.build());
                connection.send((Packet<?>) squidMeta.build());
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Location getEndLocation(Location start, Location end) {
        Vector guardianVector = start.toVector();
        Vector batVector = end.toVector();
        Vector direction = guardianVector.subtract(batVector).normalize();

        Vector newBatVector = batVector.add(direction);
        return newBatVector.toLocation(end.getWorld());
    }

    public void loop() {
        destroyTasks();
        for (UUID uuid : this.uuids) {
            Player player = Bukkit.getPlayer(uuid);
            if (player == null || !player.isOnline()) continue;
            ScheduledTask task = FoliaScheduler.entityAtFixedRate(plugin, player, () -> {
                Player current = Bukkit.getPlayer(uuid);
                if (current != null && current.isOnline()) runCheckAndShow((CraftPlayer) current);
            }, 20L, 20L);
            if (task != null) tasks.put(uuid, task);
        }
    }

    private void destroyTasks() {
        for (ScheduledTask task : tasks.values()) if (task != null) task.cancel();
        tasks.clear();
    }

    public void destroy() {
        final Object removePacket = FakeEntity262.removeFakeEntityPacket(List.of(this.guardianID, this.squidID));
        for (UUID uuid : this.uuids) {
            final Player player = Bukkit.getPlayer(uuid);
            if (player == null)
                continue;

            CraftPlayer craftPlayer = (CraftPlayer) player;
            FoliaScheduler.entity(plugin, player, () ->
                    craftPlayer.getHandle().connection.send((Packet<?>) removePacket));
        }
        destroyTasks();
        //  dataMap.remove(this.beamID);
    }

}
