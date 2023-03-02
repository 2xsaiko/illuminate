package net.dblsaiko.illuminate.init;

import net.dblsaiko.illuminate.Illuminate;
import net.dblsaiko.illuminate.client.LightSource;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public final class EntityTypes {
    private EntityType<LightSource> lightSourceType;

    @NotNull
    public EntityType<LightSource> lightSourceType() {
        return Objects.requireNonNull(this.lightSourceType);
    }

    public void register() {
        this.lightSourceType = Registry.register(
                Registries.ENTITY_TYPE,
                Illuminate.id("light"),
                FabricEntityTypeBuilder.<LightSource>create().dimensions(EntityDimensions.fixed(0.6f, 0.6f)).build()
        );
    }
}
