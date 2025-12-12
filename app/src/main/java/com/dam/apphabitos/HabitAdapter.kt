package com.dam.apphabitos

import android.app.AlarmManager
import android.app.AlertDialog
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.recyclerview.widget.RecyclerView
import com.dam.apphabitos.model.Habit
import com.dam.apphabitos.notifications.HabitReminderReceiver
import com.dam.apphabitos.notifications.NotificationHelper
import com.dam.apphabitos.notifications.SnoozeReceiver
import java.util.Calendar

class HabitAdapter(

    private val context: Context,
    private val items: MutableList<Habit>,
    private val onCheckedChanged: (habit: Habit, isChecked: Boolean) -> Unit
) : RecyclerView.Adapter<HabitAdapter.VH>() {

    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        val tvEmojis: TextView = view.findViewById(R.id.tvEmojis)
        val tvHabitName: TextView = view.findViewById(R.id.tvHabitName)
        val cbDone: CheckBox = view.findViewById(R.id.cbDone)
        val tvGPSIndicator: TextView? = view.findViewById(R.id.tvGPSIndicator) // Opcional
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_habit, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val h = items[position]
        holder.tvHabitName.text = h.name
        holder.tvEmojis.text = h.emojis

        // ========== NUEVA LÓGICA GPS ==========
        if (h.gpsEnabled) {
            // Si tiene GPS activado, ocultar checkbox y mostrar indicador
            holder.cbDone.visibility = View.GONE
            holder.tvGPSIndicator?.visibility = View.VISIBLE
            holder.tvGPSIndicator?.text = "📍 GPS"

            // Click en el item abre el tracking
            holder.itemView.setOnClickListener {
                val context = holder.itemView.context
                val intent = Intent(context, ExerciseTrackingActivity::class.java)
                intent.putExtra("habitId", h.id)
                intent.putExtra("habitName", "${h.emojis} ${h.name}")
                context.startActivity(intent)
            }
        } else {
            // Comportamiento normal (sin GPS)
            holder.cbDone.visibility = View.VISIBLE
            holder.tvGPSIndicator?.visibility = View.GONE

            holder.cbDone.setOnCheckedChangeListener(null)
            holder.cbDone.isChecked = (h.completed == 1)

            holder.cbDone.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    showCompletionDialog(holder, h)
                } else {
                    h.completed = 0
                    onCheckedChanged(h, false)
                }
            }

            // Quitar el click listener del item
            holder.itemView.setOnClickListener(null)
        }
    }

    override fun getItemCount(): Int = items.size



    private fun showCompletionDialog(holder: VH, habit: Habit) {
        val context = holder.itemView.context
        val checkBox = holder.cbDone

        //Inflar el layout personalizado
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_habit_completion, null)

        val tvDialogEmoji = dialogView.findViewById<TextView>(R.id.tvDialogEmoji)
        val tvDialogTitle = dialogView.findViewById<TextView>(R.id.tvDialogTitle)
        val tvDialogMessage = dialogView.findViewById<TextView>(R.id.tvDialogMessage)
        val btnPomodoro = dialogView.findViewById<CardView>(R.id.btnPomodoro)
        val btnComplete = dialogView.findViewById<CardView>(R.id.btnComplete)

        //Configurar contenido
        tvDialogEmoji.text = habit.emojis

        if (habit.pomodoroMinutes > 0) {
            tvDialogMessage.text = "Temporizador de ${habit.pomodoroMinutes} minutos"
            btnPomodoro.visibility = View.VISIBLE
        } else {
            tvDialogMessage.text = "¿Marcar como completado?"
            btnPomodoro.visibility = View.GONE
        }

        //Crear el diálogo
        val dialog = AlertDialog.Builder(context)
            .setView(dialogView)
            .setCancelable(true)
            .create()

        //Fondo transparente para ver el diseño personalizado
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        // Listener para cerrar al tocar fuera (desmarcar checkbox)
        dialog.setOnCancelListener {
            checkBox.setOnCheckedChangeListener(null)
            checkBox.isChecked = false
            checkBox.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    showCompletionDialog(holder, habit)
                } else {
                    habit.completed = 0
                    onCheckedChanged(habit, false)
                }
            }
        }



        //Click en Ir a Pomodoro
        btnPomodoro.setOnClickListener {
            checkBox.setOnCheckedChangeListener(null)
            checkBox.isChecked = false

            checkBox.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    showCompletionDialog(holder, habit)
                } else {
                    habit.completed = 0
                    onCheckedChanged(habit, false)
                }
            }

            val intent = Intent(context, PomodoroActivity::class.java).apply {
                putExtra("pomodoroMinutes", habit.pomodoroMinutes)
                putExtra("habitName", habit.name)
            }
            context.startActivity(intent)
            dialog.dismiss()
        }

        //Click en Marcar Terminado
        btnComplete.setOnClickListener {
            habit.completed = 1
            onCheckedChanged(habit, true)
            dialog.dismiss()
        }

        dialog.show()
    }


    fun updateList(newList: List<Habit>) {
        items.clear()
        items.addAll(newList)
        notifyDataSetChanged()
    }

    fun add(habit: Habit) {
        items.add(0, habit)
        notifyItemInserted(0)

        showHabitCreatedNotification(habit.name)
        showHabitReminder(habit.name)

    }

    fun remove(habit: Habit) {
        val index = items.indexOf(habit)
        if (index != -1) {
            items.removeAt(index)
            notifyItemRemoved(index)
        }
    }
    private fun parseTimeToCalendar(time: String): Calendar {
        val parts = time.split(":")
        val hour = parts[0].toInt()
        val minute = parts[1].toInt()

        return Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)

            if (timeInMillis < System.currentTimeMillis()) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }
    }


    private fun showHabitCreatedNotification(habitName: String) {
        // 'this' (el adapter) ya no se usa, usamos la propiedad 'context'
        val builder = NotificationCompat.Builder(context, NotificationHelper.CHANNEL_HABIT_CREATED)
            .setSmallIcon(android.R.drawable.checkbox_on_background)
            .setContentTitle("Hábito creado")
            .setContentText("El hábito \"$habitName\" fue registrado exitosamente")
            .setAutoCancel(true)

        NotificationManagerCompat.from(context)
            .notify(System.currentTimeMillis().toInt(), builder.build())
    }

private fun showHabitReminder(habitName: String) {
    // Intent para posponer
    val snoozeIntent = Intent(context, SnoozeReceiver::class.java).apply {
        putExtra("habitName", habitName)
    }

    val snoozePendingIntent = PendingIntent.getBroadcast(
        context,
        2001,
        snoozeIntent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    val builder = NotificationCompat.Builder(context, NotificationHelper.CHANNEL_SMART_REMINDER)
        .setSmallIcon(android.R.drawable.ic_popup_reminder)
        .setContentTitle("Recordatorio de hábito")
        .setContentText("¡Es hora de realizar: \"$habitName\"!")
        .setAutoCancel(true)
        .addAction(
            android.R.drawable.ic_media_pause,
            "Posponer 10 min",
            snoozePendingIntent
        )

    NotificationManagerCompat.from(context)
        .notify(System.currentTimeMillis().toInt(), builder.build())
}






}