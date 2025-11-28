package com.dam.apphabitos.model

data class Habit(
    var id: Long = 0L,
    var name: String = "",
    var emojis: String = "",
    var completed: Int = 0,
    var createdAt: Long = System.currentTimeMillis(),
    var date: String = "",
    var time: String = "",
    var pomodoroMinutes: Int = 0
)