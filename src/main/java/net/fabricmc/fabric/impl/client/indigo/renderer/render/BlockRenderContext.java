/*
 * Copyright (c) 2016, 2017, 2018, 2019 FabricMC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.fabricmc.fabric.impl.client.indigo.renderer.render;

import java.util.Random;
import java.util.function.Consumer;

import it.unimi.dsi.fastutil.ints.Int2ObjectFunction;

import net.fabricmc.fabric.mixin.client.indigo.renderer.BufferBuilderOffsetAccessor;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.BlockModelRenderer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.util.math.BlockPos;

import net.fabricmc.fabric.api.renderer.v1.mesh.Mesh;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.fabricmc.fabric.impl.client.indigo.renderer.accessor.AccessBufferBuilder;
import net.fabricmc.fabric.impl.client.indigo.renderer.aocalc.AoCalculator;
import net.fabricmc.fabric.impl.client.indigo.renderer.aocalc.AoLuminanceFix;
import net.fabricmc.fabric.impl.client.indigo.renderer.mesh.MutableQuadViewImpl;
import net.minecraft.world.World;

/**
 * Context for non-terrain block rendering.
 */
public class BlockRenderContext extends AbstractRenderContext implements RenderContext {
	private final BlockRenderInfo blockInfo = new BlockRenderInfo();
	private final AoCalculator aoCalc = new AoCalculator(blockInfo, this::brightness, this::aoLevel);
	private final MeshConsumer meshConsumer = new MeshConsumer(blockInfo, this::outputBuffer, aoCalc, this::transform);
	private final Random random = new Random();
	private BlockModelRenderer vanillaRenderer;
	private AccessBufferBuilder fabricBuffer;
	private long seed;
	private boolean isCallingVanilla = false;
	private boolean didOutput = false;

	private double offsetX;
	private double offsetY;
	private double offsetZ;

	public boolean isCallingVanilla() {
		return isCallingVanilla;
	}

	private int brightness(BlockPos pos) {
		if (blockInfo.blockView == null) {
			return 15 << 20 | 15 << 4;
		}
		System.out.println("Called which meh for me");
		return blockInfo.blockView.getLightFromNeighbors(pos);
	}

	private float aoLevel(BlockPos pos) {
		final World blockView = blockInfo.blockView;
		return blockView == null ? 1f : AoLuminanceFix.INSTANCE.apply(blockView, pos);
	}

	private AccessBufferBuilder outputBuffer(int renderLayer) {
		didOutput = true;
		return fabricBuffer;
	}

	public boolean tesselate(BlockModelRenderer vanillaRenderer, World blockView, IBakedModel model, IBlockState state, BlockPos pos, BufferBuilder buffer, long seed) {
		this.vanillaRenderer = vanillaRenderer;
		this.fabricBuffer = (AccessBufferBuilder) buffer;
		this.seed = seed;
		this.didOutput = false;
		aoCalc.clear();
		blockInfo.setBlockView(blockView);
		blockInfo.prepareForBlock(state, pos, model.isAmbientOcclusion());
		setupOffsets();

		((FabricBakedModel) model).emitBlockQuads(blockView, state, pos, blockInfo.randomSupplier, this);

		this.vanillaRenderer = null;
		blockInfo.release();
		this.fabricBuffer = null;
		return didOutput;
	}

	protected void acceptVanillaModel(IBakedModel model) {
		isCallingVanilla = true;
		didOutput = didOutput && vanillaRenderer.renderModel(blockInfo.blockView, model, blockInfo.blockState, blockInfo.blockPos, (BufferBuilder) fabricBuffer, false, seed);
		isCallingVanilla = false;
	}

	private void setupOffsets() {
		final BufferBuilderOffsetAccessor buffer = (BufferBuilderOffsetAccessor) fabricBuffer;
		final BlockPos pos = blockInfo.blockPos;
		offsetX = buffer.getXOffset() + pos.getX();
		offsetY = buffer.getYOffset() + pos.getY();
		offsetZ = buffer.getZOffset() + pos.getZ();
	}


	private class MeshConsumer extends AbstractMeshConsumer {
		MeshConsumer(BlockRenderInfo blockInfo, Int2ObjectFunction<AccessBufferBuilder> bufferFunc, AoCalculator aoCalc, QuadTransform transform) {
			super(blockInfo, bufferFunc, aoCalc, transform);
		}

		@Override
		protected void applyOffsets(MutableQuadViewImpl q) {
			final double x = offsetX;
			final double y = offsetY;
			final double z = offsetZ;

			for (int i = 0; i < 4; i++) {
				q.pos(i, (float) (q.x(i) + x), (float) (q.y(i) + y), (float) (q.z(i) + z));
			}
		}
	}

	@Override
	public Consumer<Mesh> meshConsumer() {
		return meshConsumer;
	}

	@Override
	public Consumer<IBakedModel> fallbackConsumer() {
		return this::acceptVanillaModel;
	}

	@Override
	public QuadEmitter getEmitter() {
		return meshConsumer.getEmitter();
	}
}
