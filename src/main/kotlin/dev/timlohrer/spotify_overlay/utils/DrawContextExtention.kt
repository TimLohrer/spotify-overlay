package dev.timlohrer.spotify_overlay.utils

import net.minecraft.client.gui.DrawContext
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.render.VertexConsumer
import org.joml.Matrix4f

//? if <= 1.21.5 {
/*fun DrawContext.fillDouble(
    layer: RenderLayer,
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
    val matrix4f: Matrix4f = this.matrices.peek().positionMatrix
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
    
    val vertexConsumer: VertexConsumer = this.vertexConsumers.getBuffer(layer)
    vertexConsumer.vertex(matrix4f, x1Copy.toFloat(), x2Copy.toFloat(), z.toFloat()).color(color)
    vertexConsumer.vertex(matrix4f, x1Copy.toFloat(), y2Copy.toFloat(), z.toFloat()).color(color)
    vertexConsumer.vertex(matrix4f, y1Copy.toFloat(), y2Copy.toFloat(), z.toFloat()).color(color)
    vertexConsumer.vertex(matrix4f, y1Copy.toFloat(), x2Copy.toFloat(), z.toFloat()).color(color)
}
*///?}