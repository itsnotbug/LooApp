package com.example.looapp.Model

data class RestroomMapbox (
    val id: String,
    val name: String,
    val latitude: Double,
    val longitude:Double,
    val houseNumber: String?,
    val street: String?,
    val neighborhood: String?,
    val locality: String?,
    val postcode: String?,
    val place: String?,
    val district: String?,
    val region: String?,
    val country: String?,
    val formattedAddress: String?,
    val countryIso1: String?,
    val countryIso2: String?
    )
