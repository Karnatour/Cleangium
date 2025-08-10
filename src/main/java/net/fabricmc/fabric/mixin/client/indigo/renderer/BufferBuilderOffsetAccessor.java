package net.fabricmc.fabric.mixin.client.indigo.renderer;

import net.minecraft.client.renderer.BufferBuilder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(BufferBuilder.class)
public interface BufferBuilderOffsetAccessor {
    @Accessor("xOffset")
    double getXOffset();

    @Accessor("yOffset")
    double getYOffset();

    @Accessor("zOffset")
    double getZOffset();
}
