package dev.timlohrer.spotify_overlay.utils
//? if <= 1.21.5 {
/*import com.mojang.blaze3d.vertex.VertexConsumer
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.renderer.RenderType
import org.joml.Matrix4f

fun GuiGraphics.fillDouble(
    layer: RenderType,
    x1: Double,
    x2: Double,
    y1: Double,
    y2: Double,
    z: Double,
    color: Int
) {
    var x1Copy = x1
    var x2Copy = x2
    var y1Copy = y1
    var y2Copy = y2
    val matrix4f: Matrix4f = this.pose().last().pose()
    var i: Double
    if (x1Copy < y1Copy) {
        i = x1Copy
        x1Copy = y1Copy
        y1Copy = i
    }
    if (x2Copy < y2Copy) {
        i = x2Copy
        x2Copy = y2Copy
        y2Copy = i
    }
    
    val vertexConsumer: VertexConsumer = this.bufferSource.getBuffer(layer)
    vertexConsumer.addVertex(matrix4f, x1Copy.toFloat(), x2Copy.toFloat(), z.toFloat()).setColor(color)
    vertexConsumer.addVertex(matrix4f, x1Copy.toFloat(), y2Copy.toFloat(), z.toFloat()).setColor(color)
    vertexConsumer.addVertex(matrix4f, y1Copy.toFloat(), y2Copy.toFloat(), z.toFloat()).setColor(color)
    vertexConsumer.addVertex(matrix4f, y1Copy.toFloat(), x2Copy.toFloat(), z.toFloat()).setColor(color)
}
*///?}