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

package net.fabricmc.fabric.impl.client.indigo.renderer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.fabricmc.fabric.api.renderer.v1.RendererAccess;
import net.fabricmc.fabric.impl.client.indigo.renderer.aocalc.AoConfig;

@Mod(modid = "cleangium", name = "Cleangium", version = "0.1.0")
public class Indigo{
	public static final boolean ALWAYS_TESSELATE_INDIGO = true;
	public static final boolean ENSURE_VERTEX_FORMAT_COMPATIBILITY;
	public static final AoConfig AMBIENT_OCCLUSION_MODE = AoConfig.ENHANCED;
	public static final boolean DEBUG_COMPARE_LIGHTING = true;
	public static final boolean FIX_SMOOTH_LIGHTING_OFFSET = false;
	public static final boolean FIX_EXTERIOR_VERTEX_LIGHTING = false;
	public static final boolean FIX_LUMINOUS_AO_SHADE = false;

	public static final Logger LOGGER = LogManager.getLogger();

	@SuppressWarnings({"rawtypes", "unchecked"})
	private static <T extends Enum> T asEnum(String property, T defValue) {
		if (property == null || property.isEmpty()) return defValue;
		for (Enum obj : defValue.getClass().getEnumConstants()) {
			if (property.equalsIgnoreCase(obj.name())) {
				return (T) obj;
			}
		}
		return defValue;
	}

	static {
		File configDir = new File(Loader.instance().getConfigDir(), "indigo");
		if (!configDir.exists() && !configDir.mkdir()) {
			LOGGER.warn("[Indigo] Could not create configuration directory: " + configDir.getAbsolutePath());
		}

		File configFile = new File(configDir, "indigo-renderer.properties");
		Properties properties = new Properties();

		if (configFile.exists()) {
			try (FileInputStream stream = new FileInputStream(configFile)) {
				properties.load(stream);
			} catch (IOException e) {
				LOGGER.warn("[Indigo] Could not read property file: " + configFile.getAbsolutePath(), e);
			}
		}

		final boolean forceCompatibility = true;

		ENSURE_VERTEX_FORMAT_COMPATIBILITY = forceCompatibility;

		try (FileOutputStream stream = new FileOutputStream(configFile)) {
			properties.store(stream, "Indigo Renderer Configuration");
		} catch (IOException e) {
			LOGGER.warn("[Indigo] Could not save property file: " + configFile.getAbsolutePath(), e);
		}

		RendererAccess.INSTANCE.registerRenderer(IndigoRenderer.INSTANCE);

	}

	@Mod.EventHandler
	public void onInit(FMLInitializationEvent event) {
		MinecraftForge.EVENT_BUS.register(this);
	}

	@SubscribeEvent
	public void onF3Text(RenderGameOverlayEvent.Text event) {
		if (!Minecraft.getMinecraft().gameSettings.showDebugInfo) {
			return;
		}

		event.getLeft().add("[Cleangium] Active Renderer: " + RendererAccess.INSTANCE.getRenderer().getClass().getSimpleName());
	}
}
