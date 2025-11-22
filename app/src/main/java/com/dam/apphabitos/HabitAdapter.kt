package com.dam.apphabitos

import android.app.AlertDialog
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.CompoundButton
import android.widget.TextView
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

        // Evitar callback al reusar vistas
        holder.cbDone.setOnCheckedChangeListener(null)
        holder.cbDone.isChecked = (h.completed == 1)

        holder.cbDone.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                // Usuario marcó el checkbox
                showCompletionDialog(holder, h)
            } else {
                // Usuario desmarcó el checkbox (desde Completados)
                h.completed = 0
                onCheckedChanged(h, false)
            }
        }
    }

    override fun getItemCount(): Int = items.size

    private fun showCompletionDialog(holder: VH, habit: Habit) {
        val context = holder.itemView.context
        val checkBox = holder.cbDone

        val builder = AlertDialog.Builder(context)
        builder.setTitle("¿Cómo completaste este hábito?")

        if (habit.pomodoroMinutes > 0) {
            builder.setMessage("Este hábito tiene un temporizador de ${habit.pomodoroMinutes} minutos")

            builder.setPositiveButton("Ir a Pomodoro") { dialog, _ ->
                // ⭐ IMPORTANTE: Desmarcar el checkbox porque NO está terminado aún
                checkBox.setOnCheckedChangeListener(null)
                checkBox.isChecked = false

                // Restaurar el listener
                checkBox.setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked) {
                        showCompletionDialog(holder, habit)
                    } else {
                        habit.completed = 0
                        onCheckedChanged(habit, false)
                    }
                }

                // Ir a Pomodoro
                val intent = Intent(context, PomodoroActivity::class.java).apply {
                    putExtra("pomodoroMinutes", habit.pomodoroMinutes)
                    putExtra("habitName", habit.name)
                }
                context.startActivity(intent)
                dialog.dismiss()
            }

            builder.setNegativeButton("Marcar Terminado") { dialog, _ ->
                // ⭐ Marcar como completado
                habit.completed = 1
                onCheckedChanged(habit, true)
                dialog.dismiss()
            }
        } else {
            builder.setMessage("¿Deseas marcar este hábito como completado?")

            builder.setPositiveButton("Sí, Terminado") { dialog, _ ->
                habit.completed = 1
                onCheckedChanged(habit, true)
                dialog.dismiss()
            }

            builder.setNegativeButton("Cancelar") { dialog, _ ->
                // ⭐ Desmarcar porque el usuario canceló
                checkBox.setOnCheckedChangeListener(null)
                checkBox.isChecked = false

                // Restaurar el listener
                checkBox.setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked) {
                        showCompletionDialog(holder, habit)
                    } else {
                        habit.completed = 0
                        onCheckedChanged(habit, false)
                    }
                }
                dialog.dismiss()
            }
        }

        builder.setCancelable(false)
        builder.show()
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