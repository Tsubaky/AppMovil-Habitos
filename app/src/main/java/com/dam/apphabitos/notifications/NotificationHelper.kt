package com.dam.apphabitos.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build

object NotificationHelper {

    //    Crear el nuevo canal de notificación
    // las constates son identificadores unicos
    const val CHANNEL_HABIT_CREATED = "habit_created"
    const val CHANNEL_HABIT_REMINDER = "habit_reminder"
    const val CHANNEL_SMART_REMINDER = "smart_reminder"


    fun createChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            val manager = context.getSystemService(NotificationManager::class.java)

            val smart = NotificationChannel(
                CHANNEL_SMART_REMINDER,
                "Recordatorios Inteligentes",
//IMPORTANCE_HIGH (Sonido, vibración y podría aparecer como notificación heads-up en la parte superior de la pantalla).
                NotificationManager.IMPORTANCE_HIGH
            )
            manager.createNotificationChannel(smart)


            manager.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_HABIT_CREATED,
                    "Hábito creado",
//Sonido por defecto, sin vibración a pantalla completa).
                    NotificationManager.IMPORTANCE_DEFAULT
                )
            )

            manager.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_HABIT_REMINDER,
                    "Recordatorio de hábitos",
                    NotificationManager.IMPORTANCE_HIGH
                )
            )
        }
    }
}

