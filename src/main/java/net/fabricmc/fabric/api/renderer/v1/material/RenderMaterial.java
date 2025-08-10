package net.fabricmc.fabric.api.renderer.v1.material;

import net.minecraft.block.Block;

import net.fabricmc.fabric.api.renderer.v1.Renderer;
import net.fabricmc.fabric.api.renderer.v1.mesh.MeshBuilder;
import net.fabricmc.fabric.api.renderer.v1.mesh.MutableQuadView;
import net.minecraft.util.ResourceLocation;

public interface RenderMaterial {
	ResourceLocation MATERIAL_STANDARD = new ResourceLocation("fabric", "standard");

	int spriteDepth();
}
