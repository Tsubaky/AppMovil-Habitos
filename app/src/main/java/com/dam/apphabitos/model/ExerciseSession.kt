package com.dam.apphabitos.model

data class ExerciseSession(
    val id: Long = 0L,
    val habitId: Long,
    val distanceKm: Double = 0.0,
    val durationSeconds: Int = 0,
    val calories: Int = 0,
    val avgSpeed: Double = 0.0,
    val sessionDate: String = "",
    val routePoints: String = ""
)