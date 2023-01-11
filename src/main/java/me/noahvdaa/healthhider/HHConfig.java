package me.noahvdaa.healthhider;

import net.minecraft.world.entity.EntityType;

import java.util.List;

public record HHConfig(boolean enableBypassPermission, boolean whitelistMode, List<EntityType<?>> entities) {
}
