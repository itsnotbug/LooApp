package com.example.looapp

import android.util.Log
import com.example.looapp.Model.RestroomItem
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore

class FirebaseConnection {
    private var fireStore = FirebaseFirestore.getInstance()
    private fun documentToRestroomItem(document: DocumentSnapshot): RestroomItem {
        val data = document.data
        if (data != null) {
            return RestroomItem(
                data["markerId"].toString(),
                data["accessible"] as? Boolean ?: false,
                data["approved"] as? Boolean ?: false,
                data["bearing"].toString(),
                data["changing_table"] as? Boolean ?: false,
                data["city"].toString(),
                data["comment"].toString(),
                data["country"].toString(),
                data["created_at"].toString(),
                data["directions"].toString(),
                (data["distance"] as? Number)?.toDouble() ?: 0.0,
                (data["downvote"] as? Number)?.toInt() ?: 0,
                (data["edit_id"] as? Number)?.toInt() ?: 0,
                (data["id"] as? Number)?.toInt() ?: 0,
                (data["latitude"] as? Number)?.toDouble() ?: 0.0,
                (data["longitude"] as? Number)?.toDouble() ?: 0.0,
                data["name"].toString(),
                data["state"].toString(),
                data["street"].toString(),
                data["unisex"] as? Boolean ?: false,
                data["updated_at"].toString(),
                (data["upvote"] as? Number)?.toInt() ?: 0
            )
        }
        return RestroomItem("", false, false, "", false, "", "", "", "", "", 0.0, 0, 0, 0, 0.0, 0.0, "", "", "", false, "", 0)
    }

    //Get all the restroomItem data to the firebase
    fun getAllRestroom(collectionName: String, callback: (MutableList<RestroomItem>) -> Unit) {
        val db = FirebaseFirestore.getInstance()
        val collectionRef = db.collection(collectionName)
        collectionRef.get()
            .addOnSuccessListener { result ->
                val restroomList = mutableListOf<RestroomItem>()
                for (document in result) {
                    val restroomItem = documentToRestroomItem(document)
                    restroomList.add(restroomItem)
                }
                callback(restroomList)
            }
            .addOnFailureListener { exception ->
                Log.d("Firestore", "Error getting documents: $exception")
            }
    }

    //Inserts api data to restroomItem
      fun saveRestroomItem(restroomItem: RestroomItem, markerId: String) {
        val collectionRef = fireStore.collection(
            "restroom")
        restroomItem.markerId = markerId
        // Add a new document with a randomly generated ID
        collectionRef
            .add(restroomItem)
            .addOnSuccessListener { documentReference ->
                Log.d("Firestore", "DocumentSnapshot added with ID: ${documentReference.id}")
            }
            .addOnFailureListener { e ->
                Log.w("Firestore", "Error adding document", e)
            }
    }
}
