package com.tanovai.currencyconverter.model.data

data class RateListItem(
        val abb: String,
        val description: String,
        val rate: Double,
        val quantityRate: Double,
        val drawableRId: Int,
        val isSelected:Boolean = false
    )