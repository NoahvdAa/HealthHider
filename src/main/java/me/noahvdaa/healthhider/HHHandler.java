package me.noahvdaa.healthhider;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import net.minecraft.network.Connection;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

import java.util.List;

import static net.minecraft.world.entity.LivingEntity.DATA_HEALTH_ID;

public class HHHandler extends MessageToByteEncoder<Packet<?>> {

    private final MinecraftServer server = MinecraftServer.getServer();
    private final HealthHider plugin;

    public HHHandler(HealthHider plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean acceptOutboundMessage(Object msg) {
        return msg instanceof ClientboundSetEntityDataPacket;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, Packet msg, ByteBuf out) {
        FriendlyByteBuf buf = new FriendlyByteBuf(out);
        ClientboundSetEntityDataPacket packet = (ClientboundSetEntityDataPacket) msg;

        Entity entity = null;
        for (ServerLevel level : server.getAllLevels()) {
            entity = level.getEntityLookup().get(packet.id());
            if (entity != null) {
                break;
            }
        }

        List<SynchedEntityData.DataValue<?>> packed = packet.packedItems();
        SynchedEntityData.DataValue<?> healthValue = null;

        int i = 0;
        for (SynchedEntityData.DataValue<?> value : packed) {
            if (value.id() == DATA_HEALTH_ID.getId()) {
                healthValue = value;
                break;
            }
            i++;
        }

        if (healthValue == null || (entity != null && !(entity instanceof LivingEntity))) {
            // Only living entities have health
            this.writeId(ctx, packet, buf);
            packet.write(buf);
            return;
        }

        HHConfig config = plugin.configuration();
        if (entity != null && config.entities().contains(entity.getType()) != config.whitelistMode()) {
            // We don't need to censor this entity
            this.writeId(ctx, packet, buf);
            packet.write(buf);
            return;
        }

        Connection connection = (Connection) ctx.pipeline().get("packet_handler");
        ServerPlayer player = connection.getPlayer();
        if (config.enableBypassPermission() && player.getBukkitEntity().hasPermission("healthider.bypass")) {
            this.writeId(ctx, packet, buf);
            packet.write(buf);
            return;
        }

        float health = (float) healthValue.value();
        float shownHealth;
        if (health <= 0.0F) {
            shownHealth = health;
        } else {
            shownHealth = 1F;
        }

        SynchedEntityData.DataValue<?> dataValue = new SynchedEntityData.DataValue<>(DATA_HEALTH_ID.getId(), DATA_HEALTH_ID.getSerializer(), shownHealth);

        packed.set(i, dataValue);
        this.writeId(ctx, packet, buf);
        packet.write(buf);
    }

    private void writeId(final ChannelHandlerContext ctx, final Packet<?> packet, final FriendlyByteBuf buf) {
        buf.writeVarInt(ctx.channel().attr(Connection.ATTRIBUTE_PROTOCOL).get().getPacketId(PacketFlow.CLIENTBOUND, packet));
    }

}
