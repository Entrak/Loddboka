package com.bearkingsoftware.loddboka.ui.hovedskjerm

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection

class SpeechBubbleShape(
    private val cornerRadius: Dp,
    private val tipSize: Dp
) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val cornerRadiusPx = with(density) { cornerRadius.toPx() }
        val tipSizePx = with(density) { tipSize.toPx() }
        
        val contentWidth = size.width - tipSizePx
        
        val path = Path().apply {
            // Start from top-left after corner
            moveTo(cornerRadiusPx, 0f)

            // Top edge
            lineTo(contentWidth - cornerRadiusPx, 0f)

            // Top-right corner
            arcTo(
                rect = Rect(contentWidth - 2 * cornerRadiusPx, 0f, contentWidth, 2 * cornerRadiusPx),
                startAngleDegrees = -90f,
                sweepAngleDegrees = 90f,
                forceMoveTo = false
            )

            // Right edge to arrow
            val arrowTopY = size.height / 2f - tipSizePx / 2f
            lineTo(contentWidth, arrowTopY)

            // Arrow
            lineTo(size.width, size.height / 2f)
            lineTo(contentWidth, size.height / 2f + tipSizePx / 2f)

            // Right edge after arrow
            lineTo(contentWidth, size.height - cornerRadiusPx)
            
            // Bottom-right corner
            arcTo(
                rect = Rect(contentWidth - 2 * cornerRadiusPx, size.height - 2 * cornerRadiusPx, contentWidth, size.height),
                startAngleDegrees = 0f,
                sweepAngleDegrees = 90f,
                forceMoveTo = false
            )

            // Bottom edge
            lineTo(cornerRadiusPx, size.height)

            // Bottom-left corner
            arcTo(
                rect = Rect(0f, size.height - 2 * cornerRadiusPx, 2 * cornerRadiusPx, size.height),
                startAngleDegrees = 90f,
                sweepAngleDegrees = 90f,
                forceMoveTo = false
            )

            // Left edge
            lineTo(0f, cornerRadiusPx)

            // Top-left corner
            arcTo(
                rect = Rect(0f, 0f, 2 * cornerRadiusPx, 2 * cornerRadiusPx),
                startAngleDegrees = 180f,
                sweepAngleDegrees = 90f,
                forceMoveTo = false
            )
            
            close()
        }
        return Outline.Generic(path)
    }
}