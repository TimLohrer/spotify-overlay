package dev.timlohrer.spotify_overlay.config

import dev.timlohrer.spotify_overlay.SpotifyOverlay

object ConfigManager {
    var isConfigDisabled: Boolean = false
        private set

    var overrideSettings: OverlaySettings = OverlaySettings.default()
        private set

    fun getActiveSettings(): OverlaySettings {
        return if (isConfigDisabled) {
            overrideSettings
        } else {
            OverlaySettings.fromConfig(SpotifyOverlay.getConfig())
        }
    }

    fun disableConfig() {
        isConfigDisabled = true
    }

    fun enableConfig() {
        isConfigDisabled = false
    }

    fun setOverrideSettings(settings: OverlaySettings) {
        overrideSettings = settings
    }

    fun updateOverrideSettings(
        renderOverlay: Boolean? = null,
        scale: Float? = null,
        hudType: HUD_TYPE? = null,
        cornerRadius: Float? = null,
        showBackground: Boolean? = null,
        color: Int? = null,
        sourceFilter: String? = null,
        enableMarquee: Boolean? = null,
        positionX: Int? = null,
        positionY: Int? = null
    ) {
        overrideSettings = OverlaySettings(
            renderOverlay = renderOverlay ?: overrideSettings.renderOverlay,
            scale = scale ?: overrideSettings.scale,
            hudType = hudType ?: overrideSettings.hudType,
            cornerRadius = cornerRadius ?: overrideSettings.cornerRadius,
            showBackground = showBackground ?: overrideSettings.showBackground,
            color = color ?: overrideSettings.color,
            sourceFilter = sourceFilter ?: overrideSettings.sourceFilter,
            enableMarquee = enableMarquee ?: overrideSettings.enableMarquee,
            positionX = positionX ?: overrideSettings.positionX,
            positionY = positionY ?: overrideSettings.positionY
        )
    }
}

