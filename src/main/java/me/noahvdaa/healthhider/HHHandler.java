package me.noahvdaa.healthhider;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import static net.minecraft.world.entity.LivingEntity.DATA_HEALTH_ID;

public class HHHandler extends MessageToMessageEncoder<Packet<?>> {

    private final HealthHider plugin;
    private static final EntityDataAccessor<Float> DATA_PLAYER_ABSORPTION_ID;

    public HHHandler(HealthHider plugin) {
        this.plugin = plugin;
    }

    static {
        try {
            Field field = Player.class.getDeclaredField("e"); // CHANGE ON UPDATE/MOJMAP
            field.setAccessible(true);
            DATA_PLAYER_ABSORPTION_ID = (EntityDataAccessor<Float>) field.get(null);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean acceptOutboundMessage(Object msg) {
        return msg instanceof ClientboundSetEntityDataPacket;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, Packet<?> msg, List<Object> out) {
        ClientboundSetEntityDataPacket packet = (ClientboundSetEntityDataPacket) msg;

        Connection connection = (Connection) ctx.pipeline().get("packet_handler");
        ServerPlayer player = connection.getPlayer();

        if (!shouldObfuscate(player, packet)) {
            out.add(msg);
            return;
        }

        List<SynchedEntityData.DataValue<?>> packed = new ArrayList<>(packet.packedItems());
        packed.replaceAll((dataValue) -> {
            int id = dataValue.id();
            if (id == DATA_HEALTH_ID.getId()) {
                float health = (float) dataValue.value();
                float shownHealth;
                if (health <= 0.0F) {
                    shownHealth = health;
                } else {
                    shownHealth = 1F;
                }
                return new SynchedEntityData.DataValue<>(id, EntityDataSerializers.FLOAT, shownHealth);
            }
            if (id == DATA_PLAYER_ABSORPTION_ID.getId()) {
                return new SynchedEntityData.DataValue<>(id, EntityDataSerializers.FLOAT, 0F);
            }

            return dataValue;
        });

        out.add(new ClientboundSetEntityDataPacket(packet.id(), packed));
    }

    private boolean shouldObfuscate(ServerPlayer player, ClientboundSetEntityDataPacket packet) {
        if (player == null || packet.id() == player.getId()) {
            // We don't want to hide our own health
            return false;
        }

        Entity entity = player.serverLevel().getEntityLookup().get(packet.id());

        if (!(entity instanceof LivingEntity)) {
            // Only living entities have health
            return false;
        }

        HHConfig config = plugin.configuration();
        if (config.entities().contains(entity.getType()) != config.whitelistMode()) {
            // We don't need to censor this entity
            return false;
        }

        if (entity.hasPassenger(player)) {
            // Don't obfuscate the health of the entity we're on
            return false;
        }

        if (config.enableBypassPermission() && player.getBukkitEntity().hasPermission("healthider.bypass")) {
            return false;
        }

        return true;
    }

}
