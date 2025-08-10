package net.fabricmc.fabric.api.renderer.v1;

import net.fabricmc.fabric.api.renderer.v1.material.MaterialFinder;
import net.fabricmc.fabric.api.renderer.v1.material.RenderMaterial;
import net.fabricmc.fabric.api.renderer.v1.mesh.MeshBuilder;
import net.minecraft.util.ResourceLocation;

public interface Renderer {
	MeshBuilder meshBuilder();

	MaterialFinder materialFinder();

	RenderMaterial materialById(ResourceLocation id);

	boolean registerMaterial(ResourceLocation id, RenderMaterial material);
}
