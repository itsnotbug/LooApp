package com.example.looapp.Model

import com.mapbox.geojson.Point

data class Ratings(
    val markerId:String?,
    val longitude:Double?,
    val latitude:Double?,
    val cleanliness:Float?,
    val maintenance:Float?,
    val overallExperience:Float?,
    val isHygenic:Boolean?,
    val isFreeOfUse:Boolean?,
    val isMaintained:Boolean?,
    val isComfy:Boolean?,
    val hasBidet:Boolean?,
    val hasSink:Boolean?,
    val hasTrashBin:Boolean?,
    val hasToiletTries:Boolean?,
    val hasHandDryer:Boolean?,
    val Comments:String?
)