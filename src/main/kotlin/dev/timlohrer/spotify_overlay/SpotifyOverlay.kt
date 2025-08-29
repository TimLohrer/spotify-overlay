package dev.timlohrer.spotify_overlay

import dev.timlohrer.lml.LocalMediaListener
import dev.timlohrer.lml.data.MediaInfo
import dev.timlohrer.spotify_overlay.components.SpotifyOverlayComponent
import dev.timlohrer.spotify_overlay.config.HUD_TYPE
import dev.timlohrer.spotify_overlay.config.SpotifyOverlayConfig
import dev.timlohrer.spotify_overlay.utils.ImageHandler
import dev.timlohrer.spotify_overlay.utils.Logger
import io.wispforest.owo.ui.core.Positioning
import io.wispforest.owo.ui.hud.Hud
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.shedaniel.autoconfig.AutoConfig
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents
import net.minecraft.client.MinecraftClient
import net.minecraft.client.option.KeyBinding
import net.minecraft.client.util.InputUtil
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import net.silkmc.silk.core.text.literal
import org.lwjgl.glfw.GLFW

object SpotifyOverlay : ModInitializer {
	const val MOD_ID = "spotify_overlay"

	private lateinit var backKeybinding: KeyBinding
	private lateinit var playPauseKeybinding: KeyBinding
	private lateinit var nextKeybinding: KeyBinding
	
	fun getConfig() = AutoConfig.getConfigHolder(SpotifyOverlayConfig::class.java).config

	var currentMedia: MediaInfo? = null
	var lastDownloadedImageUrl: String? = null
	var lastDownloadedImage: Identifier? = null
	var knownSources = mutableListOf<String>()
	
	// this is really hacky LOL
	internal var _shouldRender: Boolean? = null
	internal var _sourceFilter: String? = null
	internal var _cornerRadius: Float? = null
	internal var _hudType: HUD_TYPE? = null

	fun String.toId(): Identifier = Identifier.of(MOD_ID, this)

	override fun onInitialize() {
		AutoConfig.register(SpotifyOverlayConfig::class.java, ::GsonConfigSerializer)

		ServerPlayConnectionEvents.JOIN.register { handler, sender, client ->
			if (!getConfig().renderOverlay) return@register
			initializeListener()
			Hud.add("spotify_overlay".toId(), {
				SpotifyOverlayComponent().apply {
					positioning(Positioning.absolute(10, 10))
				}
			})
		}

		backKeybinding = KeyBindingHelper.registerKeyBinding(
				KeyBinding(
					"key.spotify_overlay.back",
					InputUtil.Type.KEYSYM,
					GLFW.GLFW_KEY_F7,
					"category.spotify_overlay"
				)
		)
		playPauseKeybinding = KeyBindingHelper.registerKeyBinding(
				KeyBinding(
					"key.spotify_overlay.play_pause",
					InputUtil.Type.KEYSYM,
					GLFW.GLFW_KEY_F8,
					"category.spotify_overlay"
				)
		)
		nextKeybinding = KeyBindingHelper.registerKeyBinding(
				KeyBinding(
					"key.spotify_overlay.next",
					InputUtil.Type.KEYSYM,
					GLFW.GLFW_KEY_F9,
					"category.spotify_overlay"
				)
		)

		ClientTickEvents.END_CLIENT_TICK.register { client ->
			if (!getConfig().renderOverlay || !LocalMediaListener.isRunning || currentMedia == null) return@register
			while (backKeybinding.wasPressed()) {
				LocalMediaListener.back(currentMedia!!.source)	
			}
			while (playPauseKeybinding.wasPressed()) {
				LocalMediaListener.playPause(currentMedia!!.source)
			}
			while (nextKeybinding.wasPressed()) {
				LocalMediaListener.next(currentMedia!!.source)
			}
		}
		
		CoroutineScope(Dispatchers.IO).launch {
			while (true) {
				if (getConfig() != null) {
					if (_shouldRender != getConfig().renderOverlay) {
						_shouldRender = getConfig().renderOverlay
						// If the render setting changes, initialize or uninitialize the listener
						if (_shouldRender == true) {
							initializeListener()
						} else {
							uninitializeListener()
						}
					}
					if (_sourceFilter != getConfig().sourceFilter) {
						_sourceFilter = getConfig().sourceFilter
						// Reset current media if the source filter changes
						if (currentMedia != null &&  !shouldShowMedia(currentMedia!!.source)) {
							currentMedia = null
						}
					}
					if (_cornerRadius != getConfig().cornerRadius || _hudType != getConfig().hudType) {
						_cornerRadius = getConfig().cornerRadius
						_hudType = getConfig().hudType
						// Update corner radius in the overlay component
						ImageHandler.softClearCache()
						lastDownloadedImageUrl = null
						lastDownloadedImage = null
					}
					delay(500)
				}
			}
		}
	}
	
	fun initializeListener() {
		try {
			LocalMediaListener.initialize {
				Logger.info("SpotifyOverlay listener initialized")
				LocalMediaListener.onMediaChange { mediaInfo ->
					if (currentMedia == null || mediaInfo.isPlaying) {
						if (!knownSources.contains(mediaInfo.source)) {
							knownSources.add(mediaInfo.source.trim())
							MinecraftClient.getInstance().player?.sendMessage("§a§lSpotify§fOverlay §r§b» §rNew media source detected: ${if (mediaInfo.source.contains("Spotify")) "Spotify" else mediaInfo.source}.".literal, false)
							Logger.info("New media source detected: "+mediaInfo.source)
						}

						if (shouldShowMedia(mediaInfo.source)) {
							currentMedia = mediaInfo
						}
					}
				}
			}
		} catch (e: Exception) {
			Logger.error("Failed to initialize LocalMediaListener: "+e.message, e)
			return
		}
		
		if (MinecraftClient.getInstance().player?.world == null) return
		Hud.add("spotify_overlay".toId(), {
			SpotifyOverlayComponent().apply {
				positioning(Positioning.absolute(10, 10))
			}
		})
	}
	
	fun uninitializeListener() {
		try {
			if (!LocalMediaListener.isAvailable()) return
			if (LocalMediaListener.isRunning) {
				LocalMediaListener.closeHook()
			}
		} catch (e: Exception) {
			Logger.error("Failed to properly close LocalMediaListener: "+e.message, e)
		}
		if (MinecraftClient.getInstance().player?.world == null) return
		Hud.remove("spotify_overlay".toId())
		Logger.info("SpotifyOverlay listener shut down")
	}

	fun shouldShowMedia(source: String): Boolean {
		val sourceFilter = getConfig().sourceFilter
		if (sourceFilter.isEmpty()) return true
		val splitFilter = sourceFilter.split(",").map { it.trim() }
		return splitFilter.any { source.contains(it, ignoreCase = true) }
	}
	
	fun clearCache() {
		ImageHandler.clearCache()
		MinecraftClient.getInstance().player?.sendMessage(Text.translatable("spotify_overlay.cache_cleared"), true)
	}
}