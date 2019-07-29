package com.tanovai.currencyconverter.model.data

data class RateListItem(
    val abb: String,
    val description: String,
    val rate: Double,
    val quantity: Double,
    val drawableRId: Int,
    val isSelected: Boolean = false
)