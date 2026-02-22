package dev.timlohrer.spotify_overlay.components

import dev.timlohrer.spotify_overlay.SpotifyOverlay
import dev.timlohrer.spotify_overlay.config.HUD_TYPE
import dev.timlohrer.spotify_overlay.utils.ImageHandler
import dev.timlohrer.spotify_overlay.utils.Logger
//? if <= 1.21.5 {
/*import dev.timlohrer.spotify_overlay.utils.fillDouble
import net.minecraft.client.renderer.RenderType
*///?}
import dev.timlohrer.spotify_overlay.utils.MarqueeManager
import io.wispforest.owo.ui.container.FlowLayout
//? if < 1.21.11 {
/*import io.wispforest.owo.ui.core.OwoUIDrawContext
*///?} else if >= 1.21.11 {
import io.wispforest.owo.ui.core.OwoUIGraphics as OwoUIDrawContext
//?}
import io.wispforest.owo.ui.core.Sizing
import net.minecraft.client.Minecraft
import net.minecraft.network.chat.Component
import java.awt.Color

class SpotifyOverlayComponent(
    horizontalSizing: Sizing = Sizing.content(),
    verticalSizing: Sizing = Sizing.content(),
    ) : FlowLayout(horizontalSizing, verticalSizing, Algorithm.HORIZONTAL) {
        override fun draw(
            context: OwoUIDrawContext,
            mouseX: Int,
            mouseY: Int,
            partialTicks: Float,
            delta: Float
        ) {
            super.draw(context, mouseX, mouseY, partialTicks, delta)
            
            val TEXT_RENDERER = Minecraft.getInstance().font
            val settings = SpotifyOverlay.getActiveSettings()
            val scale = settings.scale + 0.5f // yes, I am readjusting this here since ive built the whole ui in a different scale level :3 Is this clean? Nope! Do I care? Also nope!
            val hudType = settings.hudType

            val height = when (hudType) {
                HUD_TYPE.DEFAULT -> 35
                HUD_TYPE.MEDIUM_COVER -> 35
                HUD_TYPE.BIG_COVER -> 105
            } * scale
            val width = when (hudType) {
                HUD_TYPE.DEFAULT -> 125
                HUD_TYPE.MEDIUM_COVER -> 125
                HUD_TYPE.BIG_COVER -> 85
            } * scale
            
            val x = this.x.toDouble()
            val y = this.y.toDouble()
            
            if (settings.showBackground) {
                context.fill(
                    x.toInt(),
                    y.toInt(),
                    (x + width).toInt(),
                    (y + height).toInt(),
                    Color(0, 0, 0, 100).rgb //Semi-transparent black background
                )
            }
            
            val hasCover = SpotifyOverlay.currentMedia?.imageUrl != null
            val hasTimeline = hudType != HUD_TYPE.BIG_COVER && SpotifyOverlay.currentMedia?.duration != null && SpotifyOverlay.currentMedia?.position != null

            val boxPadding = 3.0 * scale
            val coverX = when (hudType) {
                HUD_TYPE.DEFAULT -> x + boxPadding
                HUD_TYPE.MEDIUM_COVER -> x
                HUD_TYPE.BIG_COVER -> x
            }.toInt()
            val coverY = when (hudType) {
                HUD_TYPE.DEFAULT -> y + boxPadding
                HUD_TYPE.MEDIUM_COVER -> y
                HUD_TYPE.BIG_COVER -> y
            }.toInt()
            val coverSize = when (hudType) {
                HUD_TYPE.DEFAULT -> height - boxPadding * 2
                HUD_TYPE.MEDIUM_COVER -> height
                HUD_TYPE.BIG_COVER -> width
            }.toInt()
            
            val titleX = when (hudType) {
                HUD_TYPE.DEFAULT -> coverX + if (hasCover) {
                    coverSize + boxPadding
                } else {
                    0
                }.toInt()
                HUD_TYPE.MEDIUM_COVER -> coverX + if (hasCover) {
                    coverSize + boxPadding
                } else {
                    0
                }.toInt()
                HUD_TYPE.BIG_COVER -> coverX + boxPadding
            }.toFloat()
            val titleY = when (hudType) {
                HUD_TYPE.DEFAULT -> coverY + 2 * scale // 2 pixels offset for better alignment with letters like "Ü"
                HUD_TYPE.MEDIUM_COVER -> coverY + 3 * scale
                HUD_TYPE.BIG_COVER -> coverY + coverSize + 3 * scale
            }
            val titleScale = when (hudType) {
                HUD_TYPE.DEFAULT -> 0.85f
                HUD_TYPE.MEDIUM_COVER -> 0.75f
                HUD_TYPE.BIG_COVER -> 0.9f
            } * scale
            val maxTitleLength = when (hudType) {
                HUD_TYPE.DEFAULT -> 18
                HUD_TYPE.MEDIUM_COVER -> 20
                HUD_TYPE.BIG_COVER -> 16
            }
            
            val artistX = when (hudType) {
                HUD_TYPE.DEFAULT -> titleX
                HUD_TYPE.MEDIUM_COVER -> titleX
                HUD_TYPE.BIG_COVER -> titleX
            }
            val artistY = when (hudType) {
                HUD_TYPE.DEFAULT -> titleY + 7 * scale + 2 // 10 pixels offset for better alignment with letters like "Ü"
                HUD_TYPE.MEDIUM_COVER -> titleY + 7 * scale + 2
                HUD_TYPE.BIG_COVER -> titleY + 7 * scale + 2
            }
            val artistScale = when (hudType) {
                HUD_TYPE.DEFAULT -> 0.55f
                HUD_TYPE.MEDIUM_COVER -> 0.55f
                HUD_TYPE.BIG_COVER -> 0.6f
            } * scale
            val maxArtistLength = when (hudType) {
                HUD_TYPE.DEFAULT -> 25
                HUD_TYPE.MEDIUM_COVER -> 25
                HUD_TYPE.BIG_COVER -> 20
            }
            
            val timelineThickness = when (hudType) {
                HUD_TYPE.DEFAULT -> 1.0
                HUD_TYPE.MEDIUM_COVER -> 1.0
                HUD_TYPE.BIG_COVER -> 0.0 // No timeline for big cover
            } * scale
            val timelineX = when (hudType) {
                HUD_TYPE.DEFAULT -> titleX
                HUD_TYPE.MEDIUM_COVER -> titleX
                HUD_TYPE.BIG_COVER -> 0.0 // No timeline for big cover
            }.toDouble()
            val timelineY = when (hudType) {
                HUD_TYPE.DEFAULT -> y + height - 11 * scale + timelineThickness
                HUD_TYPE.MEDIUM_COVER -> y + height - 11 * scale + timelineThickness
                HUD_TYPE.BIG_COVER -> 0.0 // No timeline for big cover
            }
            val timelineX2 = when (hudType) {
                HUD_TYPE.DEFAULT -> x + width - boxPadding
                HUD_TYPE.MEDIUM_COVER -> x + width - boxPadding
                HUD_TYPE.BIG_COVER -> 0.0 // No timeline for big cover
            }
            val timelineY2 = when (hudType) {
                HUD_TYPE.DEFAULT -> y + height - 11 * scale
                HUD_TYPE.MEDIUM_COVER -> y + height - 11 * scale
                HUD_TYPE.BIG_COVER -> 0.0 // No timeline for big cover
            }
            val thumbSize = when (hudType) {
                HUD_TYPE.DEFAULT -> 4.0 * scale
                HUD_TYPE.MEDIUM_COVER -> 4.0 * scale
                HUD_TYPE.BIG_COVER -> 0.0 // No timeline for big cover
            }

            if (hasCover) {
                val img = if (SpotifyOverlay.lastDownloadedImageUrl == SpotifyOverlay.currentMedia?.imageUrl!!) {
                    SpotifyOverlay.lastDownloadedImage
                } else {
                    val radius = settings.cornerRadius
                    ImageHandler.loadImage(if (radius > 1) {
                        radius * 7.5 - if (hudType == HUD_TYPE.BIG_COVER) {
                            radius * 4.5
                        } else { 0 }.toDouble()
                    } else {
                        0
                    }.toInt(),
                    topLeft = true,
                    topRight = hudType != HUD_TYPE.MEDIUM_COVER,
                    bottomLeft = hudType != HUD_TYPE.BIG_COVER,
                    bottomRight = hudType == HUD_TYPE.DEFAULT)
                }
                if (img != ImageHandler.EMPTY) {
                    SpotifyOverlay.lastDownloadedImage = img
                    SpotifyOverlay.lastDownloadedImageUrl = SpotifyOverlay.currentMedia?.imageUrl
                }

                ImageHandler.drawImage(
                    context,
                    img!!,
                    coverX,
                    coverY,
                    coverSize,
                    coverSize
                )
            }
            
            if (SpotifyOverlay.currentMedia?.isError() == true) {
                context.drawText(
                    Component.literal("Error: ${SpotifyOverlay.currentMedia?.error ?: "Unknown error"}"),
                    x.toFloat(),
                    y.toFloat(),
                    0.75f,
                    Color.RED.rgb
                )
            }
            
            val songName = SpotifyOverlay.currentMedia?.title ?: Component.translatable("spotify-overlay.no_song_playing").string
            val artistName = SpotifyOverlay.currentMedia?.artist ?: ""

            val displayTitle = if (settings.enableMarquee) {
                MarqueeManager.getMarqueeText("title", songName, maxTitleLength)
            } else {
                if (songName.length > maxTitleLength) {
                    songName.take(maxTitleLength - 3) + "..."
                } else {
                    songName
                }
            }
            

            context.drawText(
                Component.literal(displayTitle),
                titleX,
                titleY,
                titleScale,
                if (SpotifyOverlay.currentMedia?.isError() == true) {
                    Color.RED.rgb
                } else if (SpotifyOverlay.currentMedia?.isStopped() == true) {
                    Color.YELLOW.rgb
                } else {
                    Color.WHITE.rgb
                }
            )

            if (artistName.isNotEmpty()) {
                val displayArtist = if (settings.enableMarquee) {
                    "by " + MarqueeManager.getMarqueeText("artist", artistName, maxArtistLength - 3) // -3 for "by "
                } else {
                    val maxLength = maxArtistLength - 3 // -3 for "by "
                    if (artistName.length > maxLength) {
                        "by " + artistName.take(maxLength - 3) + "..."
                    } else {
                        "by " + artistName
                    }
                }

                context.drawText(
                    Component.literal(displayArtist),
                    artistX,
                    artistY,
                    artistScale,
                    Color.WHITE.rgb
                )
            }
            
            
            if (!hasTimeline) return
            
            // Draw timeline background
            //? if <= 1.21.5 {
            /*context.fillDouble(
                RenderType.gui(),
                timelineX,
                timelineY,
                timelineX2,
                timelineY2,
                0.0,
                Color.GRAY.rgb
            )
            *///?} else if >= 1.21.7 {
            context.fill(
                timelineX.toInt(),
                timelineY.toInt(),
                timelineX2.toInt(),
                timelineY2.toInt(),
                Color.GRAY.rgb
            )
            //?}
            
            val progress = {
                val positionSec = SpotifyOverlay.currentMedia?.position
                val durationSec = SpotifyOverlay.currentMedia?.duration
                if (positionSec != null && durationSec != null && durationSec > 0) {
                    (positionSec / durationSec.toDouble()).coerceIn(0.0, 1.0)
                } else {
                    0.0
                }
            }
            
            //? if <= 1.21.5 {
             /*context.fillDouble(
                 RenderType.gui(),
                timelineX,
                timelineY,
                timelineX + (timelineX2 - timelineX) * progress(),
                timelineY2,
                0.0,
                Color(settings.color).rgb
            )
            *///?} else if >= 1.21.7 {
            context.fill(
                timelineX.toInt(),
                timelineY.toInt(),
                (timelineX + (timelineX2 - timelineX) * progress()).toInt(),
                timelineY2.toInt(),
                Color(settings.color).rgb
            )
            //?}

            val thumbX1 = timelineX + ((timelineX2 - timelineX) * progress()) - (thumbSize / 2) + (timelineThickness / 2)
            val thumbY1 = timelineY - (thumbSize / 2)
            val thumbX2 = timelineX + ((timelineX2 - timelineX) * progress()) + (thumbSize / 2) - (timelineThickness / 2)
            val thumbY2 = timelineY2 + (thumbSize / 2)
            
            //? if <= 1.21.5 {
             /*context.fillDouble(
                RenderType.gui(),
                thumbX1,
                thumbY1,
                thumbX2,
                thumbY2,
                0.0,
                Color(settings.color).rgb
            )
            *///?} else if >= 1.21.7 {
            context.fill(
                thumbX1.toInt(),
                thumbY1.toInt(),
                thumbX2.toInt(),
                thumbY2.toInt(),
                Color(settings.color).rgb
            )
            //?}
  
            val durationSec = SpotifyOverlay.currentMedia?.duration ?: 0
            val positionSec = SpotifyOverlay.currentMedia?.position ?: 0.0

            if (durationSec == 0 && SpotifyOverlay.currentMedia != null) {
                Logger.warn("Duration is 0 or null for media: ${SpotifyOverlay.currentMedia?.title}")
            }

            val positionSecInt = positionSec.toInt()

            val totalTimeText = if (durationSec > 0) {
                String.format("%d:%02d", durationSec / 60, durationSec % 60)
            } else {
                "00:00"
            }

            val currentTimeText = if (positionSecInt > 0) {
                String.format("%d:%02d", positionSecInt / 60, positionSecInt % 60)
            } else {
                "00:00"
            }
            
            context.drawText(
                Component.literal(currentTimeText),
                timelineX.toFloat(),
                (timelineY + boxPadding).toFloat(),
                0.5f * scale,
                Color.WHITE.rgb
            )

            context.drawText(
                Component.literal(totalTimeText),
                (timelineX2 - (TEXT_RENDERER.width(totalTimeText) * 0.5f * scale)).toFloat(),
                (timelineY + boxPadding).toFloat(),
                0.5f * scale,
                Color.WHITE.rgb
            )
        }
    }