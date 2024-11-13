package com.example.shapemanipulator.viewModel

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.example.shapemanipulator.models.ShapeData
import com.example.shapemanipulator.models.Utils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class ShaperViewModel @Inject constructor(private val savedStateHandle: SavedStateHandle) :
    ViewModel() {

    private val _shapesCount = MutableStateFlow<Int>(2)
    val shapesCount = _shapesCount.asStateFlow()
    private val _shapesList = MutableStateFlow(
        savedStateHandle.get<List<ShapeData?>>("shapes") ?: listOf(
            ShapeData(
                UUID.randomUUID().toString(),
                Color.Red,
                Offset(100f, 100f),
                100f,
                1f,
                0f,
                Utils.ShapeType.CIRCLE
            ),
            ShapeData(
                UUID.randomUUID().toString(),
                Color.Yellow,
                Offset(100f, 100f),
                100f,
                1f,
                0f,
                Utils.ShapeType.SQUARE
            )
        )
    )

    val shapesList = _shapesList.asStateFlow()

    init {
        savedStateHandle["shapes"] = _shapesList.value
    }

    fun addShape(data: ShapeData) {
        if (shapesCount.value < 10) {
            _shapesList.value += data
            _shapesCount.value += 1
            savedStateHandle["shapes"] = _shapesList.value
        }
    }

    fun removeShape(shapeData: ShapeData) {
        _shapesCount.value -= 1
        if (_shapesCount.value == 0) {
            _shapesList.value = listOf()
            savedStateHandle["shapes"] = _shapesList.value
        } else {
            val index = _shapesList.value.indexOf(shapeData)
            val list = _shapesList.value.toMutableList()
            list[index] = null
            _shapesList.value = list
            savedStateHandle["shapes"] = _shapesList.value
        }


    }

    fun changeProps(updatedShapeData: ShapeData, shapeData: ShapeData) {
        _shapesList.value[_shapesList.value.indexOf(shapeData)]!!.position = updatedShapeData.position
        _shapesList.value[_shapesList.value.indexOf(shapeData)]!!.rotation = updatedShapeData.rotation
        _shapesList.value[_shapesList.value.indexOf(shapeData)]!!.scale = updatedShapeData.scale
    }
}