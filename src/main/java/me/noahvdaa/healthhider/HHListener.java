package me.noahvdaa.healthhider;

import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import org.bukkit.craftbukkit.v1_20_R3.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_20_R3.entity.CraftPlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.spigotmc.event.entity.EntityMountEvent;

import java.util.List;

import static net.minecraft.world.entity.LivingEntity.DATA_HEALTH_ID;

public class HHListener implements Listener {

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    // we only want to resend health if the player actually starts riding
    public void onEntityMount(EntityMountEvent e) {
        if (!(e.getEntity() instanceof CraftPlayer player) || !(e.getMount() instanceof CraftLivingEntity entity)) {
            return;
        }

        ClientboundSetEntityDataPacket packet = new ClientboundSetEntityDataPacket(entity.getEntityId(), List.of(
            new SynchedEntityData.DataValue<>(DATA_HEALTH_ID.getId(), EntityDataSerializers.FLOAT, entity.getHandle().getHealth())
        ));
        player.getHandle().connection.send(packet);

    }

}
