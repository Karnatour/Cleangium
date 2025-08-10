package net.fabricmc.fabric.impl.client.indigo.renderer.render;

import it.unimi.dsi.fastutil.longs.Long2FloatOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import net.fabricmc.fabric.impl.client.indigo.renderer.Indigo;
import net.fabricmc.fabric.impl.client.indigo.renderer.accessor.AccessBufferBuilder;
import net.fabricmc.fabric.impl.client.indigo.renderer.accessor.AccessChunkRenderer;
import net.fabricmc.fabric.impl.client.indigo.renderer.mesh.MutableQuadViewImpl;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.chunk.ChunkCompileTaskGenerator;
import net.minecraft.client.renderer.chunk.CompiledChunk;
import net.minecraft.client.renderer.chunk.RenderChunk;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.math.BlockPos;

import net.fabricmc.fabric.impl.client.indigo.renderer.aocalc.AoLuminanceFix;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class ChunkRenderInfo {
    private final Long2IntOpenHashMap brightnessCache;
    private final Long2FloatOpenHashMap aoLevelCache;

    private final BlockRenderInfo blockInfo;
    private BlockPos chunkOrigin;
    private RenderChunk renderChunk;
    private ChunkCompileTaskGenerator chunkTask;
    private CompiledChunk compiledChunk;
    boolean[] resultFlags;

    World blockView;

    private final AccessBufferBuilder[] buffers = new AccessBufferBuilder[4];
    private final BlockRenderLayer[] LAYERS = BlockRenderLayer.values();

    private double chunkOffsetX;
    private double chunkOffsetY;
    private double chunkOffsetZ;

    private float offsetX = 0;
    private float offsetY = 0;
    private float offsetZ = 0;


    ChunkRenderInfo(BlockRenderInfo blockInfo) {
        this.blockInfo = blockInfo;
        brightnessCache = new Long2IntOpenHashMap();
        brightnessCache.defaultReturnValue(Integer.MAX_VALUE);
        aoLevelCache = new Long2FloatOpenHashMap();
        aoLevelCache.defaultReturnValue(Float.MAX_VALUE);
    }

    void setBlockView(World blockView) {
        this.blockView = blockView;
    }

    void setChunkTask(ChunkCompileTaskGenerator chunkTask){
        this.chunkTask = chunkTask;
    }

    public void prepare(RenderChunk renderChunk, BlockPos.MutableBlockPos chunkOrigin, boolean[] resultFlags) {
        this.renderChunk = renderChunk;
        this.chunkOrigin = chunkOrigin;
        this.resultFlags = resultFlags;
        this.compiledChunk = null;
        buffers[0] = null;
        buffers[1] = null;
        buffers[2] = null;
        buffers[3] = null;
        chunkOffsetX = -chunkOrigin.getX();
        chunkOffsetY = -chunkOrigin.getY();
        chunkOffsetZ = -chunkOrigin.getZ();
        brightnessCache.clear();
        aoLevelCache.clear();
    }

    void beginBlock() {
        final IBlockState blockState = blockInfo.blockState;
        final BlockPos blockPos = blockInfo.blockPos;

        // When we are using the BufferBuilder input methods, the builder will
        // add the chunk offset for us, so we should only apply the block offset.
        if (Indigo.ENSURE_VERTEX_FORMAT_COMPATIBILITY) {
            offsetX = (blockPos.getX());
            offsetY = (blockPos.getY());
            offsetZ = (blockPos.getZ());
        } else {
            offsetX = (float) (chunkOffsetX + blockPos.getX());
            offsetY = (float) (chunkOffsetY + blockPos.getY());
            offsetZ = (float) (chunkOffsetZ + blockPos.getZ());
        }

        if (blockState.getBlock().getOffsetType() != Block.EnumOffsetType.NONE) {
            Vec3d offset = blockState.getOffset(blockInfo.blockView, blockPos);
            offsetX += (float) offset.x;
            offsetY += (float) offset.y;
            offsetZ += (float) offset.z;
        }
    }

    void release() {
        renderChunk = null;
        compiledChunk = null;
        chunkTask = null;
        chunkOrigin = null;
        buffers[0] = null;
        buffers[1] = null;
        buffers[2] = null;
        buffers[3] = null;
    }

    public AccessBufferBuilder getInitializedBuffer(int layerIndex) {
        resultFlags[layerIndex] = true;

        AccessBufferBuilder result = buffers[layerIndex];

        if (result == null) {
            BufferBuilder builder = chunkTask.getRegionRenderCacheBuilder().getWorldRendererByLayerId(layerIndex);
            buffers[layerIndex] = (AccessBufferBuilder) builder;
            BlockRenderLayer layer = LAYERS[layerIndex];

            compiledChunk = chunkTask.getCompiledChunk();

            if (!compiledChunk.isLayerStarted(layer)) {
                compiledChunk.setLayerStarted(layer);
               ((AccessChunkRenderer) renderChunk).fabric_beginBufferBuilding(builder, chunkOrigin);
            }

            result = (AccessBufferBuilder) builder;
        }

        return result;
    }

    void applyOffsets(MutableQuadViewImpl q) {
        for (int i = 0; i < 4; i++) {
            q.pos(i, q.x(i) + offsetX, q.y(i) + offsetY, q.z(i) + offsetZ);
        }
    }

    int cachedBrightness(BlockPos pos) {
        long key = pos.toLong();
        int result = brightnessCache.get(key);

        if (result == Integer.MAX_VALUE) {
            result = blockView.getBlockState(pos).getLightValue(blockView, pos);
            brightnessCache.put(key, result);
        }

        return result;
    }

    float cachedAoLevel(BlockPos pos) {
        long key = pos.toLong();
        float result = aoLevelCache.get(key);

        if (result == Float.MAX_VALUE) {
            result = AoLuminanceFix.INSTANCE.apply(this.renderChunk.getWorld(),pos);
            aoLevelCache.put(key, result);
        }

        return result;
    }

    public BlockPos getChunkOrigin() {
        return chunkOrigin;
    }
}
