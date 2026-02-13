package dev.timlohrer.spotify_overlay.utils

import com.mojang.blaze3d.systems.RenderSystem
import dev.timlohrer.spotify_overlay.SpotifyOverlay.toId
import dev.timlohrer.spotify_overlay.SpotifyOverlay
import com.mojang.blaze3d.platform.NativeImage
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
//? if <= 1.21.5 {
/*import net.minecraft.client.renderer.RenderType
*///?}
//? if >= 1.21.5 {
import net.minecraft.client.renderer.RenderPipelines
//?}
import net.minecraft.client.renderer.texture.DynamicTexture
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
    private val MC = Minecraft.getInstance()
    private val CACHE = HashMap<String, IdentifierAlias>()
    private val CACHE_DIR = File(MC.gameDirectory, "spotify-overlay/cache")
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
        context: GuiGraphics,
        musicImage: IdentifierAlias,
        x: Int,
        y: Int,
        height: Int,
        width: Int
    ) {
        if (musicImage == EMPTY) return
        //? if >= 1.21.4 {
        context.blit(
            //? if <= 1.21.5 {
            /*{ id -> RenderType.guiTextured(id) },
           *///?} elif > 1.21.5 {
            RenderPipelines.GUI_TEXTURED,
            //?}
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
        //?} elif >= 1.21 {
        /*context.blit(
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
        *///?}
    }

    fun getFileNameFromUrl(url: String): String {
        return UUID.nameUUIDFromBytes(url.toByteArray()).toString() + ".png"
    }

    fun downloadImage(url: String, cornerRadius: Int, topLeft: Boolean = true, topRight: Boolean = true, bottomLeft: Boolean = true, bottomRight: Boolean = true): IdentifierAlias {
        try {
            val source = SpotifyOverlay.currentMedia?.source
            if (source != null && source.contains("Apple Music", ignoreCase = true)) {
                Logger.info("Platform is Apple Music - skipping image render.")
                return EMPTY
            }

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
            } else if (url.startsWith("data:image/png;base64,") || url.startsWith("data:image/jpeg;base64,")) {
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
                val base64Data = when {
                    url.startsWith("data:image/png;base64,") -> url.removePrefix("data:image/png;base64,")
                    url.startsWith("data:image/jpeg;base64,") -> url.removePrefix("data:image/jpeg;base64,")
                    else -> return EMPTY
                }
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

    private fun loadFromDisk(file: File, url: String, cornerRadius: Int, topLeft: Boolean, topRight: Boolean, bottomLeft: Boolean, bottomRight: Boolean): IdentifierAlias {
        Logger.info("Loading cached image: ${file.absolutePath}")
        val bufferedImage = ImageIO.read(file) ?: return EMPTY
        val nativeImage = convertToNativeImage(bufferedImage)

        if (cornerRadius > 0) {
            applyRoundedCorners(nativeImage, cornerRadius, topLeft, topRight, bottomLeft, bottomRight)
        }

        val id = "spotify_cover_${UUID.randomUUID()}"
        val textureLocation = id.toId()

        //? if >= 1.21.5 {
        val dynamicTexture = DynamicTexture({ textureLocation.toString() }, nativeImage)
        //?} elif >= 1.21 {
        /*val dynamicTexture = DynamicTexture(nativeImage)
        *///?}
        Logger.info("Registering texture: $textureLocation for URL: ${url.split("base64,").first()}")

        MC.textureManager.register(textureLocation, dynamicTexture)
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

                val abgr = (a shl 24) or (b shl 16) or (g shl 8) or r
                //? if >= 1.21.4 {
                nativeImage.setPixelABGR(x, y, abgr)
                //?} elif >= 1.21 {
                /*nativeImage.setPixelRGBA(x, y, abgr)
                *///?}
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

        var effectiveRadius = minOf(radius, width / 2, height / 2)

        if (image.width < 250) {
            effectiveRadius /= 3
        }

        for (x in 0 until width) {
            for (y in 0 until height) {
                var alpha = image.getLuminanceOrAlpha(x, y).toInt()

                val isTopLeftCorner = x < effectiveRadius && y < effectiveRadius
                val isTopRightCorner = x > width - effectiveRadius && y < effectiveRadius
                val isBottomLeftCorner = x < effectiveRadius && y > height - effectiveRadius
                val isBottomRightCorner = x > width - effectiveRadius && y > height - effectiveRadius

                if (isTopLeftCorner && topLeft) {
                    val dx = effectiveRadius - x
                    val dy = effectiveRadius - y
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

                if (alpha == 0) {

                    //? if >= 1.21.4 {
                    image.setPixelABGR(x, y, image.getPixel(x, y) and 0x00FFFFFF)
                    //?} elif >= 1.21 {
                    /*image.setPixelRGBA(x, y, image.getPixelRGBA(x, y) and 0x00FFFFFF)
                    *///?}
                }
            }
        }
    }
}