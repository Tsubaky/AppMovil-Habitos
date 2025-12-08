package com.dam.apphabitos.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

class SnoozeReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val habitName = intent?.getStringExtra("habitName") ?: "Tu hábito"

        // Programar recordatorio +10 minutos
        val newTime = System.currentTimeMillis() + 10 * 60 * 1000 // 10 minutos

        val reminderIntent = Intent(context, HabitReminderReceiver::class.java).apply {
            putExtra("habitName", habitName)
        }

        val pending = PendingIntent.getBroadcast(
            context,
            newTime.toInt(),
            reminderIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.setExact(
            AlarmManager.RTC_WAKEUP,
            newTime,
            pending
        )

        // Notificación de confirmación
        val confirmNotification = NotificationCompat.Builder(context, NotificationHelper.CHANNEL_SMART_REMINDER)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle("Recordatorio pospuesto")
            .setContentText("Te recordaré el hábito \"$habitName\" en 10 minutos.")
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context)
            .notify(System.currentTimeMillis().toInt(), confirmNotification)
    }
}
