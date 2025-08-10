package net.fabricmc.fabric.impl.renderer;

import java.util.Map;
import java.util.function.Consumer;

import net.fabricmc.fabric.api.renderer.v1.mesh.QuadView;
import net.fabricmc.fabric.api.renderer.v1.model.SpriteFinder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.ResourceLocation;

public class SpriteFinderImpl implements SpriteFinder {
	private final Node root;

	public SpriteFinderImpl(Map<String, TextureAtlasSprite> sprites) {
		root = new Node(0.5f, 0.5f, 0.25f);
		sprites.values().forEach(root::add);
	}

	@Override
	public TextureAtlasSprite find(QuadView quad, int textureIndex) {
		float u = 0;
		float v = 0;

		for (int i = 0; i < 4; i++) {
			u += quad.spriteU(i, textureIndex);
			v += quad.spriteV(i, textureIndex);
		}

		return find(u * 0.25f, v * 0.25f);
	}

	@Override
	public TextureAtlasSprite find(float u, float v) {
		return root.find(u, v);
	}

	private static class Node {
		final float midU;
		final float midV;
		final float cellRadius;
		Object lowLow = null;
		Object lowHigh = null;
		Object highLow = null;
		Object highHigh = null;

		Node(float midU, float midV, float radius) {
			this.midU = midU;
			this.midV = midV;
			this.cellRadius = radius;
		}

		static final float EPS = 0.00001f;

		void add(TextureAtlasSprite sprite) {
			final boolean lowU = sprite.getMinU() < midU - EPS;
			final boolean highU = sprite.getMaxU() > midU + EPS;
			final boolean lowV = sprite.getMinV() < midV - EPS;
			final boolean highV = sprite.getMaxV() > midV + EPS;

			if (lowU && lowV) {
				addInner(sprite, lowLow, -1, -1, q -> lowLow = q);
			}

			if (lowU && highV) {
				addInner(sprite, lowHigh, -1, 1, q -> lowHigh = q);
			}

			if (highU && lowV) {
				addInner(sprite, highLow, 1, -1, q -> highLow = q);
			}

			if (highU && highV) {
				addInner(sprite, highHigh, 1, 1, q -> highHigh = q);
			}
		}

		private void addInner(TextureAtlasSprite sprite, Object quadrant, int uStep, int vStep, Consumer<Object> setter) {
			if (quadrant == null) {
				setter.accept(sprite);
			} else if (quadrant instanceof Node) {
				((Node) quadrant).add(sprite);
			} else {
				Node n = new Node(midU + cellRadius * uStep, midV + cellRadius * vStep, cellRadius * 0.5f);

				if (quadrant instanceof TextureAtlasSprite) {
					n.add((TextureAtlasSprite) quadrant);
				}

				n.add(sprite);
				setter.accept(n);
			}
		}

		private TextureAtlasSprite find(float u, float v) {
			if (u < midU) {
				return v < midV ? findInner(lowLow, u, v) : findInner(lowHigh, u, v);
			} else {
				return v < midV ? findInner(highLow, u, v) : findInner(highHigh, u, v);
			}
		}

		private TextureAtlasSprite findInner(Object quadrant, float u, float v) {
			if (quadrant instanceof TextureAtlasSprite) {
				return (TextureAtlasSprite) quadrant;
			} else if (quadrant instanceof Node) {
				return ((Node) quadrant).find(u, v);
			} else {
				return Minecraft.getMinecraft().getTextureMapBlocks().getMissingSprite();
			}
		}
	}

	public static SpriteFinderImpl get(TextureMap atlas) {
		return ((SpriteFinderAccess) atlas).fabric_spriteFinder();
	}

	public interface SpriteFinderAccess {
		SpriteFinderImpl fabric_spriteFinder();
	}
}
