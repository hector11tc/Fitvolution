package com.example.fitvolution

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentReference

data class Metrics(
    val date: Timestamp? = null,
    val user: DocumentReference? = null,
    val values: MetricsValues = MetricsValues()
)

data class MetricsValues(
    val height: Float = 0f,
    val weight: Float = 0f,
    val bodyFat: Int = 0,
    val bodyWater: Int = 0,
    val muscleMass: Float = 0f
)

