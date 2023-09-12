package com.example.looapp.Model

data class Restroom(
    val markerId:String,
    val longitude:Double,
    val latitude:Double,
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