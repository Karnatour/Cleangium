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

package net.fabricmc.fabric.impl.client.indigo.renderer.aocalc;

import net.fabricmc.fabric.impl.client.indigo.renderer.Indigo;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Implements a fix to prevent luminous blocks from casting AO shade.
 * Will give normal result if fix is disabled.
 */
@FunctionalInterface
public interface AoLuminanceFix {
	float apply(World view, BlockPos pos);

	AoLuminanceFix INSTANCE = Indigo.FIX_LUMINOUS_AO_SHADE ? AoLuminanceFix::fixed : AoLuminanceFix::vanilla;

	static float vanilla(World view, BlockPos pos) {
		return view.getBlockState(pos).getAmbientOcclusionLightValue();
	}

	static float fixed(World view, BlockPos pos) {
		final IBlockState state = view.getBlockState(pos);
		return state.getLightValue(view,pos) == 0 ? state.getAmbientOcclusionLightValue() : 1f;
	}
}
