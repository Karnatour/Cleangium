package net.fabricmc.fabric.impl.client.indigo.renderer.render;

import java.util.Random;
import java.util.function.Supplier;

import net.minecraft.block.material.MapColor;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.color.BlockColors;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlockRenderInfo {
	private final BlockColors blockColorMap = Minecraft.getMinecraft().getBlockColors();
	public final Random random = new Random();
	public World blockView;
	public BlockPos blockPos;
	public IBlockState blockState;
	public long seed;
	boolean defaultAo;
	int defaultLayerIndex;

	public static long getRenderingSeed(BlockPos pos) {
		long seed = (long) pos.getX() * 3129871L ^ (long) pos.getZ() * 116129781L ^ (long) pos.getY();
		seed = seed * seed * 42317861L + seed * 11L;
		return seed;
	}

	public final Supplier<Random> randomSupplier = () -> {
		final Random result = random;
		long seed = this.seed;

		if (seed == -1L) {
			seed = getRenderingSeed(blockPos);
			this.seed = seed;
		}

		result.setSeed(seed);
		return result;
	};

	public void setBlockView(World blockView) {
		this.blockView = blockView;
	}

	public void prepareForBlock(IBlockState blockState, BlockPos blockPos, boolean modelAO) {
		this.blockPos = blockPos;
		this.blockState = blockState;
		seed = -1L;
		defaultAo = modelAO && Minecraft.isAmbientOcclusionEnabled() && blockState.getLightValue(blockView,blockPos) == 0;
		defaultLayerIndex = blockState.getBlock().getRenderLayer().ordinal();
	}

	public void release() {
		blockPos = null;
		blockState = null;
	}

	int blockColor(int colorIndex) {
		int color = blockColorMap.colorMultiplier(blockState, blockView, blockPos, colorIndex);
		return 0xFF000000 | color;
	}

	boolean shouldDrawFace(EnumFacing face) {
		return true;
	}

	int layerIndexOrDefault(BlockRenderLayer layer) {
		return layer == null ? this.defaultLayerIndex : layer.ordinal();
	}
}
