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

package net.fabricmc.fabric.api.renderer.v1.model;

import java.util.List;
import java.util.Random;
import java.util.function.Supplier;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.fabricmc.fabric.impl.renderer.DamageModel;
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.Nullable;

import javax.vecmath.Matrix4f;

/**
 * Base class for specialized model implementations that need to wrap other baked models.
 * Avoids boilerplate code for pass-through methods. For example usage see {@link DamageModel}.
 */
public abstract class ForwardingBakedModel implements IBakedModel, FabricBakedModel {
    /**
     * implementations must set this somehow.
     */
    protected IBakedModel wrapped;

    @Override
    public void emitBlockQuads(World blockView, IBlockState state, BlockPos pos, Supplier<Random> randomSupplier, RenderContext context) {
        ((FabricBakedModel) wrapped).emitBlockQuads(blockView, state, pos, randomSupplier, context);
    }

    @Override
    public boolean isVanillaAdapter() {
        return ((FabricBakedModel) wrapped).isVanillaAdapter();
    }

    @Override
    public void emitItemQuads(ItemStack stack, Supplier<Random> randomSupplier, RenderContext context) {
        ((FabricBakedModel) wrapped).emitItemQuads(stack, randomSupplier, context);
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable IBlockState state, @Nullable EnumFacing side, long rand) {
        return wrapped.getQuads(state, side, rand);
    }

    @Override
    public boolean isAmbientOcclusion() {
        return wrapped.isAmbientOcclusion();
    }

    @Override
    public boolean isGui3d() {
        return wrapped.isGui3d();
    }

    @Override
    public boolean isBuiltInRenderer() {
        return wrapped.isBuiltInRenderer();
    }

    @Override
    public TextureAtlasSprite getParticleTexture() {
        return wrapped.getParticleTexture();
    }

    @Override
    public ItemCameraTransforms getItemCameraTransforms() {
        return wrapped.getItemCameraTransforms();
    }

    @Override
    public ItemOverrideList getOverrides() {
        return wrapped.getOverrides();
    }

    @Override
    public Pair<? extends IBakedModel, Matrix4f> handlePerspective(ItemCameraTransforms.TransformType cameraTransformType) {
        return wrapped.handlePerspective(cameraTransformType);
    }
}
