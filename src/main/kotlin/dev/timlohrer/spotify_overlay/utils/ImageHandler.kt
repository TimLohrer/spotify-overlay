package dev.timlohrer.spotify_overlay.utils

import com.mojang.blaze3d.systems.RenderSystem
import dev.timlohrer.spotify_overlay.SpotifyOverlay.toId
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.texture.NativeImage
import net.minecraft.client.texture.NativeImageBackedTexture
import net.minecraft.util.Identifier
import java.awt.image.BufferedImage
import java.io.File
import java.io.FileInputStream
import java.net.URI
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.util.HashMap
import java.util.UUID
import javax.imageio.ImageIO

/**
 * COPIED AND MODIFIED FROM https://github.com/LeonimusTTV/SpotiCraft/blob/master/src/main/java/com/leonimust/spoticraft/common/client/ui/ImageHandler.java
 * <333
 */

internal object ImageHandler {
    private val MC = MinecraftClient.getInstance()
    private val CACHE = HashMap<String, Identifier>()
    private val CACHE_DIR = File(MC.runDirectory, "spotify-overlay/cache")
    val EMPTY = "textures/spotify/empty.png".toId() // this doesnt even exist, but it is used as a placeholder

    init {
        if (!CACHE_DIR.exists()) {
            val result = CACHE_DIR.mkdirs()
            if (!result) {
                throw RuntimeException("Unable to create directory $CACHE_DIR")
            }
        }
    }
    
    fun clearCache() {
        CACHE.clear()
        CACHE_DIR.listFiles()?.forEach { file ->
            if (file.isFile) {
                file.delete()
            }
        }
        Logger.info("Spotify cache cleared.")
    }
    
    fun softClearCache() {
        CACHE.clear()
    }
    
    fun deleteOldestCachedFile() {
        val oldestFile = CACHE_DIR.listFiles()?.minByOrNull { it.lastModified() }
        if (oldestFile != null && oldestFile.delete()) {
            Logger.info("Deleted oldest cached file: ${oldestFile.name}")
        }
    }

    fun drawImage(
        context: DrawContext,
        musicImage: Identifier,
        x: Int,
        y: Int,
        height: Int,
        width: Int
    ) {
        val texture = MC.textureManager.getTexture(musicImage)?.glTexture ?: return
        RenderSystem.setShaderTexture(0, texture)

        context.drawTexture(
            { id -> RenderLayer.getGuiTextured(id) },
            musicImage,
            x,
            y,
            0f,
            0f,
            width,
            height,
            width,
            height
        )
    }
    
    fun getFileNameFromUrl(url: String): String {
        return UUID.nameUUIDFromBytes(url.toByteArray()).toString() + ".png"
    }

    fun downloadImage(url: String, cornerRadius: Int, topLeft: Boolean = true, topRight: Boolean = true, bottomLeft: Boolean = true, bottomRight: Boolean = true): Identifier {
        try {
            if (url.startsWith("http")) {
                Logger.info("Downloading $url")
                CACHE[url]?.let {
                    Logger.info("Found cached image for $url: $it")
                    return it
                }
                val fileName = getFileNameFromUrl(url)
                val cachedFile = File(CACHE_DIR, fileName)
                if (cachedFile.exists()) {
                    Logger.info("Image found in cache: ${cachedFile.absolutePath}")
                    return loadFromDisk(cachedFile, url, cornerRadius, topLeft, topRight, bottomLeft, bottomRight)
                }
                val imageUrl = URI(url).toURL()
                imageUrl.openStream().use { input ->
                    Files.copy(input, cachedFile.toPath())
                }
                if (CACHE_DIR.listFiles().size > 10) {
                    deleteOldestCachedFile()
                }
                Logger.info("Downloaded image to $url")
                if (isWebP(cachedFile)) {
                    Logger.info("Converting to PNG...")
                    val pngFile = convertWebPToPng(cachedFile)
                    return loadFromDisk(pngFile, url, cornerRadius, topLeft, topRight, bottomLeft, bottomRight)
                }
                return loadFromDisk(cachedFile, url, cornerRadius, topLeft, topRight, bottomLeft, bottomRight)
            } else if (url.startsWith("data:image/png;base64,")) {
                CACHE[url]?.let {
                    Logger.info("Found cached image for BASE64 URL: $it")
                    return it
                }
                val fileName = getFileNameFromUrl(url)
                val cachedFile = File(CACHE_DIR, fileName)
                if (cachedFile.exists()) {
                    Logger.info("Image found in cache: ${cachedFile.absolutePath}")
                    return loadFromDisk(cachedFile, url, cornerRadius, topLeft, topRight, bottomLeft, bottomRight)
                }
                // Handle base64 encoded images
                val base64Data = url.removePrefix("data:image/png;base64,")
                val imageBytes = java.util.Base64.getDecoder().decode(base64Data)
                val bufferedImage = ImageIO.read(imageBytes.inputStream()) ?: return EMPTY
                ImageIO.write(bufferedImage, "png", cachedFile)

                if (CACHE_DIR.listFiles().size > 10) {
                    deleteOldestCachedFile()
                }

                if (bufferedImage.width != bufferedImage.height) {
                    val size = minOf(bufferedImage.width, bufferedImage.height)
                    val resizedImage = BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB)
                    val graphics = resizedImage.createGraphics()
                    graphics.drawImage(bufferedImage, 0, 0, size, size, null)
                    graphics.dispose()
                    ImageIO.write(resizedImage, "png", cachedFile)
                }

                return loadFromDisk(cachedFile, url, cornerRadius, topLeft, topRight, bottomLeft, bottomRight)
            } else {
                Logger.warn("Unsupported URL format: $url")
                return EMPTY
            }
        } catch (e: Exception) {
            Logger.error("Failed to load image from $url: ${e.message}", e)
            return EMPTY
        }
    }

    private fun loadFromDisk(file: File, url: String, cornerRadius: Int, topLeft: Boolean, topRight: Boolean, bottomLeft: Boolean, bottomRight: Boolean): Identifier {
        Logger.info("Loading cached image: ${file.absolutePath}")
        val bufferedImage = ImageIO.read(file) ?: return EMPTY
        val nativeImage = convertToNativeImage(bufferedImage)

        if (cornerRadius > 0) {
            applyRoundedCorners(nativeImage, cornerRadius, topLeft, topRight, bottomLeft, bottomRight)
        }
        
        val id = "spotify_cover_${UUID.randomUUID()}"
        val dynamicTexture = NativeImageBackedTexture({ id }, nativeImage)
        val textureLocation = id.toId()
        
        Logger.info("Registering texture: $textureLocation for URL: ${url.split("base64,").first()}")

        MC.textureManager.registerTexture(textureLocation, dynamicTexture)
        CACHE[url] = textureLocation
        return textureLocation
    }

    private fun convertToNativeImage(bufferedImage: BufferedImage): NativeImage {
        val width = bufferedImage.width
        val height = bufferedImage.height
        val nativeImage = NativeImage(NativeImage.Format.RGBA, width, height, true)

        for (x in 0 until width) {
            for (y in 0 until height) {
                val argb = bufferedImage.getRGB(x, y)

                val a = (argb shr 24) and 0xFF
                val r = (argb shr 16) and 0xFF
                val g = (argb shr 8) and 0xFF
                val b = argb and 0xFF

                // Some NativeImage implementations use ABGR instead of ARGB
                val abgr = (a shl 24) or (b shl 16) or (g shl 8) or r
                nativeImage.setColor(x, y, abgr)
            }
        }

        return nativeImage
    }

    private fun isWebP(file: File): Boolean {
        return try {
            FileInputStream(file).use { input ->
                val magicBytes = input.readNBytes(12)
                val header = String(magicBytes, StandardCharsets.US_ASCII)
                "WEBP" in header
            }
        } catch (e: Exception) {
            Logger.error("Error checking WebP format: ${e.message}", e)
            false
        }
    }

    private fun convertWebPToPng(webpFile: File): File {
        val pngFileName = webpFile.name.replace(".webp", ".png")
        val pngFile = File(webpFile.parentFile, pngFileName)

        val process = ProcessBuilder("dwebp", webpFile.absolutePath, "-o", pngFile.absolutePath)
            .redirectErrorStream(true)
            .start()

        val exitCode = process.waitFor()
        val errorMsg = process.inputStream.readAllBytes().toString(StandardCharsets.UTF_8)
        if (exitCode != 0) {
            Logger.error("Failed to convert WebP to PNG: $errorMsg")
            throw RuntimeException("Failed to convert WebP image to PNG. Exit code: $exitCode")
        }

        Logger.info("Converted WebP to PNG: ${pngFile.absolutePath}")
        return pngFile
    }

    private fun applyRoundedCorners(image: NativeImage, radius: Int, topLeft: Boolean = true, topRight: Boolean = true, bottomLeft: Boolean = true, bottomRight: Boolean = true) {
        val width = image.width
        val height = image.height

        // Ensure radius does not exceed half of the smallest dimension
        var effectiveRadius = minOf(radius, width / 2, height / 2)

        if (image.width < 250) {
            effectiveRadius /= 3
        }

        for (x in 0 until width) {
            for (y in 0 until height) {
                var alpha = image.getOpacity(x, y).toInt() // Get current alpha value

                // Check if the pixel is in one of the four corner regions
                val isTopLeftCorner = x < effectiveRadius && y < effectiveRadius
                val isTopRightCorner = x > width - effectiveRadius && y < effectiveRadius
                val isBottomLeftCorner = x < effectiveRadius && y > height - effectiveRadius
                val isBottomRightCorner = x > width - effectiveRadius && y > height - effectiveRadius

                if (isTopLeftCorner && topLeft) {
                    val dx = effectiveRadius - x
                    val dy = effectiveRadius - y
                    // If distance squared from corner center to pixel is greater than radius squared, it's outside the circle
                    if (dx * dx + dy * dy > effectiveRadius * effectiveRadius) {
                        alpha = 0
                    }
                } else if (isTopRightCorner && topRight) {
                    val dx = x - (width - effectiveRadius)
                    val dy = effectiveRadius - y
                    if (dx * dx + dy * dy > effectiveRadius * effectiveRadius) {
                        alpha = 0
                    }
                } else if (isBottomLeftCorner && bottomLeft) {
                    val dx = effectiveRadius - x
                    val dy = y - (height - effectiveRadius)
                    if (dx * dx + dy * dy > effectiveRadius * effectiveRadius) {
                        alpha = 0
                    }
                } else if (isBottomRightCorner && bottomRight) {
                    val dx = x - (width - effectiveRadius)
                    val dy = y - (height - effectiveRadius)
                    if (dx * dx + dy * dy > effectiveRadius * effectiveRadius) {
                        alpha = 0
                    }
                }

                // If alpha was set to 0, update the pixel color with the new alpha
                if (alpha == 0) {
                    // Keep the RGB values, but set the alpha component to 0
                    image.setColor(x, y, image.getColorArgb(x, y) and 0x00FFFFFF)
                }
            }
        }
    }
}