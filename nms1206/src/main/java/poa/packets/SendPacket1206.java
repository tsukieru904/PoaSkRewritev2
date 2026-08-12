package poa.packets;

import net.minecraft.network.protocol.Packet;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;
import poa.util.PoaPlugin1206;
import poa.util.FoliaScheduler;

public class SendPacket1206 {
    public static void sendPacket(Player player, Object packet) {
        if (player == null || !player.isOnline() || packet == null) return;
        FoliaScheduler.entity(PoaPlugin1206.getPlugin(), player, () ->
                ((CraftPlayer) player).getHandle().connection.send((Packet<?>) packet)
        );
    }
}
