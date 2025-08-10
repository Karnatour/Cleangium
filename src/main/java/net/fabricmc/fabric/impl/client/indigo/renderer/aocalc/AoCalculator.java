package net.fabricmc.fabric.impl.client.indigo.renderer.aocalc;

import static java.lang.Math.max;
import static net.fabricmc.fabric.impl.client.indigo.renderer.helper.GeometryHelper.AXIS_ALIGNED_FLAG;
import static net.fabricmc.fabric.impl.client.indigo.renderer.helper.GeometryHelper.CUBIC_FLAG;
import static net.fabricmc.fabric.impl.client.indigo.renderer.helper.GeometryHelper.LIGHT_FACE_FLAG;

import java.util.function.ToIntFunction;

import net.fabricmc.fabric.impl.client.indigo.renderer.Indigo;
import net.fabricmc.fabric.impl.client.indigo.renderer.helper.MathHelp;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.block.Block;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

import net.fabricmc.fabric.impl.client.indigo.renderer.aocalc.AoFace.WeightFunction;
import net.fabricmc.fabric.impl.client.indigo.renderer.mesh.MutableQuadViewImpl;
import net.fabricmc.fabric.impl.client.indigo.renderer.mesh.QuadViewImpl;
import net.fabricmc.fabric.impl.client.indigo.renderer.render.BlockRenderInfo;

import javax.vecmath.Vector3f;

public class AoCalculator {
	@FunctionalInterface
	public interface AoFunc {
		float apply(BlockPos pos);
	}

	private static final int[][] VERTEX_MAP = new int[6][4];

	static {
		VERTEX_MAP[EnumFacing.DOWN.getIndex()] = new int[] {0, 1, 2, 3};
		VERTEX_MAP[EnumFacing.UP.getIndex()] = new int[] {2, 3, 0, 1};
		VERTEX_MAP[EnumFacing.NORTH.getIndex()] = new int[] {3, 0, 1, 2};
		VERTEX_MAP[EnumFacing.SOUTH.getIndex()] = new int[] {0, 1, 2, 3};
		VERTEX_MAP[EnumFacing.WEST.getIndex()] = new int[] {3, 0, 1, 2};
		VERTEX_MAP[EnumFacing.EAST.getIndex()] = new int[] {1, 2, 3, 0};
	}

	private static final Logger LOGGER = LogManager.getLogger();

	private final BlockPos.MutableBlockPos lightPos = new BlockPos.MutableBlockPos();
	private final BlockPos.MutableBlockPos searchPos = new BlockPos.MutableBlockPos();
	private final BlockRenderInfo blockInfo;
	private final ToIntFunction<BlockPos> brightnessFunc;
	private final AoFunc aoFunc;

	private final AoFaceData[] faceData = new AoFaceData[12];

	private int completionFlags = 0;

	private final float[] w = new float[4];

	public final float[] ao = new float[4];
	public final int[] light = new int[4];

	public AoCalculator(BlockRenderInfo blockInfo, ToIntFunction<BlockPos> brightnessFunc, AoFunc aoFunc) {
		this.blockInfo = blockInfo;
		this.brightnessFunc = brightnessFunc;
		this.aoFunc = aoFunc;

		for (int i = 0; i < 12; i++) {
			faceData[i] = new AoFaceData();
		}
	}

	public void clear() {
		completionFlags = 0;
	}

	public void compute(MutableQuadViewImpl quad, boolean isVanilla) {
		final AoConfig config = Indigo.AMBIENT_OCCLUSION_MODE;
		final boolean shouldCompare;

		switch (config) {
		case VANILLA:
			calcVanilla(quad);
			shouldCompare = false;
			break;

		case EMULATE:
			calcFastVanilla(quad);
			shouldCompare = Indigo.DEBUG_COMPARE_LIGHTING && isVanilla;
			break;

		case HYBRID:
		default:
			if (isVanilla) {
				shouldCompare = Indigo.DEBUG_COMPARE_LIGHTING;
				calcFastVanilla(quad);
			} else {
				shouldCompare = false;
				calcEnhanced(quad);
			}

			break;

		case ENHANCED:
			shouldCompare = false;
			calcEnhanced(quad);
		}

		if (shouldCompare) {
			float[] vanillaAo = new float[4];
			int[] vanillaLight = new int[4];

			//vanillaCalc.compute(blockInfo, quad, vanillaAo, vanillaLight);

			for (int i = 0; i < 4; i++) {
				if (light[i] != vanillaLight[i] || !MathHelp.equalsApproximate(ao[i], vanillaAo[i])) {
					LOGGER.info(String.format("Mismatch for %s @ %s", blockInfo.blockState.toString(), blockInfo.blockPos.toString()));
					LOGGER.info(String.format("Flags = %d, LightFace = %s", quad.geometryFlags(), quad.lightFace().toString()));
					LOGGER.info(String.format("	Old Multiplier: %.2f, %.2f, %.2f, %.2f",
							vanillaAo[0], vanillaAo[1], vanillaAo[2], vanillaAo[3]));
					LOGGER.info(String.format("	New Multiplier: %.2f, %.2f, %.2f, %.2f", ao[0], ao[1], ao[2], ao[3]));
					LOGGER.info(String.format("	Old Brightness: %s, %s, %s, %s",
							Integer.toHexString(vanillaLight[0]), Integer.toHexString(vanillaLight[1]),
							Integer.toHexString(vanillaLight[2]), Integer.toHexString(vanillaLight[3])));
					LOGGER.info(String.format("	New Brightness: %s, %s, %s, %s", Integer.toHexString(light[0]),
							Integer.toHexString(light[1]), Integer.toHexString(light[2]), Integer.toHexString(light[3])));
					break;
				}
			}
		}
	}

	private void calcVanilla(MutableQuadViewImpl quad) {
		//vanillaCalc.compute(blockInfo, quad, ao, light);
	}

	private void calcFastVanilla(MutableQuadViewImpl quad) {
		int flags = quad.geometryFlags();

		if ((flags & LIGHT_FACE_FLAG) == 0 && (flags & AXIS_ALIGNED_FLAG) == AXIS_ALIGNED_FLAG && blockInfo.blockState.isFullCube()) {
			flags |= LIGHT_FACE_FLAG;
		}

		if ((flags & CUBIC_FLAG) == 0) {
			vanillaPartialFace(quad, (flags & LIGHT_FACE_FLAG) != 0);
		} else {
			vanillaFullFace(quad, (flags & LIGHT_FACE_FLAG) != 0);
		}
	}

	private void calcEnhanced(MutableQuadViewImpl quad) {
		switch (quad.geometryFlags()) {
		case AXIS_ALIGNED_FLAG | CUBIC_FLAG | LIGHT_FACE_FLAG:
		case AXIS_ALIGNED_FLAG | LIGHT_FACE_FLAG:
			vanillaPartialFace(quad, true);
			break;

		case AXIS_ALIGNED_FLAG | CUBIC_FLAG:
		case AXIS_ALIGNED_FLAG:
			blendedPartialFace(quad);
			break;

		default:
			irregularFace(quad);
			break;
		}
	}

	private void vanillaFullFace(QuadViewImpl quad, boolean isOnLightFace) {
		final EnumFacing lightFace = quad.lightFace();
		computeFace(lightFace, isOnLightFace).toArray(ao, light, VERTEX_MAP[lightFace.getIndex()]);
	}

	private void vanillaPartialFace(QuadViewImpl quad, boolean isOnLightFace) {
		final EnumFacing lightFace = quad.lightFace();
		AoFaceData faceData = computeFace(lightFace, isOnLightFace);
		final WeightFunction wFunc = AoFace.get(lightFace).weightFunc;
		final float[] w = this.w;

		for (int i = 0; i < 4; i++) {
			wFunc.apply(quad, i, w);
			light[i] = faceData.weightedCombinedLight(w);
			ao[i] = faceData.weigtedAo(w);
		}
	}

	AoFaceData tmpFace = new AoFaceData();

	private AoFaceData blendedInsetFace(QuadViewImpl quad, int vertexIndex, EnumFacing lightFace) {
		final float w1 = AoFace.get(lightFace).depthFunc.apply(quad, vertexIndex);
		final float w0 = 1 - w1;
		return AoFaceData.weightedMean(computeFace(lightFace, true), w0, computeFace(lightFace, false), w1, tmpFace);
	}

	private AoFaceData gatherInsetFace(QuadViewImpl quad, int vertexIndex, EnumFacing lightFace) {
		final float w1 = AoFace.get(lightFace).depthFunc.apply(quad, vertexIndex);

		if (MathHelp.equalsApproximate(w1, 0)) {
			return computeFace(lightFace, true);
		} else if (MathHelp.equalsApproximate(w1, 1)) {
			return computeFace(lightFace, false);
		} else {
			final float w0 = 1 - w1;
			return AoFaceData.weightedMean(computeFace(lightFace, true), w0, computeFace(lightFace, false), w1, tmpFace);
		}
	}

	private void blendedPartialFace(QuadViewImpl quad) {
		final EnumFacing lightFace = quad.lightFace();
		AoFaceData faceData = blendedInsetFace(quad, 0, lightFace);
		final WeightFunction wFunc = AoFace.get(lightFace).weightFunc;

		for (int i = 0; i < 4; i++) {
			wFunc.apply(quad, i, w);
			light[i] = faceData.weightedCombinedLight(w);
			ao[i] = faceData.weigtedAo(w);
		}
	}

	private final Vector3f vertexNormal = new Vector3f();

	private void irregularFace(MutableQuadViewImpl quad) {
		final Vector3f faceNorm = quad.faceNormal();
		Vector3f normal;
		final float[] w = this.w;
		final float[] aoResult = this.ao;

        for (int i = 0; i < 4; i++) {
			normal = quad.hasNormal(i) ? quad.copyNormal(i, vertexNormal) : faceNorm;
			float ao = 0, sky = 0, block = 0, maxAo = 0;
			int maxSky = 0, maxBlock = 0;

			final float x = normal.getX();

			if (!MathHelp.equalsApproximate(0f, x)) {
				final EnumFacing face = x > 0 ? EnumFacing.EAST : EnumFacing.WEST;
				final AoFaceData fd = gatherInsetFace(quad, i, face);
				AoFace.get(face).weightFunc.apply(quad, i, w);
				final float n = x * x;
				final float a = fd.weigtedAo(w);
				final int s = fd.weigtedSkyLight(w);
				final int b = fd.weigtedBlockLight(w);
				ao += n * a;
				sky += n * s;
				block += n * b;
				maxAo = a;
				maxSky = s;
				maxBlock = b;
			}

			final float y = normal.getY();

			if (!MathHelp.equalsApproximate(0f, y)) {
				final EnumFacing face = y > 0 ? EnumFacing.UP : EnumFacing.DOWN;
				final AoFaceData fd = gatherInsetFace(quad, i, face);
				AoFace.get(face).weightFunc.apply(quad, i, w);
				final float n = y * y;
				final float a = fd.weigtedAo(w);
				final int s = fd.weigtedSkyLight(w);
				final int b = fd.weigtedBlockLight(w);
				ao += n * a;
				sky += n * s;
				block += n * b;
				maxAo = Math.max(maxAo, a);
				maxSky = Math.max(maxSky, s);
				maxBlock = Math.max(maxBlock, b);
			}

			final float z = normal.getZ();

			if (!MathHelp.equalsApproximate(0f, z)) {
				final EnumFacing face = z > 0 ? EnumFacing.SOUTH : EnumFacing.NORTH;
				final AoFaceData fd = gatherInsetFace(quad, i, face);
				AoFace.get(face).weightFunc.apply(quad, i, w);
				final float n = z * z;
				final float a = fd.weigtedAo(w);
				final int s = fd.weigtedSkyLight(w);
				final int b = fd.weigtedBlockLight(w);
				ao += n * a;
				sky += n * s;
				block += n * b;
				maxAo = Math.max(maxAo, a);
				maxSky = Math.max(maxSky, s);
				maxBlock = Math.max(maxBlock, b);
			}

			aoResult[i] = (ao + maxAo) * 0.5f;
			this.light[i] = (((int) ((sky + maxSky) * 0.5f) & 0xF0) << 16) | ((int) ((block + maxBlock) * 0.5f) & 0xF0);
		}
	}

	private AoFaceData computeFace(EnumFacing lightFace, boolean isOnBlockFace) {
		final int faceDataIndex = isOnBlockFace ? lightFace.getIndex() : lightFace.getIndex() + 6;
		final int mask = 1 << faceDataIndex;
		final AoFaceData result = faceData[faceDataIndex];

		if ((completionFlags & mask) == 0) {
			completionFlags |= mask;

			final World world = blockInfo.blockView;
			final BlockPos pos = blockInfo.blockPos;
			final BlockPos.MutableBlockPos lightPos = this.lightPos;
			final BlockPos.MutableBlockPos searchPos = this.searchPos;

			if (isOnBlockFace) {
				lightPos.setPos(pos.offset(lightFace));
			} else {
				lightPos.setPos(pos);
			}
			AoFace aoFace = AoFace.get(lightFace);

			searchPos.setPos(lightPos);
			searchPos.setPos(searchPos.offset(aoFace.neighbors[0]));
			final int light0 = brightnessFunc.applyAsInt(searchPos);
			final float ao0 = aoFunc.apply(searchPos);

			searchPos.setPos(lightPos);
			searchPos.setPos(searchPos.offset(aoFace.neighbors[1]));
			final int light1 = brightnessFunc.applyAsInt(searchPos);
			final float ao1 = aoFunc.apply(searchPos);

			searchPos.setPos(lightPos);
			searchPos.setPos(searchPos.offset(aoFace.neighbors[2]));
			final int light2 = brightnessFunc.applyAsInt(searchPos);
			final float ao2 = aoFunc.apply(searchPos);

			searchPos.setPos(lightPos);
			searchPos.setPos(searchPos.offset(aoFace.neighbors[3]));
			final int light3 = brightnessFunc.applyAsInt(searchPos);
			final float ao3 = aoFunc.apply(searchPos);


			searchPos.setPos(lightPos);
			searchPos.setPos(searchPos.offset(aoFace.neighbors[0]));
			if (!Indigo.FIX_SMOOTH_LIGHTING_OFFSET) {
				searchPos.setPos(searchPos.offset(lightFace));
			}
			final boolean isClear0 = world.getLightFromNeighbors(searchPos) == 0;

			searchPos.setPos(lightPos);
			searchPos.setPos(searchPos.offset(aoFace.neighbors[1]));
			if (!Indigo.FIX_SMOOTH_LIGHTING_OFFSET) {
				searchPos.setPos(searchPos.offset(lightFace));
			}
			final boolean isClear1 = world.getLightFromNeighbors(searchPos) == 0;

			searchPos.setPos(lightPos);
			searchPos.setPos(searchPos.offset(aoFace.neighbors[2]));
			if (!Indigo.FIX_SMOOTH_LIGHTING_OFFSET) {
				searchPos.setPos(searchPos.offset(lightFace));
			}
			final boolean isClear2 = world.getLightFromNeighbors(searchPos) == 0;

			searchPos.setPos(lightPos);
			searchPos.setPos(searchPos.offset(aoFace.neighbors[3]));
			if (!Indigo.FIX_SMOOTH_LIGHTING_OFFSET) {
				searchPos.setPos(searchPos.offset(lightFace));
			}
			final boolean isClear3 = world.getLightFromNeighbors(searchPos) == 0;



			int cLight0, cLight1, cLight2, cLight3;
			float cAo0, cAo1, cAo2, cAo3;

			if (!isClear2 && !isClear0) {
				cAo0 = ao0;
				cLight0 = light0;
			} else {
				searchPos.setPos(lightPos);
				searchPos.setPos(searchPos.offset(aoFace.neighbors[0]));
				searchPos.setPos(searchPos.offset(aoFace.neighbors[2]));
				cAo0 = aoFunc.apply(searchPos);
				cLight0 = brightnessFunc.applyAsInt(searchPos);
			}

			if (!isClear3 && !isClear0) {
				cAo1 = ao0;
				cLight1 = light0;
			} else {
				searchPos.setPos(lightPos);
				searchPos.setPos(searchPos.offset(aoFace.neighbors[0]));
				searchPos.setPos(searchPos.offset(aoFace.neighbors[3]));
				cAo1 = aoFunc.apply(searchPos);
				cLight1 = brightnessFunc.applyAsInt(searchPos);
			}

			if (!isClear2 && !isClear1) {
				cAo2 = ao1;
				cLight2 = light1;
			} else {
				searchPos.setPos(lightPos);
				searchPos.setPos(searchPos.offset(aoFace.neighbors[1]));
				searchPos.setPos(searchPos.offset(aoFace.neighbors[2]));
				cAo2 = aoFunc.apply(searchPos);
				cLight2 = brightnessFunc.applyAsInt(searchPos);
			}

			if (!isClear3 && !isClear1) {
				cAo3 = ao1;
				cLight3 = light1;
			} else {
				searchPos.setPos(lightPos);
				searchPos.setPos(searchPos.offset(aoFace.neighbors[1]));
				searchPos.setPos(searchPos.offset(aoFace.neighbors[3]));
				cAo3 = aoFunc.apply(searchPos);
				cLight3 = brightnessFunc.applyAsInt(searchPos);
			}

			int lightCenter;
			searchPos.setPos(pos);
			searchPos.setPos(searchPos.offset(lightFace));


			if (isOnBlockFace || !world.getBlockState(searchPos).isOpaqueCube()) {
				lightCenter = brightnessFunc.applyAsInt(searchPos);
			} else {
				lightCenter = brightnessFunc.applyAsInt(pos);
			}

			float aoCenter = aoFunc.apply(isOnBlockFace ? lightPos : pos);

			result.a0 = (ao3 + ao0 + cAo1 + aoCenter) * 0.25F;
			result.a1 = (ao2 + ao0 + cAo0 + aoCenter) * 0.25F;
			result.a2 = (ao2 + ao1 + cAo2 + aoCenter) * 0.25F;
			result.a3 = (ao3 + ao1 + cAo3 + aoCenter) * 0.25F;

			result.l0(meanBrightness(light3, light0, cLight1, lightCenter));
			result.l1(meanBrightness(light2, light0, cLight0, lightCenter));
			result.l2(meanBrightness(light2, light1, cLight2, lightCenter));
			result.l3(meanBrightness(light3, light1, cLight3, lightCenter));
		}

		return result;
	}

	private static int meanBrightness(int a, int b, int c, int d) {
		if (Indigo.FIX_SMOOTH_LIGHTING_OFFSET) {
			return a == 0 || b == 0 || c == 0 || d == 0 ? meanEdgeBrightness(a, b, c, d) : meanInnerBrightness(a, b, c, d);
		} else {
			return vanillaMeanBrightness(a, b, c, d);
		}
	}

	private static int vanillaMeanBrightness(int a, int b, int c, int d) {
		if (a == 0) a = d;
		if (b == 0) b = d;
		if (c == 0) c = d;
		return a + b + c + d >> 2 & 16711935;
	}

	private static int meanInnerBrightness(int a, int b, int c, int d) {
		return a + b + c + d >> 2 & 16711935;
	}

	private static int nonZeroMin(int a, int b) {
		if (a == 0) return b;
		if (b == 0) return a;
		return Math.min(a, b);
	}

	private static int meanEdgeBrightness(int a, int b, int c, int d) {
		final int min = nonZeroMin(nonZeroMin(a, b), nonZeroMin(c, d));
		return meanInnerBrightness(max(a, min), max(b, min), max(c, min), max(d, min));
	}
}
