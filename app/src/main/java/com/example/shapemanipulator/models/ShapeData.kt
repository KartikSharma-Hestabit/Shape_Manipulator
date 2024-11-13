package com.example.shapemanipulator.models

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape

data class ShapeData(
    val id:String,
    val color: Color,
    var position: Offset,
    var size: Float,
    var scale:Float = 1f,
    var rotation: Float,
    val shape: Utils.ShapeType
)