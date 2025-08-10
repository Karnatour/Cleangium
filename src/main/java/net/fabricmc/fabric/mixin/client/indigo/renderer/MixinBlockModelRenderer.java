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


package net.fabricmc.fabric.mixin.client.indigo.renderer;

import java.util.Random;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.BlockModelRenderer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.util.math.BlockPos;

import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel;
import net.fabricmc.fabric.impl.client.indigo.renderer.render.BlockRenderContext;

@Mixin(BlockModelRenderer.class)
public abstract class MixinBlockModelRenderer {

	private final ThreadLocal<BlockRenderContext> CONTEXTS = ThreadLocal.withInitial(BlockRenderContext::new);

	@Inject(
			method = "renderModel(Lnet/minecraft/world/IBlockAccess;Lnet/minecraft/client/renderer/block/model/IBakedModel;Lnet/minecraft/block/state/IBlockState;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/client/renderer/BufferBuilder;ZJ)Z",
			at = @At("HEAD"),
			cancellable = true
	)
	private void hookRenderModel(IBlockAccess world, IBakedModel model, IBlockState state, BlockPos pos, BufferBuilder buffer, boolean checkSides, long rand, CallbackInfoReturnable<Boolean> cir) {
		if (!((FabricBakedModel) model).isVanillaAdapter()) {
			BlockRenderContext context = CONTEXTS.get();

			if (!context.isCallingVanilla()) {
				cir.setReturnValue(CONTEXTS.get().tesselate((BlockModelRenderer) (Object) this, (World) world, model, state, pos, buffer, rand));
			}
		}
	}
}

