package poa.packets;

import net.minecraft.network.protocol.Packet;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;
import poa.util.PoaPlugin262;
import poa.util.FoliaScheduler;

public class SendPacket262 {
    public static void sendPacket(Player player, Object packet) {
        if (player == null || !player.isOnline() || packet == null) return;
        FoliaScheduler.entity(PoaPlugin262.getPlugin(), player, () ->
                ((CraftPlayer) player).getHandle().connection.send((Packet<?>) packet)
        );
    }
}
