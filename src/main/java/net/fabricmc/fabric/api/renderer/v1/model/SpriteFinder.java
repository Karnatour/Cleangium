package net.fabricmc.fabric.api.renderer.v1.model;

import net.fabricmc.fabric.impl.renderer.SpriteFinderImpl;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadView;

public interface SpriteFinder {
	static SpriteFinder get(TextureMap atlas) {
		return SpriteFinderImpl.get(atlas);
	}

	TextureAtlasSprite find(QuadView quad, int textureIndex);

	TextureAtlasSprite find(float u, float v);
}
