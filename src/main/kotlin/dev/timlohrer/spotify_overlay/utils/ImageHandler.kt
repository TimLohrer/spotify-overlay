package dev.timlohrer.spotify_overlay.utils

//? if <= 1.21.5 {
/*import net.minecraft.client.renderer.RenderType
*///?}
//? if >= 1.21.5 {
import net.minecraft.client.renderer.RenderPipelines
//?}
import com.mojang.blaze3d.platform.NativeImage
import dev.timlohrer.spotify_overlay.SpotifyOverlay
import dev.timlohrer.spotify_overlay.SpotifyOverlay.toId
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.renderer.texture.DynamicTexture
import org.endlesssource.mediainterface.api.ArtworkDecoder
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.util.*
import javax.imageio.ImageIO


internal object ImageHandler {
    private val MC = Minecraft.getInstance()
    val EMPTY = "textures/spotify/empty.png".toId() // this doesn't even exist, but it is used as a placeholder

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

    fun loadImage(cornerRadius: Int, topLeft: Boolean, topRight: Boolean, bottomLeft: Boolean, bottomRight: Boolean): IdentifierAlias {
        val source = SpotifyOverlay.currentMedia?.imageUrl ?: return EMPTY

        val bytes = ArtworkDecoder.decodeBytes(source)
        if (bytes.isEmpty) return EMPTY
        
        val bis = ByteArrayInputStream(bytes.orElseThrow())
        val bufferedImage = ImageIO.read(bis) ?: return EMPTY
        var nativeImage = convertToNativeImage(bufferedImage)

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
        Logger.info("Registering texture: $textureLocation")

        MC.textureManager.register(textureLocation, dynamicTexture)
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