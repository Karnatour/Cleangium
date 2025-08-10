package net.fabricmc.fabric.impl.client.indigo.renderer.helper;

import java.nio.ByteOrder;

import it.unimi.dsi.fastutil.ints.Int2IntFunction;

import net.fabricmc.fabric.api.renderer.v1.mesh.MutableQuadView;
import net.minecraft.util.EnumFacing;

import javax.vecmath.Vector3f;

public abstract class ColorHelper {
	public interface ShadeableQuad extends MutableQuadView {
		boolean isFaceAligned();
		boolean needsDiffuseShading(int textureIndex);
	}

	private ColorHelper() { }

	private static final float[] FACE_SHADE_FACTORS = { 0.5F, 1.0F, 0.8F, 0.8F, 0.6F, 0.6F};

	private static final Int2IntFunction colorSwapper = ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN
			? color -> ((color & 0xFF00FF00) | ((color & 0x00FF0000) >> 16) | ((color & 0xFF) << 16))
			: color -> color;

	public static int swapRedBlueIfNeeded(int color) {
		return colorSwapper.applyAsInt(color);
	}

	public static int multiplyColor(int color1, int color2) {
		if (color1 == -1) {
			return color2;
		} else if (color2 == -1) {
			return color1;
		}

		int alpha = ((color1 >> 24) & 0xFF) * ((color2 >> 24) & 0xFF) / 0xFF;
		int red = ((color1 >> 16) & 0xFF) * ((color2 >> 16) & 0xFF) / 0xFF;
		int green = ((color1 >> 8) & 0xFF) * ((color2 >> 8) & 0xFF) / 0xFF;
		int blue = (color1 & 0xFF) * (color2 & 0xFF) / 0xFF;

		return (alpha << 24) | (red << 16) | (green << 8) | blue;
	}

	public static int multiplyRGB(int color, float shade) {
		int alpha = ((color >> 24) & 0xFF);
		int red = (int) (((color >> 16) & 0xFF) * shade);
		int green = (int) (((color >> 8) & 0xFF) * shade);
		int blue = (int) ((color & 0xFF) * shade);

		return (alpha << 24) | (red << 16) | (green << 8) | blue;
	}

	public static float diffuseShade(EnumFacing direction) {
		return FACE_SHADE_FACTORS[direction.getIndex()];
	}

	public static float normalShade(float normalX, float normalY, float normalZ) {
		return Math.min(0.5f + Math.abs(normalX) * 0.1f + (normalY > 0 ? 0.5f * normalY : 0) + Math.abs(normalZ) * 0.3f, 1f);
	}

	public static float normalShade(Vector3f normal) {
		return normalShade(normal.getX(), normal.getY(), normal.getZ());
	}

	public static float vertexShade(ShadeableQuad q, int vertexIndex, float faceShade) {
		return q.hasNormal(vertexIndex)
				? normalShade(q.normalX(vertexIndex), q.normalY(vertexIndex), q.normalZ(vertexIndex)) : faceShade;
	}

	public static float faceShade(ShadeableQuad quad) {
		return quad.isFaceAligned() ? diffuseShade(quad.lightFace()) : normalShade(quad.faceNormal());
	}

	@FunctionalInterface
	private interface VertexLighter {
		void shade(ShadeableQuad quad, int vertexIndex, float shade);
	}

	private static VertexLighter[] VERTEX_LIGHTERS = new VertexLighter[8];

	static {
		VERTEX_LIGHTERS[0b000] = (q, i, s) -> { };
		VERTEX_LIGHTERS[0b001] = (q, i, s) -> q.spriteColor(i, 0, multiplyRGB(q.spriteColor(i, 0), s));
		VERTEX_LIGHTERS[0b010] = (q, i, s) -> q.spriteColor(i, 1, multiplyRGB(q.spriteColor(i, 1), s));
		VERTEX_LIGHTERS[0b011] = (q, i, s) -> q.spriteColor(i, 0, multiplyRGB(q.spriteColor(i, 0), s))
				.spriteColor(i, 1, multiplyRGB(q.spriteColor(i, 1), s));
		VERTEX_LIGHTERS[0b100] = (q, i, s) -> q.spriteColor(i, 2, multiplyRGB(q.spriteColor(i, 2), s));
		VERTEX_LIGHTERS[0b101] = (q, i, s) -> q.spriteColor(i, 0, multiplyRGB(q.spriteColor(i, 0), s))
				.spriteColor(i, 2, multiplyRGB(q.spriteColor(i, 2), s));
		VERTEX_LIGHTERS[0b110] = (q, i, s) -> q.spriteColor(i, 1, multiplyRGB(q.spriteColor(i, 1), s))
				.spriteColor(i, 2, multiplyRGB(q.spriteColor(i, 2), s));
		VERTEX_LIGHTERS[0b111] = (q, i, s) -> q.spriteColor(i, 0, multiplyRGB(q.spriteColor(i, 0), s))
				.spriteColor(i, 1, multiplyRGB(q.spriteColor(i, 1), s))
				.spriteColor(i, 2, multiplyRGB(q.spriteColor(i, 2), s));
	}

	public static void applyDiffuseShading(ShadeableQuad quad, boolean undo) {
		final float faceShade = faceShade(quad);
		int i = quad.needsDiffuseShading(0) ? 1 : 0;

		if (quad.needsDiffuseShading(1)) {
			i |= 2;
		}

		if (quad.needsDiffuseShading(2)) {
			i |= 4;
		}

		if (i == 0) {
			return;
		}

		final VertexLighter shader = VERTEX_LIGHTERS[i];

		for (int j = 0; j < 4; j++) {
			final float vertexShade = vertexShade(quad, j, faceShade);
			shader.shade(quad, j, undo ? 1f / vertexShade : vertexShade);
		}
	}

	public static int maxBrightness(int b0, int b1) {
		if (b0 == 0) return b1;
		if (b1 == 0) return b0;

		int skyLight0 = 0;
		int blockLight0 = 0;
		for (int shift = 24; shift >= 0; shift -= 8) {
			int currentByte = (b0 >> shift) & 0xFF;
			if (currentByte != 0) {
				skyLight0 = currentByte;
				int mask = (1 << shift) - 1;
				blockLight0 = b0 & mask;
				break;
			}
		}

		int skyLight1 = 0;
		int blockLight1 = 0;
		for (int shift = 24; shift >= 0; shift -= 8) {
			int currentByte = (b1 >> shift) & 0xFF;
			if (currentByte != 0) {
				skyLight1 = currentByte;
				int mask = (1 << shift) - 1;
				blockLight1 = b1 & mask;
				break;
			}
		}

		int skyMax   = Math.max(skyLight0, skyLight1);
		int blockMax = Math.max(blockLight0, blockLight1);

		return (blockMax << 16) | skyMax;
	}
}
