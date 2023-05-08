package com.example.fitvolution

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.DocumentReference


data class Workout(
@DocumentId
    val id: String = "",
    val name: String = "",
    val user: DocumentReference? = null,
    val exercises: List<DocumentReference> = listOf(),
    var exercisesList: List<WtExercise> = listOf(),
    var favourite: Boolean = false
)
