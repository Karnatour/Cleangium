package net.fabricmc.fabric.impl.client.indigo.renderer.aocalc;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

import java.util.BitSet;

class VanillaAoCalc {
    private final float[] vertexColorMultiplier = new float[4];
    private final int[] vertexBrightness = new int[4];

    public void updateVertexBrightness(IBlockAccess worldIn, IBlockState state, BlockPos centerPos, EnumFacing direction, float[] faceShape, BitSet shapeState) {
        BlockPos blockpos = shapeState.get(0) ? centerPos.offset(direction) : centerPos;
        BlockPos.PooledMutableBlockPos blockpos$pooledmutableblockpos = BlockPos.PooledMutableBlockPos.retain();
        VanillaAoCalc.EnumNeighborInfo blockmodelrenderer$enumneighborinfo = VanillaAoCalc.EnumNeighborInfo.getNeighbourInfo(direction);
        BlockPos.PooledMutableBlockPos blockpos$pooledmutableblockpos1 = BlockPos.PooledMutableBlockPos.retain(blockpos).move(blockmodelrenderer$enumneighborinfo.corners[0]);
        BlockPos.PooledMutableBlockPos blockpos$pooledmutableblockpos2 = BlockPos.PooledMutableBlockPos.retain(blockpos).move(blockmodelrenderer$enumneighborinfo.corners[1]);
        BlockPos.PooledMutableBlockPos blockpos$pooledmutableblockpos3 = BlockPos.PooledMutableBlockPos.retain(blockpos).move(blockmodelrenderer$enumneighborinfo.corners[2]);
        BlockPos.PooledMutableBlockPos blockpos$pooledmutableblockpos4 = BlockPos.PooledMutableBlockPos.retain(blockpos).move(blockmodelrenderer$enumneighborinfo.corners[3]);
        int i = state.getPackedLightmapCoords(worldIn, blockpos$pooledmutableblockpos1);
        int j = state.getPackedLightmapCoords(worldIn, blockpos$pooledmutableblockpos2);
        int k = state.getPackedLightmapCoords(worldIn, blockpos$pooledmutableblockpos3);
        int l = state.getPackedLightmapCoords(worldIn, blockpos$pooledmutableblockpos4);
        float f = worldIn.getBlockState(blockpos$pooledmutableblockpos1).getAmbientOcclusionLightValue();
        float f1 = worldIn.getBlockState(blockpos$pooledmutableblockpos2).getAmbientOcclusionLightValue();
        float f2 = worldIn.getBlockState(blockpos$pooledmutableblockpos3).getAmbientOcclusionLightValue();
        float f3 = worldIn.getBlockState(blockpos$pooledmutableblockpos4).getAmbientOcclusionLightValue();
        boolean flag = worldIn.getBlockState(blockpos$pooledmutableblockpos.setPos(blockpos$pooledmutableblockpos1).move(direction)).isTranslucent();
        boolean flag1 = worldIn.getBlockState(blockpos$pooledmutableblockpos.setPos(blockpos$pooledmutableblockpos2).move(direction)).isTranslucent();
        boolean flag2 = worldIn.getBlockState(blockpos$pooledmutableblockpos.setPos(blockpos$pooledmutableblockpos3).move(direction)).isTranslucent();
        boolean flag3 = worldIn.getBlockState(blockpos$pooledmutableblockpos.setPos(blockpos$pooledmutableblockpos4).move(direction)).isTranslucent();
        float f4;
        int i1;
        if (!flag2 && !flag) {
            f4 = f;
            i1 = i;
        } else {
            BlockPos blockpos1 = blockpos$pooledmutableblockpos.setPos(blockpos$pooledmutableblockpos1).move(blockmodelrenderer$enumneighborinfo.corners[2]);
            f4 = worldIn.getBlockState(blockpos1).getAmbientOcclusionLightValue();
            i1 = state.getPackedLightmapCoords(worldIn, blockpos1);
        }

        int j1;
        float f5;
        if (!flag3 && !flag) {
            f5 = f;
            j1 = i;
        } else {
            BlockPos blockpos2 = blockpos$pooledmutableblockpos.setPos(blockpos$pooledmutableblockpos1).move(blockmodelrenderer$enumneighborinfo.corners[3]);
            f5 = worldIn.getBlockState(blockpos2).getAmbientOcclusionLightValue();
            j1 = state.getPackedLightmapCoords(worldIn, blockpos2);
        }

        int k1;
        float f6;
        if (!flag2 && !flag1) {
            f6 = f1;
            k1 = j;
        } else {
            BlockPos blockpos3 = blockpos$pooledmutableblockpos.setPos(blockpos$pooledmutableblockpos2).move(blockmodelrenderer$enumneighborinfo.corners[2]);
            f6 = worldIn.getBlockState(blockpos3).getAmbientOcclusionLightValue();
            k1 = state.getPackedLightmapCoords(worldIn, blockpos3);
        }

        int l1;
        float f7;
        if (!flag3 && !flag1) {
            f7 = f1;
            l1 = j;
        } else {
            BlockPos blockpos4 = blockpos$pooledmutableblockpos.setPos(blockpos$pooledmutableblockpos2).move(blockmodelrenderer$enumneighborinfo.corners[3]);
            f7 = worldIn.getBlockState(blockpos4).getAmbientOcclusionLightValue();
            l1 = state.getPackedLightmapCoords(worldIn, blockpos4);
        }

        int i3 = state.getPackedLightmapCoords(worldIn, centerPos);
        if (shapeState.get(0) || !worldIn.getBlockState(centerPos.offset(direction)).isOpaqueCube()) {
            i3 = state.getPackedLightmapCoords(worldIn, centerPos.offset(direction));
        }

        float f8 = shapeState.get(0) ? worldIn.getBlockState(blockpos).getAmbientOcclusionLightValue() : worldIn.getBlockState(centerPos).getAmbientOcclusionLightValue();
        VanillaAoCalc.VertexTranslations blockmodelrenderer$vertextranslations = VanillaAoCalc.VertexTranslations.getVertexTranslations(direction);
        blockpos$pooledmutableblockpos.release();
        blockpos$pooledmutableblockpos1.release();
        blockpos$pooledmutableblockpos2.release();
        blockpos$pooledmutableblockpos3.release();
        blockpos$pooledmutableblockpos4.release();
        if (shapeState.get(1) && blockmodelrenderer$enumneighborinfo.doNonCubicWeight) {
            float f29 = (f3 + f + f5 + f8) * 0.25F;
            float f30 = (f2 + f + f4 + f8) * 0.25F;
            float f31 = (f2 + f1 + f6 + f8) * 0.25F;
            float f32 = (f3 + f1 + f7 + f8) * 0.25F;
            float f13 = faceShape[blockmodelrenderer$enumneighborinfo.vert0Weights[0].shape] * faceShape[blockmodelrenderer$enumneighborinfo.vert0Weights[1].shape];
            float f14 = faceShape[blockmodelrenderer$enumneighborinfo.vert0Weights[2].shape] * faceShape[blockmodelrenderer$enumneighborinfo.vert0Weights[3].shape];
            float f15 = faceShape[blockmodelrenderer$enumneighborinfo.vert0Weights[4].shape] * faceShape[blockmodelrenderer$enumneighborinfo.vert0Weights[5].shape];
            float f16 = faceShape[blockmodelrenderer$enumneighborinfo.vert0Weights[6].shape] * faceShape[blockmodelrenderer$enumneighborinfo.vert0Weights[7].shape];
            float f17 = faceShape[blockmodelrenderer$enumneighborinfo.vert1Weights[0].shape] * faceShape[blockmodelrenderer$enumneighborinfo.vert1Weights[1].shape];
            float f18 = faceShape[blockmodelrenderer$enumneighborinfo.vert1Weights[2].shape] * faceShape[blockmodelrenderer$enumneighborinfo.vert1Weights[3].shape];
            float f19 = faceShape[blockmodelrenderer$enumneighborinfo.vert1Weights[4].shape] * faceShape[blockmodelrenderer$enumneighborinfo.vert1Weights[5].shape];
            float f20 = faceShape[blockmodelrenderer$enumneighborinfo.vert1Weights[6].shape] * faceShape[blockmodelrenderer$enumneighborinfo.vert1Weights[7].shape];
            float f21 = faceShape[blockmodelrenderer$enumneighborinfo.vert2Weights[0].shape] * faceShape[blockmodelrenderer$enumneighborinfo.vert2Weights[1].shape];
            float f22 = faceShape[blockmodelrenderer$enumneighborinfo.vert2Weights[2].shape] * faceShape[blockmodelrenderer$enumneighborinfo.vert2Weights[3].shape];
            float f23 = faceShape[blockmodelrenderer$enumneighborinfo.vert2Weights[4].shape] * faceShape[blockmodelrenderer$enumneighborinfo.vert2Weights[5].shape];
            float f24 = faceShape[blockmodelrenderer$enumneighborinfo.vert2Weights[6].shape] * faceShape[blockmodelrenderer$enumneighborinfo.vert2Weights[7].shape];
            float f25 = faceShape[blockmodelrenderer$enumneighborinfo.vert3Weights[0].shape] * faceShape[blockmodelrenderer$enumneighborinfo.vert3Weights[1].shape];
            float f26 = faceShape[blockmodelrenderer$enumneighborinfo.vert3Weights[2].shape] * faceShape[blockmodelrenderer$enumneighborinfo.vert3Weights[3].shape];
            float f27 = faceShape[blockmodelrenderer$enumneighborinfo.vert3Weights[4].shape] * faceShape[blockmodelrenderer$enumneighborinfo.vert3Weights[5].shape];
            float f28 = faceShape[blockmodelrenderer$enumneighborinfo.vert3Weights[6].shape] * faceShape[blockmodelrenderer$enumneighborinfo.vert3Weights[7].shape];
            this.vertexColorMultiplier[blockmodelrenderer$vertextranslations.vert0] = f29 * f13 + f30 * f14 + f31 * f15 + f32 * f16;
            this.vertexColorMultiplier[blockmodelrenderer$vertextranslations.vert1] = f29 * f17 + f30 * f18 + f31 * f19 + f32 * f20;
            this.vertexColorMultiplier[blockmodelrenderer$vertextranslations.vert2] = f29 * f21 + f30 * f22 + f31 * f23 + f32 * f24;
            this.vertexColorMultiplier[blockmodelrenderer$vertextranslations.vert3] = f29 * f25 + f30 * f26 + f31 * f27 + f32 * f28;
            int i2 = this.getAoBrightness(l, i, j1, i3);
            int j2 = this.getAoBrightness(k, i, i1, i3);
            int k2 = this.getAoBrightness(k, j, k1, i3);
            int l2 = this.getAoBrightness(l, j, l1, i3);
            this.vertexBrightness[blockmodelrenderer$vertextranslations.vert0] = this.getVertexBrightness(i2, j2, k2, l2, f13, f14, f15, f16);
            this.vertexBrightness[blockmodelrenderer$vertextranslations.vert1] = this.getVertexBrightness(i2, j2, k2, l2, f17, f18, f19, f20);
            this.vertexBrightness[blockmodelrenderer$vertextranslations.vert2] = this.getVertexBrightness(i2, j2, k2, l2, f21, f22, f23, f24);
            this.vertexBrightness[blockmodelrenderer$vertextranslations.vert3] = this.getVertexBrightness(i2, j2, k2, l2, f25, f26, f27, f28);
        } else {
            float f9 = (f3 + f + f5 + f8) * 0.25F;
            float f10 = (f2 + f + f4 + f8) * 0.25F;
            float f11 = (f2 + f1 + f6 + f8) * 0.25F;
            float f12 = (f3 + f1 + f7 + f8) * 0.25F;
            this.vertexBrightness[blockmodelrenderer$vertextranslations.vert0] = this.getAoBrightness(l, i, j1, i3);
            this.vertexBrightness[blockmodelrenderer$vertextranslations.vert1] = this.getAoBrightness(k, i, i1, i3);
            this.vertexBrightness[blockmodelrenderer$vertextranslations.vert2] = this.getAoBrightness(k, j, k1, i3);
            this.vertexBrightness[blockmodelrenderer$vertextranslations.vert3] = this.getAoBrightness(l, j, l1, i3);
            this.vertexColorMultiplier[blockmodelrenderer$vertextranslations.vert0] = f9;
            this.vertexColorMultiplier[blockmodelrenderer$vertextranslations.vert1] = f10;
            this.vertexColorMultiplier[blockmodelrenderer$vertextranslations.vert2] = f11;
            this.vertexColorMultiplier[blockmodelrenderer$vertextranslations.vert3] = f12;
        }

    }

    private int getAoBrightness(int br1, int br2, int br3, int br4) {
        if (br1 == 0) {
            br1 = br4;
        }

        if (br2 == 0) {
            br2 = br4;
        }

        if (br3 == 0) {
            br3 = br4;
        }

        return br1 + br2 + br3 + br4 >> 2 & 16711935;
    }

    private int getVertexBrightness(int p_178203_1_, int p_178203_2_, int p_178203_3_, int p_178203_4_, float p_178203_5_, float p_178203_6_, float p_178203_7_, float p_178203_8_) {
        int i = (int) ((float) (p_178203_1_ >> 16 & 255) * p_178203_5_ + (float) (p_178203_2_ >> 16 & 255) * p_178203_6_ + (float) (p_178203_3_ >> 16 & 255) * p_178203_7_ + (float) (p_178203_4_ >> 16 & 255) * p_178203_8_) & 255;
        int j = (int) ((float) (p_178203_1_ & 255) * p_178203_5_ + (float) (p_178203_2_ & 255) * p_178203_6_ + (float) (p_178203_3_ & 255) * p_178203_7_ + (float) (p_178203_4_ & 255) * p_178203_8_) & 255;
        return i << 16 | j;
    }

    public enum EnumNeighborInfo {
        DOWN(new EnumFacing[]{EnumFacing.WEST, EnumFacing.EAST, EnumFacing.NORTH, EnumFacing.SOUTH}, 0.5F, true, new VanillaAoCalc.Orientation[]{VanillaAoCalc.Orientation.FLIP_WEST, VanillaAoCalc.Orientation.SOUTH, VanillaAoCalc.Orientation.FLIP_WEST, VanillaAoCalc.Orientation.FLIP_SOUTH, VanillaAoCalc.Orientation.WEST, VanillaAoCalc.Orientation.FLIP_SOUTH, VanillaAoCalc.Orientation.WEST, VanillaAoCalc.Orientation.SOUTH}, new VanillaAoCalc.Orientation[]{VanillaAoCalc.Orientation.FLIP_WEST, VanillaAoCalc.Orientation.NORTH, VanillaAoCalc.Orientation.FLIP_WEST, VanillaAoCalc.Orientation.FLIP_NORTH, VanillaAoCalc.Orientation.WEST, VanillaAoCalc.Orientation.FLIP_NORTH, VanillaAoCalc.Orientation.WEST, VanillaAoCalc.Orientation.NORTH}, new VanillaAoCalc.Orientation[]{VanillaAoCalc.Orientation.FLIP_EAST, VanillaAoCalc.Orientation.NORTH, VanillaAoCalc.Orientation.FLIP_EAST, VanillaAoCalc.Orientation.FLIP_NORTH, VanillaAoCalc.Orientation.EAST, VanillaAoCalc.Orientation.FLIP_NORTH, VanillaAoCalc.Orientation.EAST, VanillaAoCalc.Orientation.NORTH}, new VanillaAoCalc.Orientation[]{VanillaAoCalc.Orientation.FLIP_EAST, VanillaAoCalc.Orientation.SOUTH, VanillaAoCalc.Orientation.FLIP_EAST, VanillaAoCalc.Orientation.FLIP_SOUTH, VanillaAoCalc.Orientation.EAST, VanillaAoCalc.Orientation.FLIP_SOUTH, VanillaAoCalc.Orientation.EAST, VanillaAoCalc.Orientation.SOUTH}),
        UP(new EnumFacing[]{EnumFacing.EAST, EnumFacing.WEST, EnumFacing.NORTH, EnumFacing.SOUTH}, 1.0F, true, new VanillaAoCalc.Orientation[]{VanillaAoCalc.Orientation.EAST, VanillaAoCalc.Orientation.SOUTH, VanillaAoCalc.Orientation.EAST, VanillaAoCalc.Orientation.FLIP_SOUTH, VanillaAoCalc.Orientation.FLIP_EAST, VanillaAoCalc.Orientation.FLIP_SOUTH, VanillaAoCalc.Orientation.FLIP_EAST, VanillaAoCalc.Orientation.SOUTH}, new VanillaAoCalc.Orientation[]{VanillaAoCalc.Orientation.EAST, VanillaAoCalc.Orientation.NORTH, VanillaAoCalc.Orientation.EAST, VanillaAoCalc.Orientation.FLIP_NORTH, VanillaAoCalc.Orientation.FLIP_EAST, VanillaAoCalc.Orientation.FLIP_NORTH, VanillaAoCalc.Orientation.FLIP_EAST, VanillaAoCalc.Orientation.NORTH}, new VanillaAoCalc.Orientation[]{VanillaAoCalc.Orientation.WEST, VanillaAoCalc.Orientation.NORTH, VanillaAoCalc.Orientation.WEST, VanillaAoCalc.Orientation.FLIP_NORTH, VanillaAoCalc.Orientation.FLIP_WEST, VanillaAoCalc.Orientation.FLIP_NORTH, VanillaAoCalc.Orientation.FLIP_WEST, VanillaAoCalc.Orientation.NORTH}, new VanillaAoCalc.Orientation[]{VanillaAoCalc.Orientation.WEST, VanillaAoCalc.Orientation.SOUTH, VanillaAoCalc.Orientation.WEST, VanillaAoCalc.Orientation.FLIP_SOUTH, VanillaAoCalc.Orientation.FLIP_WEST, VanillaAoCalc.Orientation.FLIP_SOUTH, VanillaAoCalc.Orientation.FLIP_WEST, VanillaAoCalc.Orientation.SOUTH}),
        NORTH(new EnumFacing[]{EnumFacing.UP, EnumFacing.DOWN, EnumFacing.EAST, EnumFacing.WEST}, 0.8F, true, new VanillaAoCalc.Orientation[]{VanillaAoCalc.Orientation.UP, VanillaAoCalc.Orientation.FLIP_WEST, VanillaAoCalc.Orientation.UP, VanillaAoCalc.Orientation.WEST, VanillaAoCalc.Orientation.FLIP_UP, VanillaAoCalc.Orientation.WEST, VanillaAoCalc.Orientation.FLIP_UP, VanillaAoCalc.Orientation.FLIP_WEST}, new VanillaAoCalc.Orientation[]{VanillaAoCalc.Orientation.UP, VanillaAoCalc.Orientation.FLIP_EAST, VanillaAoCalc.Orientation.UP, VanillaAoCalc.Orientation.EAST, VanillaAoCalc.Orientation.FLIP_UP, VanillaAoCalc.Orientation.EAST, VanillaAoCalc.Orientation.FLIP_UP, VanillaAoCalc.Orientation.FLIP_EAST}, new VanillaAoCalc.Orientation[]{VanillaAoCalc.Orientation.DOWN, VanillaAoCalc.Orientation.FLIP_EAST, VanillaAoCalc.Orientation.DOWN, VanillaAoCalc.Orientation.EAST, VanillaAoCalc.Orientation.FLIP_DOWN, VanillaAoCalc.Orientation.EAST, VanillaAoCalc.Orientation.FLIP_DOWN, VanillaAoCalc.Orientation.FLIP_EAST}, new VanillaAoCalc.Orientation[]{VanillaAoCalc.Orientation.DOWN, VanillaAoCalc.Orientation.FLIP_WEST, VanillaAoCalc.Orientation.DOWN, VanillaAoCalc.Orientation.WEST, VanillaAoCalc.Orientation.FLIP_DOWN, VanillaAoCalc.Orientation.WEST, VanillaAoCalc.Orientation.FLIP_DOWN, VanillaAoCalc.Orientation.FLIP_WEST}),
        SOUTH(new EnumFacing[]{EnumFacing.WEST, EnumFacing.EAST, EnumFacing.DOWN, EnumFacing.UP}, 0.8F, true, new VanillaAoCalc.Orientation[]{VanillaAoCalc.Orientation.UP, VanillaAoCalc.Orientation.FLIP_WEST, VanillaAoCalc.Orientation.FLIP_UP, VanillaAoCalc.Orientation.FLIP_WEST, VanillaAoCalc.Orientation.FLIP_UP, VanillaAoCalc.Orientation.WEST, VanillaAoCalc.Orientation.UP, VanillaAoCalc.Orientation.WEST}, new VanillaAoCalc.Orientation[]{VanillaAoCalc.Orientation.DOWN, VanillaAoCalc.Orientation.FLIP_WEST, VanillaAoCalc.Orientation.FLIP_DOWN, VanillaAoCalc.Orientation.FLIP_WEST, VanillaAoCalc.Orientation.FLIP_DOWN, VanillaAoCalc.Orientation.WEST, VanillaAoCalc.Orientation.DOWN, VanillaAoCalc.Orientation.WEST}, new VanillaAoCalc.Orientation[]{VanillaAoCalc.Orientation.DOWN, VanillaAoCalc.Orientation.FLIP_EAST, VanillaAoCalc.Orientation.FLIP_DOWN, VanillaAoCalc.Orientation.FLIP_EAST, VanillaAoCalc.Orientation.FLIP_DOWN, VanillaAoCalc.Orientation.EAST, VanillaAoCalc.Orientation.DOWN, VanillaAoCalc.Orientation.EAST}, new VanillaAoCalc.Orientation[]{VanillaAoCalc.Orientation.UP, VanillaAoCalc.Orientation.FLIP_EAST, VanillaAoCalc.Orientation.FLIP_UP, VanillaAoCalc.Orientation.FLIP_EAST, VanillaAoCalc.Orientation.FLIP_UP, VanillaAoCalc.Orientation.EAST, VanillaAoCalc.Orientation.UP, VanillaAoCalc.Orientation.EAST}),
        WEST(new EnumFacing[]{EnumFacing.UP, EnumFacing.DOWN, EnumFacing.NORTH, EnumFacing.SOUTH}, 0.6F, true, new VanillaAoCalc.Orientation[]{VanillaAoCalc.Orientation.UP, VanillaAoCalc.Orientation.SOUTH, VanillaAoCalc.Orientation.UP, VanillaAoCalc.Orientation.FLIP_SOUTH, VanillaAoCalc.Orientation.FLIP_UP, VanillaAoCalc.Orientation.FLIP_SOUTH, VanillaAoCalc.Orientation.FLIP_UP, VanillaAoCalc.Orientation.SOUTH}, new VanillaAoCalc.Orientation[]{VanillaAoCalc.Orientation.UP, VanillaAoCalc.Orientation.NORTH, VanillaAoCalc.Orientation.UP, VanillaAoCalc.Orientation.FLIP_NORTH, VanillaAoCalc.Orientation.FLIP_UP, VanillaAoCalc.Orientation.FLIP_NORTH, VanillaAoCalc.Orientation.FLIP_UP, VanillaAoCalc.Orientation.NORTH}, new VanillaAoCalc.Orientation[]{VanillaAoCalc.Orientation.DOWN, VanillaAoCalc.Orientation.NORTH, VanillaAoCalc.Orientation.DOWN, VanillaAoCalc.Orientation.FLIP_NORTH, VanillaAoCalc.Orientation.FLIP_DOWN, VanillaAoCalc.Orientation.FLIP_NORTH, VanillaAoCalc.Orientation.FLIP_DOWN, VanillaAoCalc.Orientation.NORTH}, new VanillaAoCalc.Orientation[]{VanillaAoCalc.Orientation.DOWN, VanillaAoCalc.Orientation.SOUTH, VanillaAoCalc.Orientation.DOWN, VanillaAoCalc.Orientation.FLIP_SOUTH, VanillaAoCalc.Orientation.FLIP_DOWN, VanillaAoCalc.Orientation.FLIP_SOUTH, VanillaAoCalc.Orientation.FLIP_DOWN, VanillaAoCalc.Orientation.SOUTH}),
        EAST(new EnumFacing[]{EnumFacing.DOWN, EnumFacing.UP, EnumFacing.NORTH, EnumFacing.SOUTH}, 0.6F, true, new VanillaAoCalc.Orientation[]{VanillaAoCalc.Orientation.FLIP_DOWN, VanillaAoCalc.Orientation.SOUTH, VanillaAoCalc.Orientation.FLIP_DOWN, VanillaAoCalc.Orientation.FLIP_SOUTH, VanillaAoCalc.Orientation.DOWN, VanillaAoCalc.Orientation.FLIP_SOUTH, VanillaAoCalc.Orientation.DOWN, VanillaAoCalc.Orientation.SOUTH}, new VanillaAoCalc.Orientation[]{VanillaAoCalc.Orientation.FLIP_DOWN, VanillaAoCalc.Orientation.NORTH, VanillaAoCalc.Orientation.FLIP_DOWN, VanillaAoCalc.Orientation.FLIP_NORTH, VanillaAoCalc.Orientation.DOWN, VanillaAoCalc.Orientation.FLIP_NORTH, VanillaAoCalc.Orientation.DOWN, VanillaAoCalc.Orientation.NORTH}, new VanillaAoCalc.Orientation[]{VanillaAoCalc.Orientation.FLIP_UP, VanillaAoCalc.Orientation.NORTH, VanillaAoCalc.Orientation.FLIP_UP, VanillaAoCalc.Orientation.FLIP_NORTH, VanillaAoCalc.Orientation.UP, VanillaAoCalc.Orientation.FLIP_NORTH, VanillaAoCalc.Orientation.UP, VanillaAoCalc.Orientation.NORTH}, new VanillaAoCalc.Orientation[]{VanillaAoCalc.Orientation.FLIP_UP, VanillaAoCalc.Orientation.SOUTH, VanillaAoCalc.Orientation.FLIP_UP, VanillaAoCalc.Orientation.FLIP_SOUTH, VanillaAoCalc.Orientation.UP, VanillaAoCalc.Orientation.FLIP_SOUTH, VanillaAoCalc.Orientation.UP, VanillaAoCalc.Orientation.SOUTH});

        private final EnumFacing[] corners;
        private final float shadeWeight;
        private final boolean doNonCubicWeight;
        private final VanillaAoCalc.Orientation[] vert0Weights;
        private final VanillaAoCalc.Orientation[] vert1Weights;
        private final VanillaAoCalc.Orientation[] vert2Weights;
        private final VanillaAoCalc.Orientation[] vert3Weights;
        private static final VanillaAoCalc.EnumNeighborInfo[] VALUES = new VanillaAoCalc.EnumNeighborInfo[6];

        EnumNeighborInfo(EnumFacing[] p_i46236_3, float p_i46236_4, boolean p_i46236_5, VanillaAoCalc.Orientation[] p_i46236_6, VanillaAoCalc.Orientation[] p_i46236_7, VanillaAoCalc.Orientation[] p_i46236_8, VanillaAoCalc.Orientation[] p_i46236_9) {
            this.corners = p_i46236_3;
            this.shadeWeight = p_i46236_4;
            this.doNonCubicWeight = p_i46236_5;
            this.vert0Weights = p_i46236_6;
            this.vert1Weights = p_i46236_7;
            this.vert2Weights = p_i46236_8;
            this.vert3Weights = p_i46236_9;
        }

        public static VanillaAoCalc.EnumNeighborInfo getNeighbourInfo(EnumFacing p_178273_0_) {
            return VALUES[p_178273_0_.getIndex()];
        }

        static {
            VALUES[EnumFacing.DOWN.getIndex()] = DOWN;
            VALUES[EnumFacing.UP.getIndex()] = UP;
            VALUES[EnumFacing.NORTH.getIndex()] = NORTH;
            VALUES[EnumFacing.SOUTH.getIndex()] = SOUTH;
            VALUES[EnumFacing.WEST.getIndex()] = WEST;
            VALUES[EnumFacing.EAST.getIndex()] = EAST;
        }
    }

    public enum Orientation {
        DOWN(EnumFacing.DOWN, false),
        UP(EnumFacing.UP, false),
        NORTH(EnumFacing.NORTH, false),
        SOUTH(EnumFacing.SOUTH, false),
        WEST(EnumFacing.WEST, false),
        EAST(EnumFacing.EAST, false),
        FLIP_DOWN(EnumFacing.DOWN, true),
        FLIP_UP(EnumFacing.UP, true),
        FLIP_NORTH(EnumFacing.NORTH, true),
        FLIP_SOUTH(EnumFacing.SOUTH, true),
        FLIP_WEST(EnumFacing.WEST, true),
        FLIP_EAST(EnumFacing.EAST, true);

        private final int shape;

        private Orientation(EnumFacing p_i46233_3, boolean p_i46233_4) {
            this.shape = p_i46233_3.getIndex() + (p_i46233_4 ? EnumFacing.values().length : 0);
        }
    }

    public enum VertexTranslations {
        DOWN(0, 1, 2, 3),
        UP(2, 3, 0, 1),
        NORTH(3, 0, 1, 2),
        SOUTH(0, 1, 2, 3),
        WEST(3, 0, 1, 2),
        EAST(1, 2, 3, 0);

        private final int vert0;
        private final int vert1;
        private final int vert2;
        private final int vert3;
        private static final VanillaAoCalc.VertexTranslations[] VALUES = new VanillaAoCalc.VertexTranslations[6];

        VertexTranslations(int p_i46234_3, int p_i46234_4, int p_i46234_5, int p_i46234_6) {
            this.vert0 = p_i46234_3;
            this.vert1 = p_i46234_4;
            this.vert2 = p_i46234_5;
            this.vert3 = p_i46234_6;
        }

        public static VanillaAoCalc.VertexTranslations getVertexTranslations(EnumFacing p_178184_0_) {
            return VALUES[p_178184_0_.getIndex()];
        }

        static {
            VALUES[EnumFacing.DOWN.getIndex()] = DOWN;
            VALUES[EnumFacing.UP.getIndex()] = UP;
            VALUES[EnumFacing.NORTH.getIndex()] = NORTH;
            VALUES[EnumFacing.SOUTH.getIndex()] = SOUTH;
            VALUES[EnumFacing.WEST.getIndex()] = WEST;
            VALUES[EnumFacing.EAST.getIndex()] = EAST;
        }
    }
}