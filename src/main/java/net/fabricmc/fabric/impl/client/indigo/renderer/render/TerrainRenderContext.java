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

import java.util.function.Consumer;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.chunk.ChunkCompileTaskGenerator;
import net.minecraft.client.renderer.chunk.CompiledChunk;
import net.minecraft.client.renderer.chunk.RenderChunk;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.util.ReportedException;
import net.minecraft.util.math.BlockPos;

import net.fabricmc.fabric.api.renderer.v1.mesh.Mesh;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.fabricmc.fabric.impl.client.indigo.renderer.aocalc.AoCalculator;
import net.minecraft.world.World;

/**
 * Implementation of {@link RenderContext} used during terrain rendering.
 * Dispatches calls from models during chunk rebuild to the appropriate consumer,
 * and holds/manages all of the state needed by them.
 */
public class TerrainRenderContext extends AbstractRenderContext implements RenderContext {
	public static final ThreadLocal<TerrainRenderContext> POOL = ThreadLocal.withInitial(TerrainRenderContext::new);
	private final TerrainBlockRenderInfo blockInfo = new TerrainBlockRenderInfo();
	private final ChunkRenderInfo chunkInfo = new ChunkRenderInfo(blockInfo);
	private final AoCalculator aoCalc = new AoCalculator(blockInfo, chunkInfo::cachedBrightness, chunkInfo::cachedAoLevel);
	private final TerrainMeshConsumer meshConsumer = new TerrainMeshConsumer(blockInfo, chunkInfo, aoCalc, this::transform);
	private final TerrainFallbackConsumer fallbackConsumer = new TerrainFallbackConsumer(blockInfo, chunkInfo, aoCalc, this::transform);
	public  ChunkCompileTaskGenerator chunkTask = null;

	public void setBlockView(World blockView) {
		blockInfo.setBlockView(blockView);
		chunkInfo.setBlockView(blockView);
	}

	public void setChunkTask(ChunkCompileTaskGenerator chunkTask){
		chunkInfo.setChunkTask(chunkTask);
		this.chunkTask = chunkTask;
	}

	public TerrainRenderContext prepare(RenderChunk chunkRenderer, BlockPos.MutableBlockPos chunkOrigin, boolean[] resultFlags) {
		chunkInfo.prepare(chunkRenderer, chunkOrigin, resultFlags);
		return this;
	}

	public void release() {
		chunkInfo.release();
		blockInfo.release();
	}

	/** Called from chunk renderer hook. */
	public boolean tesselateBlock(IBlockState blockState, BlockPos blockPos, final IBakedModel model) {
		try {
			aoCalc.clear();
			blockInfo.prepareForBlock(blockState, blockPos, model.isAmbientOcclusion());
			chunkInfo.beginBlock();
			((FabricBakedModel) model).emitBlockQuads(blockInfo.blockView, blockInfo.blockState, blockInfo.blockPos, blockInfo.randomSupplier, this);
		} catch (Throwable var9) {
			CrashReport crashreport = CrashReport.makeCrashReport(var9, "Tesselating block model");
			CrashReportCategory crashreportcategory = crashreport.makeCategory("Block model being tesselated");
			CrashReportCategory.addBlockInfo(crashreportcategory, blockPos, blockState);

			throw new ReportedException(crashreport);
		}

		return chunkInfo.resultFlags[blockInfo.defaultLayerIndex];
	}

	@Override
	public Consumer<Mesh> meshConsumer() {
		return meshConsumer;
	}

	@Override
	public Consumer<IBakedModel> fallbackConsumer() {
		return fallbackConsumer;
	}

	@Override
	public QuadEmitter getEmitter() {
		return meshConsumer.getEmitter();
	}
}
