package dev.timlohrer.spotify_overlay.utils

object MarqueeManager {
    private val marqueeStates = mutableMapOf<String, MarqueeState>()

    data class MarqueeState(
        var fullText: String,
        val maxLength: Int,
        var position: Int = 0,
        var tick: Int = 0,
        var lastUpdateTime: Long = System.currentTimeMillis()
    )

    private const val TICKS_PER_MOVE = 8
    private const val PAUSE_AT_START = 30

    fun getMarqueeText(id: String, text: String, maxLength: Int): String {
        if (text.length <= maxLength) {
            marqueeStates.remove(id)
            return text
        }

        val currentTime = System.currentTimeMillis()
        val state = marqueeStates.getOrPut(id) {
            MarqueeState(
                fullText = "$text    ",
                maxLength = maxLength,
                lastUpdateTime = currentTime
            )
        }

        if (state.fullText.trimEnd() != text) {
            state.fullText = "$text    "
            state.position = 0
            state.tick = 0
            state.lastUpdateTime = currentTime
        }

        if (currentTime - state.lastUpdateTime > 50) {
            state.lastUpdateTime = currentTime
            state.tick++

            if (state.tick >= PAUSE_AT_START) {
                if ((state.tick - PAUSE_AT_START) % TICKS_PER_MOVE == 0) {
                    state.position++

                    if (state.position >= state.fullText.length) {
                        state.position = 0
                        state.tick = 0
                    }
                }
            }
        }

        val endPos = minOf(state.position + maxLength, state.fullText.length)
        var visibleText = state.fullText.substring(state.position, endPos)

        if (visibleText.length < maxLength) {
            val needed = maxLength - visibleText.length
            visibleText += state.fullText.substring(0, minOf(needed, state.fullText.length))
        }

        return visibleText.take(maxLength)
    }

    fun clearState(id: String) {
        marqueeStates.remove(id)
    }

    fun clearAllStates() {
        marqueeStates.clear()
    }
}
