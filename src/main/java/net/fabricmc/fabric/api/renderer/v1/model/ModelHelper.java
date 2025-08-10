package net.fabricmc.fabric.api.renderer.v1.model;

import com.google.common.collect.ImmutableList;
import net.fabricmc.fabric.api.renderer.v1.mesh.Mesh;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.util.EnumFacing;

import java.util.Arrays;
import java.util.List;

public abstract class ModelHelper {
    private ModelHelper() {
    }

    public static final int NULL_FACE_ID = 6;

    public static int toFaceIndex(EnumFacing face) {
        return face == null ? NULL_FACE_ID : face.getIndex();
    }

    public static EnumFacing faceFromIndex(int faceIndex) {
        return FACES[faceIndex];
    }

    private static final EnumFacing[] FACES = Arrays.copyOf(EnumFacing.values(), 7);

    public static List<BakedQuad>[] toQuadLists(Mesh mesh) {
        SpriteFinder finder = SpriteFinder.get(Minecraft.getMinecraft().getTextureMapBlocks());

        @SuppressWarnings("unchecked") final ImmutableList.Builder<BakedQuad>[] builders = new ImmutableList.Builder[7];

        for (int i = 0; i < 7; i++) {
            builders[i] = ImmutableList.builder();
        }

        mesh.forEach(q -> {
            final int limit = q.material().spriteDepth();

            for (int l = 0; l < limit; l++) {
                EnumFacing face = q.cullFace();
                builders[face == null ? 6 : face.getIndex()].add(q.toBakedQuad(l, finder.find(q, l), false));
            }
        });

        @SuppressWarnings("unchecked")
        List<BakedQuad>[] result = new List[7];

        for (int i = 0; i < 7; i++) {
            result[i] = builders[i].build();
        }

        return result;
    }

}
