package poa.packets;

import net.minecraft.network.protocol.Packet;
import org.bukkit.craftbukkit.v1_20_R2.entity.CraftPlayer;
import org.bukkit.entity.Player;
import poa.util.PoaPlugin1202;
import poa.util.FoliaScheduler;

public class SendPacket1202 {
    public static void sendPacket(Player player, Object packet) {
        if (player == null || !player.isOnline() || packet == null) return;
        FoliaScheduler.entity(PoaPlugin1202.getPlugin(), player, () ->
                ((CraftPlayer) player).getHandle().connection.send((Packet<?>) packet)
        );
    }
}
