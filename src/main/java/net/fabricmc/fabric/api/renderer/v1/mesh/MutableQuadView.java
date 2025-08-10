package net.fabricmc.fabric.api.renderer.v1.mesh;

import net.fabricmc.fabric.api.renderer.v1.material.RenderMaterial;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.EnumFacing;

import javax.vecmath.Vector3f;

public interface MutableQuadView extends QuadView {
	int BAKE_ROTATE_NONE = 0;

	int BAKE_ROTATE_90 = 1;

	int BAKE_ROTATE_180 = 2;

	int BAKE_ROTATE_270 = 3;

	int BAKE_LOCK_UV = 4;

	int BAKE_FLIP_U = 8;

	int BAKE_FLIP_V = 16;

	int BAKE_NORMALIZED = 32;

	MutableQuadView material(RenderMaterial material);

	MutableQuadView cullFace(EnumFacing face);

	MutableQuadView nominalFace(EnumFacing face);

	MutableQuadView colorIndex(int colorIndex);

	MutableQuadView fromVanilla(int[] quadData, int startIndex, boolean isItem);

	MutableQuadView tag(int tag);

	MutableQuadView pos(int vertexIndex, float x, float y, float z);

	default MutableQuadView pos(int vertexIndex, Vector3f vec) {
		return pos(vertexIndex, vec.getX(), vec.getY(), vec.getZ());
	}

	MutableQuadView normal(int vertexIndex, float x, float y, float z);

	default MutableQuadView normal(int vertexIndex, Vector3f vec) {
		return normal(vertexIndex, vec.getX(), vec.getY(), vec.getZ());
	}

	MutableQuadView lightmap(int vertexIndex, int lightmap);

	default MutableQuadView lightmap(int b0, int b1, int b2, int b3) {
		lightmap(0, b0);
		lightmap(1, b1);
		lightmap(2, b2);
		lightmap(3, b3);
		return this;
	}

	MutableQuadView spriteColor(int vertexIndex, int spriteIndex, int color);

	default MutableQuadView spriteColor(int spriteIndex, int c0, int c1, int c2, int c3) {
		spriteColor(0, spriteIndex, c0);
		spriteColor(1, spriteIndex, c1);
		spriteColor(2, spriteIndex, c2);
		spriteColor(3, spriteIndex, c3);
		return this;
	}

	MutableQuadView sprite(int vertexIndex, int spriteIndex, float u, float v);

	MutableQuadView spriteBake(int spriteIndex, TextureAtlasSprite sprite, int bakeFlags);

}
