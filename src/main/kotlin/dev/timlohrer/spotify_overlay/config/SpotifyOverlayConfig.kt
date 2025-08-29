package dev.timlohrer.spotify_overlay.config

import dev.timlohrer.spotify_overlay.SpotifyOverlay
import me.shedaniel.autoconfig.ConfigData
import me.shedaniel.autoconfig.annotation.Config
import me.shedaniel.autoconfig.annotation.ConfigEntry

@Config(name = SpotifyOverlay.MOD_ID)
class SpotifyOverlayConfig : ConfigData {
    @ConfigEntry.Gui.Tooltip
    var renderOverlay = true
    @ConfigEntry.Gui.NoTooltip
    var scale: Float = 1f
    @ConfigEntry.Gui.EnumHandler
    @ConfigEntry.Gui.Tooltip
    var hudType: HUD_TYPE = HUD_TYPE.DEFAULT
    @ConfigEntry.Gui.NoTooltip
    var cornerRadius: Float = 4.5f
    @ConfigEntry.Gui.NoTooltip
    var showBackground: Boolean = true
    @ConfigEntry.ColorPicker(allowAlpha = false)
    var color = 0x00BB00
    @ConfigEntry.Gui.Tooltip
    var sourceFilter: String = "Spotify"
    @ConfigEntry.Gui.Tooltip
    var enableMarquee: Boolean = true
}