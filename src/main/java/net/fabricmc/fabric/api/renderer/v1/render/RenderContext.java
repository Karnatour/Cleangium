package net.fabricmc.fabric.api.renderer.v1.render;

import java.util.function.Consumer;


import net.fabricmc.fabric.api.renderer.v1.mesh.Mesh;
import net.fabricmc.fabric.api.renderer.v1.mesh.MutableQuadView;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.minecraft.client.renderer.block.model.IBakedModel;

public interface RenderContext {
	Consumer<Mesh> meshConsumer();

	Consumer<IBakedModel> fallbackConsumer();

	QuadEmitter getEmitter();

	void pushTransform(QuadTransform transform);

	void popTransform();

	@FunctionalInterface
	public interface QuadTransform {
		boolean transform(MutableQuadView quad);
	}
}
