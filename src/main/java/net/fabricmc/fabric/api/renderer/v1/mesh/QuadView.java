package net.fabricmc.fabric.api.renderer.v1.mesh;

import net.fabricmc.fabric.api.renderer.v1.material.RenderMaterial;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.EnumFacing;

import javax.vecmath.Vector3f;

public interface QuadView {
	void toVanilla(int spriteIndex, int[] target, int targetIndex, boolean isItem);

	void copyTo(MutableQuadView target);

	RenderMaterial material();

	int colorIndex();

	EnumFacing lightFace();

	EnumFacing cullFace();

	EnumFacing nominalFace();

	Vector3f faceNormal();

	default BakedQuad toBakedQuad(int spriteIndex, TextureAtlasSprite sprite, boolean isItem) {
		int[] vertexData = new int[28];
		toVanilla(spriteIndex, vertexData, 0, isItem);
		return new BakedQuad(vertexData, colorIndex(), lightFace(), sprite);
	}

	int tag();

	Vector3f copyPos(int vertexIndex, Vector3f target);

	float posByIndex(int vertexIndex, int coordinateIndex);

	float x(int vertexIndex);

	float y(int vertexIndex);

	float z(int vertexIndex);

	boolean hasNormal(int vertexIndex);

	Vector3f copyNormal(int vertexIndex, Vector3f target);

	float normalX(int vertexIndex);

	float normalY(int vertexIndex);

	float normalZ(int vertexIndex);

	int lightmap(int vertexIndex);

	int spriteColor(int vertexIndex, int spriteIndex);

	float spriteU(int vertexIndex, int spriteIndex);

	float spriteV(int vertexIndex, int spriteIndex);
}
