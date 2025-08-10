package net.fabricmc.fabric.impl.client.indigo.renderer.helper;

import net.fabricmc.fabric.api.renderer.v1.mesh.MutableQuadView;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.EnumFacing;

public class TextureHelper {
	private static final float NORMALIZER = 1f / 16f;

	public static void bakeSprite(MutableQuadView quad, int spriteIndex, TextureAtlasSprite sprite, int bakeFlags) {
		if (quad.nominalFace() != null && (MutableQuadView.BAKE_LOCK_UV & bakeFlags) != 0) {
			applyModifier(quad, spriteIndex, UVLOCKERS[quad.nominalFace().getIndex()]);
		} else if ((MutableQuadView.BAKE_NORMALIZED & bakeFlags) == 0) {
			applyModifier(quad, spriteIndex, (q, i, t) -> q.sprite(i, t, q.spriteU(i, t) * NORMALIZER, q.spriteV(i, t) * NORMALIZER));
		}

		final int rotation = bakeFlags & 3;

		if (rotation != 0) {
			applyModifier(quad, spriteIndex, ROTATIONS[rotation]);
		}

		if ((MutableQuadView.BAKE_FLIP_U & bakeFlags) != 0) {
			applyModifier(quad, spriteIndex, (q, i, t) -> q.sprite(i, t, 1 - q.spriteU(i, t), q.spriteV(i, t)));
		}

		if ((MutableQuadView.BAKE_FLIP_V & bakeFlags) != 0) {
			applyModifier(quad, spriteIndex, (q, i, t) -> q.sprite(i, t, q.spriteU(i, t), 1 - q.spriteV(i, t)));
		}

		interpolate(quad, spriteIndex, sprite);
	}

	private static void interpolate(MutableQuadView q, int spriteIndex, TextureAtlasSprite sprite) {
		final float uMin = sprite.getMinU();
		final float uSpan = sprite.getMaxU() - uMin;
		final float vMin = sprite.getMinV();
		final float vSpan = sprite.getMaxV() - vMin;

		for (int i = 0; i < 4; i++) {
			q.sprite(i, spriteIndex, uMin + q.spriteU(i, spriteIndex) * uSpan, vMin + q.spriteV(i, spriteIndex) * vSpan);
		}
	}

	@FunctionalInterface
	private interface VertexModifier {
		void apply(MutableQuadView quad, int vertexIndex, int spriteIndex);
	}

	private static void applyModifier(MutableQuadView quad, int spriteIndex, VertexModifier modifier) {
		for (int i = 0; i < 4; i++) {
			modifier.apply(quad, i, spriteIndex);
		}
	}

	private static final VertexModifier[] ROTATIONS = new VertexModifier[] {
			null,
			(q, i, t) -> q.sprite(i, t, q.spriteV(i, t), q.spriteU(i, t)),
			(q, i, t) -> q.sprite(i, t, 1 - q.spriteU(i, t), 1 - q.spriteV(i, t)),
			(q, i, t) -> q.sprite(i, t, 1 - q.spriteV(i, t), q.spriteU(i, t))
	};

	private static final VertexModifier[] UVLOCKERS = new VertexModifier[6];

	static {
		UVLOCKERS[EnumFacing.EAST.getIndex()] = (q, i, t) -> q.sprite(i, t, 1 - q.z(i), 1 - q.y(i));
		UVLOCKERS[EnumFacing.WEST.getIndex()] = (q, i, t) -> q.sprite(i, t, q.z(i), 1 - q.y(i));
		UVLOCKERS[EnumFacing.NORTH.getIndex()] = (q, i, t) -> q.sprite(i, t, 1 - q.x(i), 1 - q.y(i));
		UVLOCKERS[EnumFacing.SOUTH.getIndex()] = (q, i, t) -> q.sprite(i, t, q.x(i), 1 - q.y(i));
		UVLOCKERS[EnumFacing.DOWN.getIndex()] = (q, i, t) -> q.sprite(i, t, q.x(i), 1 - q.z(i));
		UVLOCKERS[EnumFacing.UP.getIndex()] = (q, i, t) -> q.sprite(i, t, q.x(i), 1 - q.z(i));
	}
}
