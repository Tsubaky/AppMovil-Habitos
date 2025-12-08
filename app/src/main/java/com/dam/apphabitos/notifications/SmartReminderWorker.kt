package com.dam.apphabitos.notifications

import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.dam.apphabitos.DBHelper


class SmartReminderWorker(
    context: Context,
    params: WorkerParameters

//Worker, lo que significa que la tarea que ejecuta está garantizada para completarse
) : Worker(context, params) {
//doWork es una logica principal donde se ejecuta en segundo plano
    override fun doWork(): Result {
//inicializa la bd
        val db = DBHelper(applicationContext)
        val habits = db.getAllHabits()

        val now = System.currentTimeMillis()
        val oneDay = 24 * 60 * 60 * 1000

        habits.forEach { habit ->

            val lastTime = habit.createdAt
            val diff = now - lastTime
            val days = diff / oneDay
//Si han pasado ≥ 1 días desde la fecha de creación o última modificación y el hábito está sin completar → dispara un Smart Reminder.
            if (days >= 1 && habit.completed == 0) {
                sendSmartNotification(habit.name, days.toInt())
            }
        }

        return Result.success()
    }

    private fun sendSmartNotification(habitName: String, days: Int) {

        val builder = NotificationCompat.Builder(
            applicationContext,
            NotificationHelper.CHANNEL_SMART_REMINDER
        )
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle("Recordatorio inteligente")
            .setContentText("Hace $days días no completas el hábito: \"$habitName\"")
            .setAutoCancel(true)

        NotificationManagerCompat.from(applicationContext)
            .notify(System.currentTimeMillis().toInt(), builder.build())
    }
}
