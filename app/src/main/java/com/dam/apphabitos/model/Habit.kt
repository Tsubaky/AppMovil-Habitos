package com.dam.apphabitos.model

data class Habit(
    var id: Long = 0L,
    var name: String = "",
    var emojis: String = "",      // guarda varios emojis como "🔥,🌙"
    var completed: Int = 0,       // 0 = no completado, 1 = completado
    var createdAt: Long = System.currentTimeMillis(),
    var date: String = "",  // formato: yyyy-MM-dd
    var time: String = "",      // formato: HH:mm
    var pomodoroMinutes: Int = 0
)