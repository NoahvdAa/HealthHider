package me.noahvdaa.healthhider;

import io.papermc.paper.network.ChannelInitializeListenerHolder;
import net.kyori.adventure.key.Key;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.SimplePie;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class HealthHider extends JavaPlugin {

    private static final int BSTATS_ID = 17377;
    private static final Key LISTENER_KEY = Key.key("healthider", "initializelistener");
    private HHConfig configuration;

    @Override
    public void onEnable() {
        this.saveDefaultConfig();

        this.loadConfig();

        ChannelInitializeListenerHolder.addListener(LISTENER_KEY, (channel) -> channel.pipeline().addBefore("unbundler", "healthider_handler", new HHHandler(this)));

        Metrics metrics = new Metrics(this, BSTATS_ID);
        metrics.addCustomChart(new SimplePie("bypass-permission", () -> this.configuration.enableBypassPermission() ? "Yes" : "No"));
        metrics.addCustomChart(new SimplePie("list-mode", () -> this.configuration.whitelistMode() ? "Whitelist" : "Blacklist"));
    }

    @Override
    public void onDisable() {
        ChannelInitializeListenerHolder.removeListener(LISTENER_KEY);
    }

    private void loadConfig() {
        FileConfiguration config = this.getConfig();

        String listMode = config.getString("list-mode", "blacklist").toLowerCase();
        if (!listMode.equals("blacklist") && !listMode.equals("whitelist")) {
            this.getLogger().warning("Unknown list mode '" + listMode + "'. Falling back to blacklist...");
            listMode = "blacklist";
        }

        Set<EntityType<?>> entities = new HashSet<>();
        for (String entityName : config.getStringList("entities")) {
            ResourceLocation key = ResourceLocation.tryParse(entityName);
            if (key == null) {
                this.getLogger().warning("Invalid resource key '" + entityName + "'");
                continue;
            }

            EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.getOptional(key).orElse(null);
            if (type == null) {
                this.getLogger().warning("Unknown entity type '" + entityName + "'");
                continue;
            }

            if (!type.getBaseClass().isAssignableFrom(LivingEntity.class)) {
                this.getLogger().warning("Entity '" + entityName + "' is not a living entity and can't have health!");
                continue;
            }

            entities.add(type);
        }

        this.configuration = new HHConfig(
            config.getBoolean("enable-bypass-permission", false),
            listMode.equals("whitelist"),
            entities
        );
    }

    public HHConfig configuration() {
        return this.configuration;
    }

}
