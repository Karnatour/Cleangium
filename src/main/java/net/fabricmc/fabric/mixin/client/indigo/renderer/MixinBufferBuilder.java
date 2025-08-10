/*
 * Copyright (c) 2016, 2017, 2018, 2019 FabricMC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.fabricmc.fabric.mixin.client.indigo.renderer;

import java.nio.IntBuffer;

import net.fabricmc.fabric.impl.client.indigo.renderer.Indigo;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import net.minecraft.util.EnumFacing;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import net.fabricmc.fabric.impl.client.indigo.renderer.accessor.AccessBufferBuilder;
import net.fabricmc.fabric.impl.client.indigo.renderer.mesh.QuadViewImpl;

@Mixin(BufferBuilder.class)
public abstract class MixinBufferBuilder implements AccessBufferBuilder {

    @Shadow
    private IntBuffer rawIntBuffer;
    @Shadow
    private int vertexCount;

    @Shadow
    protected abstract void growBuffer(int size);

    @Shadow
    protected abstract int getBufferSize();

    @Shadow
    public abstract VertexFormat getVertexFormat();

    private static final int VERTEX_STRIDE_INTS = 7;
    private static final int QUAD_STRIDE_INTS = VERTEX_STRIDE_INTS * 4;
    private static final int QUAD_STRIDE_BYTES = QUAD_STRIDE_INTS * 4;
    private static int test = 0;

    @Override
    public void fabric_putQuad(QuadViewImpl quad) {
/*        if (Indigo.ENSURE_VERTEX_FORMAT_COMPATIBILITY) {
            bufferCompatibly(quad);
        } else {
            bufferFast(quad);
        }*/
        bufferCompatibly(quad);
    }

    private void bufferFast(QuadViewImpl quad) {
        growBuffer(QUAD_STRIDE_BYTES);
        rawIntBuffer.position(getBufferSize());
        rawIntBuffer.put(quad.data(), quad.vertexStart(), QUAD_STRIDE_INTS);
        vertexCount += 4;
    }

    /**
     * Uses buffer vertex format to drive buffer population.
     * Relies on logic elsewhere to ensure coordinates don't include chunk offset
     * (because buffer builder will handle that.)
     *
     * <p>Calling putVertexData() would likely be a little faster but this approach
     * gives us a chance to pass vertex normals to shaders, which isn't possible
     * with the standard block format. It also doesn't require us to encode a specific
     * custom format directly, which would be prone to breakage outside our control.
     */
    private void bufferCompatibly(QuadViewImpl quad) {
        VertexFormat format = getVertexFormat();
        final int elementCount = format.getElementCount();

        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < elementCount; j++) {
                VertexFormatElement e = format.getElement(j);
                switch (e.getUsage()) {
                    case VertexFormatElement.EnumUsage.COLOR:
                        final int c = quad.spriteColor(i, 0);
                        ((BufferBuilder) (Object) this).color(c & 0xFF, (c >>> 8) & 0xFF, (c >>> 16) & 0xFF, (c >>> 24) & 0xFF);
                        break;
                    case VertexFormatElement.EnumUsage.NORMAL:
                        ((BufferBuilder) (Object) this).normal(quad.normalX(i), quad.normalY(i), quad.normalZ(i));
                        break;
                    case VertexFormatElement.EnumUsage.POSITION:
                        ((BufferBuilder) (Object) this).pos(quad.x(i), quad.y(i), quad.z(i));
                        break;
                    case VertexFormatElement.EnumUsage.UV:
                        if (e.getIndex() == 0) {
                            ((BufferBuilder) (Object) this).tex(quad.spriteU(i, 0), quad.spriteV(i, 0));
                        } else {
                            final int b = quad.lightmap(i);

                            //TODO Fix ?????????????????
                            int skyLight = 0;
                            int blockLight = 0;
                            for (int shift = 24; shift >= 0; shift -= 8) {
                                int currentByte = (b >> shift) & 0xFF;
                                if (currentByte != 0) {
                                    skyLight = currentByte;
                                    int mask = (1 << shift) - 1;
                                    blockLight = b & mask;
                                    break;
                                }
                            }

                            ((BufferBuilder) (Object) this).lightmap(skyLight, blockLight);
                        }

                        break;

                    // these types should never occur and/or require no action
                    case VertexFormatElement.EnumUsage.MATRIX:
                    case VertexFormatElement.EnumUsage.BLEND_WEIGHT:
                    case VertexFormatElement.EnumUsage.PADDING:
                    default:
                        break;
                }
            }

            ((BufferBuilder) (Object) this).endVertex();
        }
    }
}
