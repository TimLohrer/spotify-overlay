package dev.timlohrer.spotify_overlay.utils

import org.slf4j.LoggerFactory

object Logger {
    private val logger = LoggerFactory.getLogger("spotify_overlay")
    private const val TAG = "[Spotify Overlay]"

    fun info(message: String) {
        logger.info("$TAG [INFO] $message")
    }
    fun warn(message: String) {
        logger.warn("$TAG [WARN] $message")
    }
    fun error(message: String, throwable: Throwable? = null) {
        if (throwable != null) {
            logger.error("$TAG [ERROR] $message", throwable)
        } else {
            logger.error("$TAG [ERROR] $message")
        }
    }
    fun debug(message: String) {
        logger.debug("$TAG [DEBUG] $message")
    }
    fun trace(message: String) {
        logger.trace("$TAG [TRACE] $message")
    }
}