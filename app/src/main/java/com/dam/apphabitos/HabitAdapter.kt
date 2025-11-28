package com.dam.apphabitos

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.dam.apphabitos.model.Habit

class HabitAdapter(
    private val items: MutableList<Habit>,
    private val onCheckedChanged: (habit: Habit, isChecked: Boolean) -> Unit
) : RecyclerView.Adapter<HabitAdapter.VH>() {

    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        val tvEmojis: TextView = view.findViewById(R.id.tvEmojis)
        val tvHabitName: TextView = view.findViewById(R.id.tvHabitName)
        val cbDone: CheckBox = view.findViewById(R.id.cbDone)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_habit, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val h = items[position]
        holder.tvHabitName.text = h.name
        holder.tvEmojis.text = h.emojis

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
            .setCancelable(true)  //PERMITE CERRAR AL TOCAR FUERA
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
    }

    fun remove(habit: Habit) {
        val index = items.indexOf(habit)
        if (index != -1) {
            items.removeAt(index)
            notifyItemRemoved(index)
        }
    }
}