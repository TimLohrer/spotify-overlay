package dev.timlohrer.spotify_overlay

import com.mojang.blaze3d.platform.InputConstants
import dev.timlohrer.lml.LocalMediaListener
import dev.timlohrer.lml.data.MediaInfo
import dev.timlohrer.spotify_overlay.components.SpotifyOverlayComponent
import dev.timlohrer.spotify_overlay.config.ConfigManager
import dev.timlohrer.spotify_overlay.config.HUD_TYPE
import dev.timlohrer.spotify_overlay.config.SpotifyOverlayConfig
import dev.timlohrer.spotify_overlay.utils.IdentifierAlias
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
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents
import net.minecraft.client.KeyMapping
import net.minecraft.client.Minecraft
import net.minecraft.network.chat.Component

import org.lwjgl.glfw.GLFW

object SpotifyOverlay : ModInitializer {
	const val MOD_ID = "spotify_overlay"

	private lateinit var backKeybinding: KeyMapping
	private lateinit var playPauseKeybinding: KeyMapping
	private lateinit var nextKeybinding: KeyMapping
    //? if >= 1.21.10 {
    private var spotifyCategory = KeyMapping.Category.register(IdentifierAlias.parse("category.spotify_overlay"))
    //?}
 
    fun getConfig() = AutoConfig.getConfigHolder(SpotifyOverlayConfig::class.java).config

	fun getActiveSettings() = ConfigManager.getActiveSettings()

	var currentMedia: MediaInfo? = null
	var lastDownloadedImageUrl: String? = null
	var lastDownloadedImage: IdentifierAlias? = null
	var knownSources = mutableListOf<String>()
	
	// this is really hacky LOL
	internal var _shouldRender: Boolean? = null
	internal var _sourceFilter: String? = null
	internal var _cornerRadius: Float? = null
	internal var _hudType: HUD_TYPE? = null

	fun String.toId(): IdentifierAlias = IdentifierAlias.fromNamespaceAndPath(MOD_ID, this)

	override fun onInitialize() {
		AutoConfig.register(SpotifyOverlayConfig::class.java, ::GsonConfigSerializer)

		ServerPlayConnectionEvents.JOIN.register { handler, sender, client ->
			if (!getActiveSettings().renderOverlay) return@register
			initializeListener()
			val settings = getActiveSettings()
			Hud.add("spotify_overlay".toId(), {
				SpotifyOverlayComponent().apply {
					positioning(Positioning.absolute(settings.positionX, settings.positionY))
				}
			})
		}

        //wer auch immer mojang in den kopf geschissen hat mit diesen kategorien ab 1.21.10 soll sich alt f4n

        backKeybinding = KeyBindingHelper.registerKeyBinding(
			KeyMapping(
					"key.spotify_overlay.back",
				InputConstants.Type.KEYSYM,
					GLFW.GLFW_KEY_F7,
                    //? if < 1.21.10 {
					/*"category.spotify_overlay"
                    *///?} elif >= 1.21.10 {
                    spotifyCategory
                    //?}
				)
		)
		playPauseKeybinding = KeyBindingHelper.registerKeyBinding(
			KeyMapping(
					"key.spotify_overlay.play_pause",
					InputConstants.Type.KEYSYM,
					GLFW.GLFW_KEY_F8,
                    //? if < 1.21.10 {
                    /*"category.spotify_overlay"
                    *///?} elif >= 1.21.10 {
                    spotifyCategory
                    //?}
				)
		)
		nextKeybinding = KeyBindingHelper.registerKeyBinding(
			KeyMapping(
					"key.spotify_overlay.next",
				InputConstants.Type.KEYSYM,
					GLFW.GLFW_KEY_F9,
                    //? if < 1.21.10 {
					/*"category.spotify_overlay"
                    *///?} elif >= 1.21.10 {
                    spotifyCategory
                    //?}
				)
		)

		ClientTickEvents.END_CLIENT_TICK.register { client ->
			if (!getActiveSettings().renderOverlay || !LocalMediaListener.isRunning || currentMedia == null) return@register
			while (backKeybinding.isDown()) {
				LocalMediaListener.back(currentMedia!!.source)	
			}
			while (playPauseKeybinding.isDown()) {
				LocalMediaListener.playPause(currentMedia!!.source)
			}
			while (nextKeybinding.isDown()) {
				LocalMediaListener.next(currentMedia!!.source)
			}
		}
		
		CoroutineScope(Dispatchers.IO).launch {
			while (true) {
				if (getConfig() != null) {
					val settings = getActiveSettings()
					if (_shouldRender != settings.renderOverlay) {
						_shouldRender = settings.renderOverlay
						// If the render setting changes, initialize or uninitialize the listener
						if (_shouldRender == true) {
							initializeListener()
						} else {
							uninitializeListener()
						}
					}
					if (_sourceFilter != settings.sourceFilter) {
						_sourceFilter = settings.sourceFilter
						// Reset current media if the source filter changes
						if (currentMedia != null &&  !shouldShowMedia(currentMedia!!.source)) {
							currentMedia = null
						}
					}
					if (_cornerRadius != settings.cornerRadius || _hudType != settings.hudType) {
						_cornerRadius = settings.cornerRadius
						_hudType = settings.hudType
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
							Minecraft.getInstance().player?.displayClientMessage(Component.literal("§a§lSpotify§fOverlay §r§b» §rNew media source detected: ${if (mediaInfo.source.contains("Spotify")) "Spotify" else mediaInfo.source}.") , false)
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
		
		if (Minecraft.getInstance().player?.level() == null) return
		val settings = getActiveSettings()
		Hud.add("spotify_overlay".toId(), {
			SpotifyOverlayComponent().apply {
				positioning(Positioning.absolute(settings.positionX, settings.positionY))
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
		if (Minecraft.getInstance().player?.level() == null) return
		Hud.remove("spotify_overlay".toId())
		clearCache()
		Logger.info("SpotifyOverlay listener shut down")
	}

	fun shouldShowMedia(source: String): Boolean {
		val sourceFilter = getActiveSettings().sourceFilter
		if (sourceFilter.isEmpty()) return true
		val splitFilter = sourceFilter.split(",").map { it.trim() }
		return splitFilter.any { source.contains(it, ignoreCase = true) }
	}
	
	fun clearCache() {
		ImageHandler.clearCache()
		Minecraft.getInstance().player?.displayClientMessage(Component.translatable("spotify_overlay.cache_cleared"), true)
	}
}