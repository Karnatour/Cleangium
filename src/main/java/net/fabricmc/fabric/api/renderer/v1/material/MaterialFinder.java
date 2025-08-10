package net.fabricmc.fabric.api.renderer.v1.material;

import net.minecraft.block.Block;

import net.fabricmc.fabric.api.renderer.v1.Renderer;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.minecraft.util.BlockRenderLayer;

public interface MaterialFinder {
	RenderMaterial find();

	MaterialFinder clear();

	MaterialFinder spriteDepth(int depth);

	MaterialFinder blendMode(int spriteIndex, BlockRenderLayer blendMode);

	MaterialFinder disableColorIndex(int spriteIndex, boolean disable);

	MaterialFinder disableDiffuse(int spriteIndex, boolean disable);

	MaterialFinder disableAo(int spriteIndex, boolean disable);

	MaterialFinder emissive(int spriteIndex, boolean isEmissive);
}
