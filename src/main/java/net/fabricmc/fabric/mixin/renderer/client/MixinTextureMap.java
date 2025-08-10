package net.fabricmc.fabric.mixin.renderer.client;

import java.util.Map;

import net.fabricmc.fabric.impl.renderer.SpriteFinderImpl;
import net.minecraft.client.renderer.texture.Stitcher;
import net.minecraftforge.fml.common.ProgressManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;

@Mixin(TextureMap.class)
public abstract class MixinTextureMap implements SpriteFinderImpl.SpriteFinderAccess {
	@Shadow
	private Map<String, TextureAtlasSprite> mapRegisteredSprites;

	private SpriteFinderImpl fabric_spriteFinder = null;

	@Inject(method = "loadTexture(Lnet/minecraft/client/renderer/texture/Stitcher;Lnet/minecraft/client/resources/IResourceManager;Lnet/minecraft/util/ResourceLocation;Lnet/minecraft/client/renderer/texture/TextureAtlasSprite;Lnet/minecraftforge/fml/common/ProgressManager$ProgressBar;II)I", at = @At("RETURN"))
	private void onLoadTexture(Stitcher stitcher, IResourceManager resourceManager, ResourceLocation location, TextureAtlasSprite textureatlassprite, ProgressManager.ProgressBar bar, int j, int k, CallbackInfoReturnable<Integer> cir) {
		fabric_spriteFinder = null;
	}

	@Override
	public SpriteFinderImpl fabric_spriteFinder() {
		if (fabric_spriteFinder == null) {
			fabric_spriteFinder = new SpriteFinderImpl(mapRegisteredSprites);
		}
		return fabric_spriteFinder;
	}
}
