package com.tanovai.currencyconverter.model.data

data class RateListItem(
        val abb: String,
        val name: String,
        val rate: Double,
        val drawableRId: Int
    )