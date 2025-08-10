package net.fabricmc.fabric.api.renderer.v1.mesh;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.EnumFacing;

import net.fabricmc.fabric.api.renderer.v1.material.RenderMaterial;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;

import javax.vecmath.Vector3f;

public interface QuadEmitter extends MutableQuadView {
	@Override
	QuadEmitter material(RenderMaterial material);

	@Override
	QuadEmitter cullFace(EnumFacing face);

	@Override
	QuadEmitter nominalFace(EnumFacing face);

	@Override
	QuadEmitter colorIndex(int colorIndex);

	@Override
	QuadEmitter fromVanilla(int[] quadData, int startIndex, boolean isItem);

	@Override
	QuadEmitter tag(int tag);

	@Override
	QuadEmitter pos(int vertexIndex, float x, float y, float z);

	@Override
	default QuadEmitter pos(int vertexIndex, Vector3f vec) {
		MutableQuadView.super.pos(vertexIndex, vec);
		return this;
	}

	@Override
	default QuadEmitter normal(int vertexIndex, Vector3f vec) {
		MutableQuadView.super.normal(vertexIndex, vec);
		return this;
	}

	@Override
	QuadEmitter lightmap(int vertexIndex, int lightmap);

	@Override
	default QuadEmitter lightmap(int b0, int b1, int b2, int b3) {
		MutableQuadView.super.lightmap(b0, b1, b2, b3);
		return this;
	}

	@Override
	QuadEmitter spriteColor(int vertexIndex, int spriteIndex, int color);

	@Override
	default QuadEmitter spriteColor(int spriteIndex, int c0, int c1, int c2, int c3) {
		MutableQuadView.super.spriteColor(spriteIndex, c0, c1, c2, c3);
		return this;
	}

	@Override
	QuadEmitter sprite(int vertexIndex, int spriteIndex, float u, float v);

	default QuadEmitter spriteUnitSquare(int spriteIndex) {
		sprite(0, spriteIndex, 0, 0);
		sprite(1, spriteIndex, 0, 1);
		sprite(2, spriteIndex, 1, 1);
		sprite(3, spriteIndex, 1, 0);
		return this;
	}

	@Override
	QuadEmitter spriteBake(int spriteIndex, TextureAtlasSprite sprite, int bakeFlags);

	float CULL_FACE_EPSILON = 0.00001f;

	default QuadEmitter square(EnumFacing nominalFace, float left, float bottom, float right, float top, float depth) {
		if (Math.abs(depth) < CULL_FACE_EPSILON) {
			cullFace(nominalFace);
			depth = 0;
		} else {
			cullFace(null);
		}

		nominalFace(nominalFace);
		switch (nominalFace) {
		case UP:
			depth = 1 - depth;
			top = 1 - top;
			bottom = 1 - bottom;

		case DOWN:
			pos(0, left, depth, top);
			pos(1, left, depth, bottom);
			pos(2, right, depth, bottom);
			pos(3, right, depth, top);
			break;

		case EAST:
			depth = 1 - depth;
			left = 1 - left;
			right = 1 - right;

		case WEST:
			pos(0, depth, top, left);
			pos(1, depth, bottom, left);
			pos(2, depth, bottom, right);
			pos(3, depth, top, right);
			break;

		case SOUTH:
			depth = 1 - depth;
			left = 1 - left;
			right = 1 - right;

		case NORTH:
			pos(0, right, top, depth);
			pos(1, right, bottom, depth);
			pos(2, left, bottom, depth);
			pos(3, left, top, depth);
			break;
		}

		return this;
	}

	QuadEmitter emit();

}
