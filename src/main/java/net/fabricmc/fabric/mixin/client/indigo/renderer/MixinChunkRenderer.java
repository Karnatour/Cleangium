package net.fabricmc.fabric.mixin.client.indigo.renderer;

import net.fabricmc.fabric.impl.client.indigo.renderer.Indigo;
import net.fabricmc.fabric.impl.client.indigo.renderer.render.TerrainRenderContext;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.chunk.ChunkCompileTaskGenerator;
import net.minecraft.client.renderer.chunk.CompiledChunk;
import net.minecraft.client.renderer.chunk.RenderChunk;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.world.ChunkCache;
import net.minecraft.world.IBlockAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.util.math.BlockPos;

import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel;
import net.fabricmc.fabric.impl.client.indigo.renderer.accessor.AccessChunkRenderer;

@Mixin(RenderChunk.class)
public abstract class MixinChunkRenderer implements AccessChunkRenderer {
    @Shadow
    private BlockPos.MutableBlockPos position;

    @Shadow
    private ChunkCache worldView;

    @Shadow
    protected abstract void preRenderBlocks(BufferBuilder bufferBuilder, BlockPos blockPos);

    @Shadow
    protected abstract void postRenderBlocks(BlockRenderLayer layer, float x, float y, float z, BufferBuilder bufferBuilder, CompiledChunk compiledChunk);

    @Shadow
    public CompiledChunk compiledChunk;

    private TerrainRenderContext currentRenderer;

    @Override
    public void fabric_setRenderer(TerrainRenderContext renderer) {
        this.currentRenderer = renderer;
    }

    @Override
    public TerrainRenderContext fabric_getRenderer() {
        return this.currentRenderer;
    }

    @Override
    public void fabric_beginBufferBuilding(BufferBuilder bufferBuilder_1, BlockPos blockPos_1) {
        preRenderBlocks(bufferBuilder_1, blockPos_1);
    }

    // Set up the world view and get the renderer ready
    @Inject(at = @At("TAIL"), method = "rebuildWorldView")
    private void chunkDataHook(CallbackInfo ci) {
        final ChunkCache chunkCache = worldView;

        if (chunkCache != null && currentRenderer == null) {
            currentRenderer = new TerrainRenderContext();
            currentRenderer.setBlockView(chunkCache.world);
        }
    }

    @Inject(method = "rebuildChunk",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/Minecraft;getBlockRendererDispatcher()Lnet/minecraft/client/renderer/BlockRendererDispatcher;"),
            require = 1)
    private void hookPrepareRenderer(float x, float y, float z, ChunkCompileTaskGenerator generator, CallbackInfo ci) {
        if (currentRenderer != null && !this.worldView.isEmpty()) {
            //System.err.println("ChunkRendererMixin: null chunk task in hookResultFlagsAndPrepare!");
            currentRenderer.setChunkTask(generator);
        }
    }

    @ModifyVariable(method = "rebuildChunk",
            at = @At(value = "STORE"),
            ordinal = 0,
            require = 1)
    private boolean[] hookResultFlagsAndPrepare(boolean[] flagsIn, float x, float y, float z, ChunkCompileTaskGenerator generator) {
        if (currentRenderer != null && !this.worldView.isEmpty()) {
            if (currentRenderer.chunkTask == null) {
                //System.err.println("ChunkRendererMixin: null chunk task in hookResultFlagsAndPrepare!");
                currentRenderer.setChunkTask(generator);
            }
            currentRenderer.prepare((RenderChunk) (Object) this, position, flagsIn);
        }
        return flagsIn;
    }

    @Redirect(
            method = "rebuildChunk",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/BlockRendererDispatcher;renderBlock(Lnet/minecraft/block/state/IBlockState;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/world/IBlockAccess;Lnet/minecraft/client/renderer/BufferBuilder;)Z"
            )
    )
    private boolean hookChunkBuildTesselate(BlockRendererDispatcher dispatcher, IBlockState state, BlockPos pos, IBlockAccess world, BufferBuilder buffer) {
        if (state.getRenderType() == EnumBlockRenderType.MODEL) {
            IBakedModel model = dispatcher.getModelForState(state);

            if (Indigo.ALWAYS_TESSELATE_INDIGO || !((FabricBakedModel) model).isVanillaAdapter()) {
                // use prepared renderer if available
                if (currentRenderer != null) {
                    return currentRenderer.tesselateBlock(state, pos, model);
                }
            }
        }

        return dispatcher.renderBlock(state, pos, world, buffer);
    }

    @Inject(at = @At("RETURN"), method = "rebuildChunk")
    private void hookRebuildChunkReturn(float x, float y, float z, ChunkCompileTaskGenerator generator, CallbackInfo ci) {
        // release renderer back
        if (currentRenderer != null) {
            currentRenderer.release();
        }
    }
}