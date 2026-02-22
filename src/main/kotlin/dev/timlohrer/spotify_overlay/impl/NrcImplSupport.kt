package dev.timlohrer.spotify_overlay.impl

import dev.timlohrer.lml.data.MediaInfo
import dev.timlohrer.spotify_overlay.SpotifyOverlay
import dev.timlohrer.spotify_overlay.components.SpotifyOverlayComponent
import dev.timlohrer.spotify_overlay.config.ConfigManager
import dev.timlohrer.spotify_overlay.config.HUD_TYPE
import dev.timlohrer.spotify_overlay.config.OverlaySettings
import dev.timlohrer.spotify_overlay.utils.IdentifierAlias
import io.wispforest.owo.ui.container.FlowLayout
import io.wispforest.owo.ui.core.Positioning
import io.wispforest.owo.ui.hud.Hud
import net.minecraft.client.Minecraft

class NrcImplSupport {

    private var customRenderer: CustomRenderer? = null
    private val hudId: IdentifierAlias = IdentifierAlias.fromNamespaceAndPath(SpotifyOverlay.MOD_ID, "nrc_custom_overlay")

    fun disableDefaultConfig() {
        ConfigManager.disableConfig()
    }

    fun enableDefaultConfig() {
        ConfigManager.enableConfig()
    }

    fun isDefaultConfigDisabled(): Boolean {
        return ConfigManager.isConfigDisabled
    }

    fun getActiveSettings(): OverlaySettings {
        return ConfigManager.getActiveSettings()
    }

    fun isRenderEnabled(): Boolean {
        return getActiveSettings().renderOverlay
    }

    fun getScale(): Float {
        return getActiveSettings().scale
    }

    fun getHudType(): HUD_TYPE {
        return getActiveSettings().hudType
    }

    fun getCornerRadius(): Float {
        return getActiveSettings().cornerRadius
    }

    fun isBackgroundVisible(): Boolean {
        return getActiveSettings().showBackground
    }

    fun getColor(): Int {
        return getActiveSettings().color
    }

    fun getSourceFilter(): String {
        return getActiveSettings().sourceFilter
    }

    fun isMarqueeEnabled(): Boolean {
        return getActiveSettings().enableMarquee
    }

    fun getPositionX(): Int {
        return getActiveSettings().positionX
    }

    fun getPositionY(): Int {
        return getActiveSettings().positionY
    }

    fun setRenderEnabled(enabled: Boolean) {
        ConfigManager.updateOverrideSettings(renderOverlay = enabled)
    }

    fun setScale(scale: Float) {
        ConfigManager.updateOverrideSettings(scale = scale)
    }

    fun setHudType(type: HUD_TYPE) {
        ConfigManager.updateOverrideSettings(hudType = type)
    }

    fun setCornerRadius(radius: Float) {
        ConfigManager.updateOverrideSettings(cornerRadius = radius)
    }

    fun setBackgroundVisible(visible: Boolean) {
        ConfigManager.updateOverrideSettings(showBackground = visible)
    }

    fun setColor(color: Int) {
        ConfigManager.updateOverrideSettings(color = color)
    }

    fun setSourceFilter(filter: String) {
        ConfigManager.updateOverrideSettings(sourceFilter = filter)
    }

    fun setMarqueeEnabled(enabled: Boolean) {
        ConfigManager.updateOverrideSettings(enableMarquee = enabled)
    }

    fun setComponentPosition(x: Int, y: Int) {
        ConfigManager.updateOverrideSettings(positionX = x, positionY = y)
        updateComponentPosition()
    }

    fun applySettings(settings: OverlaySettings) {
        ConfigManager.setOverrideSettings(settings)
    }

    fun getCurrentMedia(): MediaInfo? {
        return SpotifyOverlay.currentMedia
    }

    fun getCurrentTitle(): String? {
        return SpotifyOverlay.currentMedia?.title
    }

    fun getCurrentArtist(): String? {
        return SpotifyOverlay.currentMedia?.artist
    }

    fun getCurrentImageUrl(): String? {
        return SpotifyOverlay.currentMedia?.imageUrl
    }

    fun getCurrentPosition(): Double? {
        return SpotifyOverlay.currentMedia?.position
    }

    fun getDuration(): Int? {
        return SpotifyOverlay.currentMedia?.duration
    }

    fun isPlaying(): Boolean {
        return SpotifyOverlay.currentMedia?.isPlaying ?: false
    }

    fun getSource(): String? {
        return SpotifyOverlay.currentMedia?.source
    }

    fun interface CustomRenderer {
        fun render(context: Any, mouseX: Int, mouseY: Int, partialTicks: Float, delta: Float)
    }

    fun registerCustomRenderer(renderer: CustomRenderer) {
        customRenderer = renderer
        // TODO: Replace the default component with a custom one that calls this renderer
    }

    fun unregisterCustomRenderer() {
        customRenderer = null
    }

    fun getCustomRenderer(): CustomRenderer? {
        return customRenderer
    }

    fun hasCustomRenderer(): Boolean {
        return customRenderer != null
    }

    fun addCustomComponent(component: FlowLayout) {
        if (Minecraft.getInstance().player?.level() == null) return
        Hud.add(hudId, { component })
    }

    fun removeCustomComponent() {
        if (Minecraft.getInstance().player?.level() == null) return
        Hud.remove(hudId)
    }

    private fun updateComponentPosition() {
        val settings = getActiveSettings()
        val id = IdentifierAlias.fromNamespaceAndPath(SpotifyOverlay.MOD_ID, "spotify_overlay")

        if (Minecraft.getInstance().player?.level() == null) return

        val component = Hud.getComponent(id)
        component?.positioning(Positioning.absolute(settings.positionX, settings.positionY))
    }

    fun refreshOverlay() {
        SpotifyOverlay.lastDownloadedImageUrl = null
        SpotifyOverlay.lastDownloadedImage = null
    }

    fun initializeListener() {
        SpotifyOverlay.initializeListener()
    }

    fun uninitializeListener() {
        SpotifyOverlay.uninitializeListener()
    }

    fun isListenerRunning(): Boolean {
        return try {
            dev.timlohrer.lml.LocalMediaListener.isRunning
        } catch (e: Exception) {
            false
        }
    }
}