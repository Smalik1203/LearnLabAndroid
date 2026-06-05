package com.learnlab.engines.workedproblem

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.learnlab.design.LL
import kotlin.math.cos
import kotlin.math.sin

/**
 * Renders the projectile-launch diagram for the WorkedProblemEngine.
 *
 * Hardcoded for the prototype problem (u = 20 m/s, θ = 30°, g = 10).
 * If we add a second projectile problem with different givens, parameterise
 * this — pass the launch angle + a "scale" so the parabola fits the pane.
 *
 * Annotations toggle on as the worked-problem engine reveals steps:
 *   - always:      launch vector (arrow + "u = 20 m/s, θ = 30°")
 *   - "components":   u_x and u_y component arrows
 *   - "trajectory":   the parabolic path
 *   - "apex":         dot + label at the peak
 *   - "timeOfFlight": "T = 2 s" marker at landing
 *   - "maxHeight":    dotted horizontal line + "H = 5 m"
 *   - "range":        ground bracket + "R ≈ 34.6 m"
 *
 * Coordinate system inside the canvas:
 *   - Ground line ~10% above the bottom of the pane.
 *   - Launch point ~12% from the left edge.
 *   - Parabola is laid out so the apex sits ~40% from the top.
 */
@Composable
fun ProjectileDiagram(
    annotations: Set<String>,
    modifier: Modifier = Modifier,
) {
    val t = LL.tokens
    val muted = t.ink500
    val accent = t.accent500       // brand emerald — used for the launch context
    val accentSoft = t.accent300
    val launchVector = t.accent400 // initial u vector — neutral brand color
    val horizontalColor = t.motionHorizontal // u_x, range, T (horizontal outcomes)
    val verticalColor   = t.motionVertical   // u_y, max height, apex
    val measurer = rememberTextMeasurer()

    // Visual constants
    val angleDeg = 30f
    val angleRad = angleDeg * (kotlin.math.PI.toFloat() / 180f)

    // Animated reveal of the velocity decomposition. When "components"
    // first becomes active, both arrows grow from the launch point.
    // Sequential timing — u_y arrives first (the more pedagogically
    // important component for max-height work), then u_x.
    val componentsActive = "components" in annotations
    val uyProgress by animateFloatAsState(
        targetValue = if (componentsActive) 1f else 0f,
        animationSpec = tween(durationMillis = 500),
        label = "uyProgress",
    )
    val uxProgress by animateFloatAsState(
        targetValue = if (componentsActive) 1f else 0f,
        animationSpec = tween(durationMillis = 500, delayMillis = 300),
        label = "uxProgress",
    )

    Box(modifier = modifier) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val w = size.width
            val h = size.height

            // Geometry: ground line + launch + landing
            val groundY = h * 0.85f
            val launchX = w * 0.12f
            val landingX = w * 0.88f
            val apexX = (launchX + landingX) / 2f
            val apexY = h * 0.30f

            val launch = Offset(launchX, groundY)
            val landing = Offset(landingX, groundY)
            val apex = Offset(apexX, apexY)

            // ── Always: ground line + axes ──
            drawAxes(
                ground = launch,
                groundEnd = Offset(w * 0.95f, groundY),
                verticalEnd = Offset(launchX, h * 0.10f),
                ink = muted,
            )

            // ── trajectory ──
            if ("trajectory" in annotations) {
                drawParabola(launch, apex, landing, color = accentSoft)
            }

            // ── apex ──
            if ("apex" in annotations) {
                drawCircle(color = verticalColor, radius = 5.dp.toPx(), center = apex)
                drawLabel(
                    measurer = measurer,
                    text = "apex",
                    topLeft = Offset(apex.x + 8.dp.toPx(), apex.y - 18.dp.toPx()),
                    color = verticalColor,
                )
            }

            // ── maxHeight (dotted line + "H = 5 m") ──
            if ("maxHeight" in annotations) {
                drawDottedHorizontal(
                    from = Offset(launchX, apexY),
                    to = Offset(apex.x, apexY),
                    color = verticalColor,
                )
                drawLabel(
                    measurer = measurer,
                    text = "H = 5 m",
                    topLeft = Offset(launchX - 4.dp.toPx(), apexY - 20.dp.toPx()),
                    color = verticalColor,
                    bold = true,
                )
            }

            // ── timeOfFlight ──
            if ("timeOfFlight" in annotations) {
                // Small "x" marker at landing + label — horizontal outcome
                val s = 5.dp.toPx()
                drawLine(
                    color = horizontalColor,
                    start = Offset(landing.x - s, landing.y - s),
                    end = Offset(landing.x + s, landing.y + s),
                    strokeWidth = 2.dp.toPx(),
                )
                drawLine(
                    color = horizontalColor,
                    start = Offset(landing.x - s, landing.y + s),
                    end = Offset(landing.x + s, landing.y - s),
                    strokeWidth = 2.dp.toPx(),
                )
                drawLabel(
                    measurer = measurer,
                    text = "T = 2 s",
                    topLeft = Offset(landing.x - 22.dp.toPx(), landing.y + 6.dp.toPx()),
                    color = horizontalColor,
                    bold = true,
                )
            }

            // ── range (ground bracket + "R ≈ 34.6 m") — horizontal outcome ──
            if ("range" in annotations) {
                val bracketY = groundY + 16.dp.toPx()
                val tick = 6.dp.toPx()
                drawLine(
                    color = horizontalColor,
                    start = Offset(launchX, bracketY),
                    end = Offset(landingX, bracketY),
                    strokeWidth = 1.5.dp.toPx(),
                )
                drawLine(
                    color = horizontalColor,
                    start = Offset(launchX, bracketY - tick),
                    end = Offset(launchX, bracketY + tick),
                    strokeWidth = 1.5.dp.toPx(),
                )
                drawLine(
                    color = horizontalColor,
                    start = Offset(landingX, bracketY - tick),
                    end = Offset(landingX, bracketY + tick),
                    strokeWidth = 1.5.dp.toPx(),
                )
                drawLabel(
                    measurer = measurer,
                    text = "R ≈ 34.6 m",
                    topLeft = Offset((launchX + landingX) / 2f - 36.dp.toPx(),
                                     bracketY + 6.dp.toPx()),
                    color = horizontalColor,
                    bold = true,
                )
            }

            // ── Always: launch vector (drawn on top so it's never obscured) ──
            val vecLen = 70.dp.toPx()
            val vecEnd = Offset(
                launchX + vecLen * cos(angleRad),
                groundY - vecLen * sin(angleRad),
            )
            drawArrow(
                start = launch,
                end = vecEnd,
                color = launchVector,
                strokeWidth = 2.5.dp.toPx(),
                headSize = 10.dp.toPx(),
            )
            drawLabel(
                measurer = measurer,
                text = "u = 20 m/s",
                topLeft = Offset(vecEnd.x + 6.dp.toPx(), vecEnd.y - 16.dp.toPx()),
                color = launchVector,
                bold = true,
            )

            // Angle arc + label
            drawAngleArc(
                center = launch,
                radius = 28.dp.toPx(),
                angleDeg = angleDeg,
                color = launchVector,
            )
            drawLabel(
                measurer = measurer,
                text = "30°",
                topLeft = Offset(launchX + 32.dp.toPx(), groundY - 22.dp.toPx()),
                color = launchVector,
            )

            // ── components (drawn last so they sit over the launch vector) ──
            //
            // Animated reveal: each arrow grows from the launch point with
            // its own progress (uy then ux). Labels fade in proportionally
            // so they don't appear before the arrow has reached its tip.
            val compLen = 55.dp.toPx()

            // u_y — vertical (blue) — appears first
            if (uyProgress > 0f) {
                val uyEnd = Offset(launchX, groundY - compLen * uyProgress)
                drawArrow(
                    start = launch, end = uyEnd,
                    color = verticalColor,
                    strokeWidth = 2.dp.toPx(),
                    headSize = 9.dp.toPx() * uyProgress.coerceAtLeast(0.4f),
                )
                if (uyProgress > 0.5f) {
                    drawLabel(
                        measurer = measurer,
                        text = "u_y = 10",
                        topLeft = Offset(
                            launchX - 60.dp.toPx(),
                            groundY - compLen - 8.dp.toPx(),
                        ),
                        color = verticalColor.copy(alpha = (uyProgress - 0.5f) * 2f),
                        bold = true,
                    )
                }
            }

            // u_x — horizontal (lime) — appears second, slightly delayed
            if (uxProgress > 0f) {
                val uxEnd = Offset(launchX + compLen * uxProgress, groundY)
                drawArrow(
                    start = launch, end = uxEnd,
                    color = horizontalColor,
                    strokeWidth = 2.dp.toPx(),
                    headSize = 9.dp.toPx() * uxProgress.coerceAtLeast(0.4f),
                )
                if (uxProgress > 0.5f) {
                    drawLabel(
                        measurer = measurer,
                        text = "u_x = 17.3",
                        topLeft = Offset(launchX + 4.dp.toPx(), groundY + 4.dp.toPx()),
                        color = horizontalColor.copy(alpha = (uxProgress - 0.5f) * 2f),
                        bold = true,
                    )
                }
            }
        }
    }
}

// ── Drawing helpers ────────────────────────────────────────────────────

private fun DrawScope.drawAxes(
    ground: Offset,
    groundEnd: Offset,
    verticalEnd: Offset,
    ink: Color,
) {
    val w = 1.dp.toPx()
    // Ground (x-axis)
    drawLine(color = ink, start = ground, end = groundEnd, strokeWidth = w)
    // Vertical (y-axis)
    drawLine(color = ink, start = ground, end = verticalEnd, strokeWidth = w)
}

private fun DrawScope.drawParabola(
    launch: Offset,
    apex: Offset,
    landing: Offset,
    color: Color,
) {
    val path = Path().apply {
        moveTo(launch.x, launch.y)
        // Quadratic Bézier with the apex pulled up so the curve passes
        // through (apex.x, apex.y). For a symmetric parabola through
        // launch and landing with peak at apex, the control point sits
        // at (apex.x, 2*apex.y - launch.y).
        val ctrl = Offset(apex.x, 2f * apex.y - launch.y)
        quadraticTo(ctrl.x, ctrl.y, landing.x, landing.y)
    }
    drawPath(path = path, color = color, style = Stroke(width = 2.5.dp.toPx()))
}

private fun DrawScope.drawDottedHorizontal(
    from: Offset,
    to: Offset,
    color: Color,
) {
    drawLine(
        color = color,
        start = from, end = to,
        strokeWidth = 1.5.dp.toPx(),
        pathEffect = PathEffect.dashPathEffect(
            floatArrayOf(6.dp.toPx(), 6.dp.toPx()), 0f,
        ),
    )
}

private fun DrawScope.drawArrow(
    start: Offset,
    end: Offset,
    color: Color,
    strokeWidth: Float,
    headSize: Float,
) {
    drawLine(color = color, start = start, end = end, strokeWidth = strokeWidth)
    // Arrow head — two short lines at the end, rotated to match direction.
    val dx = end.x - start.x
    val dy = end.y - start.y
    val len = kotlin.math.sqrt(dx * dx + dy * dy).coerceAtLeast(0.0001f)
    val ux = dx / len
    val uy = dy / len
    // Two perpendicular offsets, rotated 30° back from the tip
    val cosA = cos(Math.toRadians(150.0)).toFloat()
    val sinA = sin(Math.toRadians(150.0)).toFloat()
    fun rot(c: Float, s: Float) = Offset(
        end.x + headSize * (ux * c - uy * s),
        end.y + headSize * (ux * s + uy * c),
    )
    drawLine(color = color, start = end, end = rot(cosA, sinA),
        strokeWidth = strokeWidth)
    drawLine(color = color, start = end, end = rot(cosA, -sinA),
        strokeWidth = strokeWidth)
}

private fun DrawScope.drawAngleArc(
    center: Offset,
    radius: Float,
    angleDeg: Float,
    color: Color,
) {
    // Draw an arc from the +x axis sweeping up by `angleDeg`.
    // Compose's arc convention: drawArc on a rect, sweep angle is CW from
    // 0° (3 o'clock). We want CCW from 3 o'clock, so sweep is -angleDeg.
    translate(center.x - radius, center.y - radius) {
        rotate(degrees = 0f, pivot = Offset(radius, radius)) {
            drawArc(
                color = color,
                startAngle = 0f,
                sweepAngle = -angleDeg,
                useCenter = false,
                topLeft = Offset(0f, 0f),
                size = Size(radius * 2f, radius * 2f),
                style = Stroke(width = 1.5.dp.toPx()),
            )
        }
    }
}

private fun DrawScope.drawLabel(
    measurer: TextMeasurer,
    text: String,
    topLeft: Offset,
    color: Color,
    bold: Boolean = false,
) {
    val style = TextStyle(
        color = color,
        fontSize = 12.sp,
        fontWeight = if (bold) FontWeight.SemiBold else FontWeight.Normal,
        fontFamily = FontFamily.Monospace,
    )
    val layout = measurer.measure(text, style = style)
    drawText(textLayoutResult = layout, topLeft = topLeft)
}
