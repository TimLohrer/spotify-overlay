package dev.timlohrer.spotify_overlay.config

data class OverlaySettings(
    val renderOverlay: Boolean = true,
    val scale: Float = 1f,
    val hudType: HUD_TYPE = HUD_TYPE.DEFAULT,
    val cornerRadius: Float = 4.5f,
    val showBackground: Boolean = true,
    val color: Int = 0x00BB00,
    val sourceFilter: String = "Spotify",
    val enableMarquee: Boolean = true,
    val positionX: Int = 10,
    val positionY: Int = 10
) {
    companion object {
        fun fromConfig(config: SpotifyOverlayConfig): OverlaySettings {
            return OverlaySettings(
                renderOverlay = config.renderOverlay,
                scale = config.scale,
                hudType = config.hudType,
                cornerRadius = config.cornerRadius,
                showBackground = config.showBackground,
                color = config.color,
                sourceFilter = config.sourceFilter,
                enableMarquee = config.enableMarquee
            )
        }

        fun default(): OverlaySettings = OverlaySettings()
    }
}

