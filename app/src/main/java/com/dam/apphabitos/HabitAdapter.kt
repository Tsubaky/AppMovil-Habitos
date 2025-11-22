package com.dam.apphabitos

import android.app.AlertDialog
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
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

        // ⭐ NUEVO: Cuando el usuario marca el checkbox
        holder.cbDone.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                // Mostrar modal con opciones
                showCompletionDialog(holder.itemView, h)
            } else {
                // Si desmarca, simplemente actualiza
                h.completed = 0
                onCheckedChanged(h, false)
            }
        }
    }

    override fun getItemCount(): Int = items.size

    // ⭐ NUEVO: Modal al completar hábito
    private fun showCompletionDialog(view: View, habit: Habit) {
        val context = view.context

        val builder = AlertDialog.Builder(context)
        builder.setTitle("¿Cómo completaste este hábito?")

        // Si el hábito tiene temporizador configurado
        if (habit.pomodoroMinutes > 0) {
            builder.setMessage("Este hábito tiene un temporizador de ${habit.pomodoroMinutes} minutos")

            builder.setPositiveButton("Ir a Pomodoro") { dialog, _ ->
                // Navegar a PomodoroActivity con los minutos
                val intent = Intent(context, PomodoroActivity::class.java).apply {
                    putExtra("pomodoroMinutes", habit.pomodoroMinutes)
                    putExtra("habitName", habit.name)
                }
                context.startActivity(intent)
                dialog.dismiss()

                // Desmarcar el checkbox (no completar aún)
                val holder = view.tag as? VH
                holder?.cbDone?.isChecked = false
            }

            builder.setNegativeButton("Marcar Terminado") { dialog, _ ->
                // Completar directamente
                habit.completed = 1
                onCheckedChanged(habit, true)
                dialog.dismiss()
            }
        } else {
            // Si no tiene temporizador, solo preguntar si lo completó
            builder.setMessage("¿Deseas marcar este hábito como completado?")

            builder.setPositiveButton("Sí, Terminado") { dialog, _ ->
                habit.completed = 1
                onCheckedChanged(habit, true)
                dialog.dismiss()
            }

            builder.setNegativeButton("Cancelar") { dialog, _ ->
                // Desmarcar el checkbox
                val holder = view.tag as? VH
                holder?.cbDone?.isChecked = false
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
}