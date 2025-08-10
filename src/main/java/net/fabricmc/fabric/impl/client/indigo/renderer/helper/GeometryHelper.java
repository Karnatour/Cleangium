package net.fabricmc.fabric.impl.client.indigo.renderer.helper;

import net.fabricmc.fabric.api.renderer.v1.mesh.QuadView;
import net.minecraft.util.EnumFacing;

import javax.vecmath.Vector3f;

import static net.fabricmc.fabric.impl.client.indigo.renderer.helper.MathHelp.equalsApproximate;

public abstract class GeometryHelper {
	public static final int CUBIC_FLAG = 1;

	public static final int AXIS_ALIGNED_FLAG = CUBIC_FLAG << 1;

	public static final int LIGHT_FACE_FLAG = AXIS_ALIGNED_FLAG << 1;

	private static final float EPS_MIN = 0.0001f;
	private static final float EPS_MAX = 1.0f - EPS_MIN;

	private GeometryHelper() { }

	public static int computeShapeFlags(QuadView quad) {
		EnumFacing lightFace = quad.lightFace();
		int bits = 0;

		if (isQuadParallelToFace(lightFace, quad)) {
			bits |= AXIS_ALIGNED_FLAG;

			if (isParallelQuadOnFace(lightFace, quad)) {
				bits |= LIGHT_FACE_FLAG;
			}
		}

		if (isQuadCubic(lightFace, quad)) {
			bits |= CUBIC_FLAG;
		}

		return bits;
	}

	public static boolean isQuadParallelToFace(EnumFacing face, QuadView quad) {
		if (face == null) {
			return false;
		}

		int i = face.getAxis().ordinal();
		final float val = quad.posByIndex(0, i);
		return equalsApproximate(val, quad.posByIndex(1, i))
				&& equalsApproximate(val, quad.posByIndex(2, i))
				&& equalsApproximate(val, quad.posByIndex(3, i));
	}

	public static boolean isParallelQuadOnFace(EnumFacing lightFace, QuadView quad) {
		if (lightFace == null) return false;

		final float x = quad.posByIndex(0, lightFace.getAxis().ordinal());
		return lightFace.getAxisDirection() == EnumFacing.AxisDirection.POSITIVE ? x >= EPS_MAX : x <= EPS_MIN;
	}

	public static boolean isQuadCubic(EnumFacing lightFace, QuadView quad) {
		if (lightFace == null) {
			return false;
		}

		int a, b;

		switch (lightFace) {
		case EAST:
		case WEST:
			a = 1;
			b = 2;
			break;
		case UP:
		case DOWN:
			a = 0;
			b = 2;
			break;
		case SOUTH:
		case NORTH:
			a = 1;
			b = 0;
			break;
		default:
			return false;
		}

		return confirmSquareCorners(a, b, quad);
	}

	private static boolean confirmSquareCorners(int aCoordinate, int bCoordinate, QuadView quad) {
		int flags = 0;

		for (int i = 0; i < 4; i++) {
			final float a = quad.posByIndex(i, aCoordinate);
			final float b = quad.posByIndex(i, bCoordinate);

			if (a <= EPS_MIN) {
				if (b <= EPS_MIN) {
					flags |= 1;
				} else if (b >= EPS_MAX) {
					flags |= 2;
				} else {
					return false;
				}
			} else if (a >= EPS_MAX) {
				if (b <= EPS_MIN) {
					flags |= 4;
				} else if (b >= EPS_MAX) {
					flags |= 8;
				} else {
					return false;
				}
			} else {
				return false;
			}
		}

		return flags == 15;
	}

	public static EnumFacing lightFace(QuadView quad) {
		final Vector3f normal = quad.faceNormal();
		switch (GeometryHelper.longestAxis(normal)) {
		case X:
			return normal.getX() > 0 ? EnumFacing.EAST : EnumFacing.WEST;

		case Y:
			return normal.getY() > 0 ? EnumFacing.UP : EnumFacing.DOWN;

		case Z:
			return normal.getZ() > 0 ? EnumFacing.SOUTH : EnumFacing.NORTH;

		default:
			return EnumFacing.UP;
		}
	}

	public static float min(float a, float b, float c, float d) {
		final float x = a < b ? a : b;
		final float y = c < d ? c : d;
		return x < y ? x : y;
	}

	public static float max(float a, float b, float c, float d) {
		final float x = a > b ? a : b;
		final float y = c > d ? c : d;
		return x > y ? x : y;
	}

	public static EnumFacing.Axis longestAxis(Vector3f vec) {
		return longestAxis(vec.getX(), vec.getY(), vec.getZ());
	}

	public static EnumFacing.Axis longestAxis(float normalX, float normalY, float normalZ) {
		EnumFacing.Axis result = EnumFacing.Axis.Y;
		float longest = Math.abs(normalY);
		float a = Math.abs(normalX);

		if (a > longest) {
			result = EnumFacing.Axis.X;
			longest = a;
		}

		return Math.abs(normalZ) > longest
				? EnumFacing.Axis.Z : result;
	}
}
