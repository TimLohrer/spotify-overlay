package dev.timlohrer.spotify_overlay.components

import dev.timlohrer.spotify_overlay.SpotifyOverlay
import dev.timlohrer.spotify_overlay.config.HUD_TYPE
import dev.timlohrer.spotify_overlay.utils.ImageHandler
import dev.timlohrer.spotify_overlay.utils.fillDouble
import dev.timlohrer.spotify_overlay.utils.MarqueeManager
import io.wispforest.owo.ui.container.FlowLayout
import io.wispforest.owo.ui.core.OwoUIDrawContext
import io.wispforest.owo.ui.core.Sizing
import net.minecraft.client.MinecraftClient
import net.minecraft.client.render.RenderLayer
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import net.silkmc.silk.core.text.literalText
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
            
            val TEXT_RENDERER = MinecraftClient.getInstance().textRenderer
            val scale = SpotifyOverlay.getConfig().scale + 1.0f // yes, I am readjusting this here since ive built the whole ui in a different scale level :3 Is this clean? Nope! Do I care? Also nope!
            val hudType = SpotifyOverlay.getConfig().hudType
            
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
            
            if (SpotifyOverlay.getConfig().showBackground) {
                context.fill(
                    x.toInt(),
                    y.toInt(),
                    (x + width).toInt(),
                    (y + height).toInt(),
                    Color(0, 0, 0, 150).rgb //Semi-transparent black background
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
                    val radius = SpotifyOverlay.getConfig().cornerRadius
                    ImageHandler.downloadImage(SpotifyOverlay.currentMedia?.imageUrl!!,  if (radius > 1) {
                        radius * 7.5 - if (hudType == HUD_TYPE.BIG_COVER) {
                            radius * 4.5
                        } else { 0 }.toDouble()
                    } else {
                        0
                    }.toInt(),
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
                    literalText("Error: ${SpotifyOverlay.currentMedia?.error ?: "Unknown error"}"),
                    x.toFloat(),
                    y.toFloat(),
                    0.75f,
                    Color.RED.rgb
                )
            }
            
            val songName = SpotifyOverlay.currentMedia?.title ?: Text.translatable("spotify-overlay.no_song_playing").string
            val artistName = SpotifyOverlay.currentMedia?.artist ?: ""

            val displayTitle = if (SpotifyOverlay.getConfig().enableMarquee) {
                MarqueeManager.getMarqueeText("title", songName, maxTitleLength)
            } else {
                if (songName.length > maxTitleLength) {
                    songName.take(maxTitleLength - 3) + "..."
                } else {
                    songName
                }
            }

            context.drawText(
                literalText(displayTitle),
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
                val displayArtist = if (SpotifyOverlay.getConfig().enableMarquee) {
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
                    literalText(displayArtist),
                    artistX,
                    artistY,
                    artistScale,
                    Color.WHITE.rgb
                )
            }
            
            
            if (!hasTimeline) return
            
            // Draw timeline background
            context.fillDouble(
                RenderLayer.getGui(),
                timelineX,
                timelineY,
                timelineX2,
                timelineY2,
                0.0,
                Color.GRAY.rgb // White color
            )
            
            // Draw timeline progress
            val progress = {
                val sec = SpotifyOverlay.currentMedia?.position
                val totalTime = SpotifyOverlay.currentMedia?.duration
                if (sec != null && totalTime != null) {
                    (sec / totalTime.toDouble())
                } else {
                    0.0
                }
            }
            context.fillDouble(
                RenderLayer.getGui(),
                timelineX,
                timelineY,
                timelineX + (timelineX2 - timelineX) * progress(),
                timelineY2,
                0.0,
                Color(SpotifyOverlay.getConfig().color).rgb
            )

            // Draw small progress thumb
            context.fillDouble(
                RenderLayer.getGui(),
                timelineX + ((timelineX2 - timelineX) * progress()) - (thumbSize / 2) + (timelineThickness / 2),
                timelineY - (thumbSize / 2),
                timelineX + ((timelineX2 - timelineX) * progress()) + (thumbSize / 2) - (timelineThickness / 2),
                timelineY2 + (thumbSize / 2),
                0.0,
                Color(SpotifyOverlay.getConfig().color).rgb
            )
  
            val totalTime = SpotifyOverlay.currentMedia?.duration ?: 0
            val position = SpotifyOverlay.currentMedia?.position?.toInt() ?: 0
            val totalTimeText = if (totalTime > 0) {
                String.format("%02d:%02d", totalTime / 60, totalTime % 60)
            } else {
                "00:00"
            }
            val current = if (position > 0) {
                String.format("%02d:%02d", position / 60, position % 60)
            } else {
                "00:00"
            }
            
            // Draw current time
            context.drawText(
                literalText(current),
                timelineX.toFloat(),
                (timelineY + boxPadding).toFloat(),
                0.5f * scale,
                Color.WHITE.rgb
            )

            // Draw total time
            context.drawText(
                literalText(totalTimeText),
                (timelineX2 - (TEXT_RENDERER.getWidth(totalTimeText) * scale) / 2).toFloat(), // Dikka das macht so null Sinn Junge es kracht komplett
                (timelineY + boxPadding).toFloat(),
                0.5f * scale,
                Color.WHITE.rgb
            )
        }
    }