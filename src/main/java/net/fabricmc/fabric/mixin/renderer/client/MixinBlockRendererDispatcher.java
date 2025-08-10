package net.fabricmc.fabric.mixin.renderer.client;

import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel;
import net.fabricmc.fabric.impl.renderer.DamageModel;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

import org.apache.commons.lang3.tuple.MutablePair;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BlockRendererDispatcher.class)
public abstract class MixinBlockRendererDispatcher {

	private static final ThreadLocal<MutablePair<DamageModel, IBakedModel>> DAMAGE_STATE =
			ThreadLocal.withInitial(() -> MutablePair.of(new DamageModel(), null));

	@ModifyVariable(method = "renderBlockDamage", at = @At("STORE"), ordinal = 0)
	private IBakedModel hookRenderDamageModel(IBakedModel modelIn) {
		DAMAGE_STATE.get().setRight(modelIn);
		return modelIn;
	}

	@Inject(method = "renderBlockDamage", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/BlockModelRenderer;renderModel(Lnet/minecraft/world/IBlockAccess;Lnet/minecraft/client/renderer/block/model/IBakedModel;Lnet/minecraft/block/state/IBlockState;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/client/renderer/BufferBuilder;Z)Z"), cancellable = true)
	private void hookRenderDamage(IBlockState state, BlockPos pos, TextureAtlasSprite texture, IBlockAccess world, CallbackInfo ci) {
		MutablePair<DamageModel, IBakedModel> damageState = DAMAGE_STATE.get();
		IBakedModel originalModel = damageState.getRight();

		if (originalModel instanceof FabricBakedModel && !((FabricBakedModel) originalModel).isVanillaAdapter()) {
			DamageModel damageModel = damageState.getLeft();
			damageModel.prepare(originalModel, texture, state, pos);

			Tessellator tessellator = Tessellator.getInstance();
			BufferBuilder buffer = tessellator.getBuffer();
			buffer.begin(7, DefaultVertexFormats.BLOCK);

			BlockRendererDispatcher dispatcher = (BlockRendererDispatcher) (Object) this;
			dispatcher.getBlockModelRenderer().renderModel(world, damageModel, state, pos, buffer, true);

			tessellator.draw();
			ci.cancel();
		}
	}
}
