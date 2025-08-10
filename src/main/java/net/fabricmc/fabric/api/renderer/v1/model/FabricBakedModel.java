package net.fabricmc.fabric.api.renderer.v1.model;

import java.util.Random;
import java.util.function.Supplier;

import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;

import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.minecraft.world.World;

public interface FabricBakedModel {
	boolean isVanillaAdapter();

	void emitBlockQuads(World blockView, IBlockState state, BlockPos pos, Supplier<Random> randomSupplier, RenderContext context);

	void emitItemQuads(ItemStack stack, Supplier<Random> randomSupplier, RenderContext context);
}
