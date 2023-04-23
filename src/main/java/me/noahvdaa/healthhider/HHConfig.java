package me.noahvdaa.healthhider;

import net.minecraft.world.entity.EntityType;

import java.util.Set;

public record HHConfig(boolean enableBypassPermission, boolean whitelistMode, Set<EntityType<?>> entities) {
}
