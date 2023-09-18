package com.example.looapp.Model

data class RestroomItem(
    var markerId:String,
    var accessible: Boolean,
    var approved: Boolean,
    var bearing: String,
    var changing_table: Boolean,
    var city: String,
    var comment: String,
    var country: String,
    var created_at: String,
    var directions: String,
    var distance: Double,
    var downvote: Int,
    var edit_id: Int,
    var id: Int,
    var latitude: Double,
    var longitude: Double,
    var name: String,
    var state: String,
    var street: String,
    var unisex: Boolean,
    var updated_at: String,
    var upvote: Int
)