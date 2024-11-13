package com.example.shapemanipulator.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.shapemanipulator.models.ShapeData
import com.example.shapemanipulator.models.Utils
import com.example.shapemanipulator.viewModel.ShaperViewModel
import java.util.UUID
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

@Composable
fun ShapingScreen(modifier: Modifier = Modifier) {

    val viewModel: ShaperViewModel = hiltViewModel()

    val shapes by viewModel.shapesList.collectAsState()
    val shapesCount by viewModel.shapesCount.collectAsState()

    val shapeTypes = listOf(Utils.ShapeType.SQUARE, Utils.ShapeType.CIRCLE)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        shapes.forEach { shape ->
            if (shape != null) {
                DraggableShape(shape = shape)
            }
        }

        Button(
            onClick = {
                viewModel.addShape(
                    ShapeData(
                        id = UUID.randomUUID().toString(),
                        color = Color(Random.nextFloat(), Random.nextFloat(), Random.nextFloat()),
                        position = Offset(100f, 100f),
                        size = 100f,
                        rotation = 0f,
                        shape = shapeTypes[Random.nextInt(shapeTypes.size)]
                    )
                )
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        ) {
            Text("Add Shape ${shapesCount}/10")
        }
    }

}
@Composable
private fun DraggableShape(
    shape: ShapeData,
) {

    var offset by remember { mutableStateOf(shape.position) }
    var scale by remember { mutableFloatStateOf(shape.scale) }
    var rotation by remember { mutableFloatStateOf(shape.rotation) }

    val viewModel: ShaperViewModel = hiltViewModel()

    // Get screen width and height
    val configuration = LocalConfiguration.current
    val screenWidthPx = with(LocalDensity.current) { configuration.screenWidthDp.dp.toPx() }
    val screenHeightPx = with(LocalDensity.current) { configuration.screenHeightDp.dp.toPx() }

    Box(
        modifier = Modifier
            .offset {
                IntOffset(
                    offset.x.toInt(),
                    offset.y.toInt(),
                )
            }
    ) {
        Column(
            modifier = Modifier
                .offset(y = (-80).dp)
                .background(Color(0x88FFFFFF))
                .padding(4.dp)
                .zIndex(2f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "X: ${offset.x.toInt()}, Y: ${offset.y.toInt()}",
                fontSize = 10.sp,
                textAlign = TextAlign.Center,
                color = Color.Black
            )
            Text(
                "Rotation: ${rotation.toInt()}°",
                fontSize = 10.sp,
                textAlign = TextAlign.Center,
                color = Color.Black
            )
            Text(
                "Scale: ${String.format("%.2f", scale)}",
                fontSize = 10.sp,
                textAlign = TextAlign.Center,
                color = Color.Black
            )
        }

        Box(
            modifier = Modifier
                .graphicsLayer(
                    scaleX = scale,
                    scaleY = scale,
                    rotationZ = rotation
                )
                .size(shape.size.dp * scale)
                .background(shape.color, if (shape.shape == Utils.ShapeType.SQUARE) {
                    RoundedCornerShape(0.dp)
                } else {
                    CircleShape
                })
                .pointerInput(Unit) {
                    detectTransformGestures { centroid, pan, zoom, rotationDelta ->
                        scale = (scale * zoom).coerceIn(0.5f, 2.0f)

                        // Adjust pan speed according to scale
                        val adjustedPanX = pan.x * scale
                        val adjustedPanY = pan.y * scale

                        // Convert rotation to radians
                        val angleRad = Math.toRadians(rotation.toDouble())
                        val rotatedPanX = (adjustedPanX * cos(angleRad) - adjustedPanY * sin(angleRad)).toFloat()
                        val rotatedPanY = (adjustedPanY * cos(angleRad) + adjustedPanX * sin(angleRad)).toFloat()

                        // Calculate new size with scale
                        val shapeWidthPx = shape.size.dp.toPx() * scale
                        val shapeHeightPx = shape.size.dp.toPx() * scale

                        // Constrain movement to screen bounds
                        val newX = (offset.x + rotatedPanX).coerceIn(0f, screenWidthPx - shapeWidthPx)
                        val newY = (offset.y + rotatedPanY).coerceIn(0f, screenHeightPx - shapeHeightPx)
                        offset = Offset(newX, newY)


                        rotation = (rotation + rotationDelta) % 360f

                        viewModel.changeProps(
                            shape.copy(
                                position = offset,
                                rotation = rotation,
                                scale = scale
                            ),
                            shape
                        )
                    }
                }
                .pointerInput(Unit) {
                    detectTapGestures(
                        onDoubleTap = { viewModel.removeShape(shape) }
                    )
                }
        )
    }
}