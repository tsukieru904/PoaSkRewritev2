package poa.packets;

import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.custom.BrandPayload;
import net.minecraft.network.protocol.common.custom.GameTestAddMarkerDebugPayload;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_20_R2.block.CraftBlock;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class SendPayloadPacket1202 {

    public static void sendGameTestMarker(Plugin plugin, Player player, Location location, String text, Color color, int durationMs) {
        BlockPos pos = ((CraftBlock) location.getBlock()).getPosition();

        GameTestAddMarkerDebugPayload marker = new GameTestAddMarkerDebugPayload(pos, color.asARGB(), text, durationMs);

        SendPacket1202.sendPacket(plugin, player, new ClientboundCustomPayloadPacket(marker));

    }

    public static void sendBrandPayload(Plugin plugin, Player player, String brand){
        SendPacket1202.sendPacket(plugin, player, new ClientboundCustomPayloadPacket(new BrandPayload(brand)));
    }

}
