package com.dam.apphabitos.notifications;

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

class HabitReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {

        val habitName = intent.getStringExtra("habit_name") ?: "Tu hábito"
//Creacion de la notificacion, context especifica especifica el canal
        val builder = NotificationCompat.Builder(context, NotificationHelper.CHANNEL_HABIT_REMINDER)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle("Recordatorio de hábito")
            .setContentText("Es hora de realizar: $habitName")
            .setAutoCancel(true)
//Muestra la notificacion
        NotificationManagerCompat.from(context)
            .notify(System.currentTimeMillis().toInt(), builder.build())
    }
}

