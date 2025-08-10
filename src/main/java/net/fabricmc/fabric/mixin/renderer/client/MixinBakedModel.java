package net.fabricmc.fabric.mixin.renderer.client;

import java.util.Random;
import java.util.function.Supplier;

import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;

import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;

@Mixin(IBakedModel.class)
public interface MixinBakedModel extends FabricBakedModel {

	@Override
	default boolean isVanillaAdapter() {
		return true;
	}

	@Override
	default void emitBlockQuads(World blockView, IBlockState state, BlockPos pos, Supplier<Random> randomSupplier, RenderContext context) {
		context.fallbackConsumer().accept((IBakedModel) this);
	}

	@Override
	default void emitItemQuads(ItemStack stack, Supplier<Random> randomSupplier, RenderContext context) {
		context.fallbackConsumer().accept((IBakedModel) this);
	}
}
