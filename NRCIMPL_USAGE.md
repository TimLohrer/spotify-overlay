# NrcImplSupport - Native Rendering Control Implementation Support

## Übersicht

Die `NrcImplSupport`-Klasse bietet eine saubere API für externe Mods/Clients, um:
1. Das Standard-Config-System komplett zu deaktivieren
2. Alle Konfigurationswerte programmatisch zu steuern
3. Die Component mit eigener Logik und Positionierung zu rendern
4. Direkt auf Media-Informationen zuzugreifen

## Grundlegende Verwendung

### Schritt 1: Instanz erstellen

```kotlin
import dev.timlohrer.spotify_overlay.impl.NrcImplSupport
import dev.timlohrer.spotify_overlay.config.HUD_TYPE
import dev.timlohrer.spotify_overlay.config.OverlaySettings

val nrc = NrcImplSupport()
```

### Schritt 2: Standard-Config deaktivieren

```kotlin
// Deaktiviert das AutoConfig-System komplett
nrc.disableDefaultConfig()

// Um es wieder zu aktivieren:
// nrc.enableDefaultConfig()
```

### Schritt 3: Eigene Konfiguration setzen

#### Option A: Einzelne Werte setzen

```kotlin
nrc.setRenderEnabled(true)
nrc.setScale(1.5f)
nrc.setHudType(HUD_TYPE.BIG_COVER)
nrc.setColor(0xFF0000) // Rot
nrc.setBackgroundVisible(true)
nrc.setCornerRadius(8.0f)
nrc.setSourceFilter("Spotify,Apple Music")
nrc.setMarqueeEnabled(true)
nrc.setComponentPosition(100, 50)
```

#### Option B: Alle Werte auf einmal setzen

```kotlin
nrc.applySettings(OverlaySettings(
    renderOverlay = true,
    scale = 1.5f,
    hudType = HUD_TYPE.BIG_COVER,
    cornerRadius = 8.0f,
    showBackground = true,
    color = 0xFF0000,
    sourceFilter = "Spotify",
    enableMarquee = true,
    positionX = 100,
    positionY = 50
))
```

## Konfigurationswerte abrufen

```kotlin
// Aktuelle Settings abrufen
val settings = nrc.getActiveSettings()

// Oder einzelne Werte:
val scale = nrc.getScale()
val hudType = nrc.getHudType()
val color = nrc.getColor()
val isEnabled = nrc.isRenderEnabled()
```

## Media-Informationen abrufen

```kotlin
// Vollständige MediaInfo
val media = nrc.getCurrentMedia()

// Oder einzelne Eigenschaften:
val title = nrc.getCurrentTitle()
val artist = nrc.getCurrentArtist()
val imageUrl = nrc.getCurrentImageUrl()
val position = nrc.getCurrentPosition() // in Sekunden
val duration = nrc.getDuration() // in Sekunden
val isPlaying = nrc.isPlaying()
val source = nrc.getSource() // z.B. "Spotify"
```

## Eigenes Component-Rendering

### Custom Renderer registrieren

```kotlin
nrc.registerCustomRenderer { context, mouseX, mouseY, partialTicks, delta ->
    // Deine eigene Rendering-Logik hier
    val media = nrc.getCurrentMedia()
    
    if (media != null && media.isPlaying) {
        // Beispiel: Zeichne einen einfachen Text
        // context ist OwoUIDrawContext/OwoUIGraphics
        // Du kannst alle Owo UI Funktionen nutzen
        
        val title = media.title ?: "Unknown"
        val artist = media.artist ?: "Unknown"
        
        // Zeichne Text, Boxen, Bilder etc.
        // ... deine Custom-Rendering-Logik ...
    }
}

// Um zum Standard-Rendering zurückzukehren:
// nrc.unregisterCustomRenderer()
```

### Eigene OWO UI Component hinzufügen

```kotlin
import io.wispforest.owo.ui.container.FlowLayout
import io.wispforest.owo.ui.core.Positioning
import io.wispforest.owo.ui.core.Sizing

// Erstelle deine eigene Component
val myComponent = FlowLayout(Sizing.content(), Sizing.content(), FlowLayout.Algorithm.VERTICAL).apply {
    positioning(Positioning.absolute(200, 100))
    // Füge deine UI-Elemente hinzu
}

// Füge sie zum HUD hinzu
nrc.addCustomComponent(myComponent)

// Um sie zu entfernen:
// nrc.removeCustomComponent()
```

## Listener-Kontrolle

```kotlin
// Listener manuell initialisieren
nrc.initializeListener()

// Listener stoppen
nrc.uninitializeListener()

// Prüfen ob Listener läuft
val isRunning = nrc.isListenerRunning()
```

## Cache-Verwaltung

```kotlin
// Overlay aktualisieren (nützlich nach Änderungen)
nrc.refreshOverlay()

// Bild-Cache leeren
nrc.clearCache()
```

## Vollständiges Beispiel

```kotlin
import dev.timlohrer.spotify_overlay.impl.NrcImplSupport
import dev.timlohrer.spotify_overlay.config.HUD_TYPE
import dev.timlohrer.spotify_overlay.config.OverlaySettings
import net.minecraft.network.chat.Component
import java.awt.Color

class MyCustomSpotifyIntegration {
    private val nrc = NrcImplSupport()
    
    fun initialize() {
        // 1. Deaktiviere Standard-Config
        nrc.disableDefaultConfig()
        
        // 2. Setze eigene Einstellungen
        nrc.applySettings(OverlaySettings(
            renderOverlay = true,
            scale = 2.0f,
            hudType = HUD_TYPE.BIG_COVER,
            cornerRadius = 10.0f,
            showBackground = true,
            color = 0x00FF00, // Grün
            sourceFilter = "Spotify",
            enableMarquee = true,
            positionX = 50,
            positionY = 50
        ))
        
        // 3. Optional: Eigenen Renderer registrieren
        registerCustomRenderer()
        
        // 4. Listener starten
        nrc.initializeListener()
    }
    
    private fun registerCustomRenderer() {
        nrc.registerCustomRenderer { context, mouseX, mouseY, partialTicks, delta ->
            val media = nrc.getCurrentMedia()
            
            if (media != null && media.isPlaying) {
                // Zeichne eigenes UI
                val title = media.title ?: "Unknown"
                val artist = media.artist ?: "Unknown"
                
                // Beispiel: Zeichne einen Text an einer festen Position
                // context.drawText(
                //     Component.literal("♫ $title - $artist"),
                //     50f, 50f, 1.0f, Color.WHITE.rgb
                // )
            }
        }
    }
    
    fun updatePosition(x: Int, y: Int) {
        nrc.setComponentPosition(x, y)
    }
    
    fun updateColor(color: Int) {
        nrc.setColor(color)
    }
    
    fun getCurrentSongInfo(): String {
        val title = nrc.getCurrentTitle() ?: "Kein Lied"
        val artist = nrc.getCurrentArtist() ?: "Unbekannt"
        return "$title - $artist"
    }
    
    fun cleanup() {
        nrc.uninitializeListener()
        nrc.enableDefaultConfig() // Optional: Config wieder aktivieren
    }
}
```

## Verfügbare HUD-Typen

```kotlin
HUD_TYPE.DEFAULT        // Standard-Layout mit kleinem Cover
HUD_TYPE.MEDIUM_COVER   // Medium Cover
HUD_TYPE.BIG_COVER      // Großes Cover (ohne Timeline)
```

## Wichtige Hinweise

1. **Config-Deaktivierung**: Nach `disableDefaultConfig()` werden alle Werte aus `OverlaySettings` verwendet, nicht mehr aus der AutoConfig-Datei.

2. **Thread-Safety**: Die Config-Werte werden in einer Coroutine überwacht. Änderungen werden automatisch erkannt.

3. **Position**: Die Position wird in absoluten Pixeln angegeben (X, Y von der oberen linken Ecke).

4. **Farbe**: Farben werden als RGB-Hex-Werte angegeben (z.B. `0xFF0000` für Rot, `0x00FF00` für Grün, `0x0000FF` für Blau).

5. **Custom Renderer**: Wenn ein Custom Renderer registriert ist, kannst du die Standard-Component trotzdem mit `addCustomComponent()` hinzufügen und deine eigene Logik implementieren.

## API-Referenz

### Konfiguration

- `disableDefaultConfig()` - Deaktiviert AutoConfig
- `enableDefaultConfig()` - Aktiviert AutoConfig
- `isDefaultConfigDisabled(): Boolean` - Prüft Config-Status
- `getActiveSettings(): OverlaySettings` - Holt aktive Einstellungen
- `applySettings(settings: OverlaySettings)` - Setzt alle Einstellungen

### Getter

- `isRenderEnabled(): Boolean`
- `getScale(): Float`
- `getHudType(): HUD_TYPE`
- `getCornerRadius(): Float`
- `isBackgroundVisible(): Boolean`
- `getColor(): Int`
- `getSourceFilter(): String`
- `isMarqueeEnabled(): Boolean`
- `getPositionX(): Int`
- `getPositionY(): Int`

### Setter

- `setRenderEnabled(enabled: Boolean)`
- `setScale(scale: Float)`
- `setHudType(type: HUD_TYPE)`
- `setCornerRadius(radius: Float)`
- `setBackgroundVisible(visible: Boolean)`
- `setColor(color: Int)`
- `setSourceFilter(filter: String)`
- `setMarqueeEnabled(enabled: Boolean)`
- `setComponentPosition(x: Int, y: Int)`

### Media-Info

- `getCurrentMedia(): MediaInfo?`
- `getCurrentTitle(): String?`
- `getCurrentArtist(): String?`
- `getCurrentImageUrl(): String?`
- `getCurrentPosition(): Double?`
- `getDuration(): Int?`
- `isPlaying(): Boolean`
- `getSource(): String?`

### Rendering

- `registerCustomRenderer(renderer: CustomRenderer)`
- `unregisterCustomRenderer()`
- `getCustomRenderer(): CustomRenderer?`
- `hasCustomRenderer(): Boolean`
- `addCustomComponent(component: Component)`
- `removeCustomComponent()`

### Listener & Cache

- `initializeListener()`
- `uninitializeListener()`
- `isListenerRunning(): Boolean`
- `refreshOverlay()`
- `clearCache()`

