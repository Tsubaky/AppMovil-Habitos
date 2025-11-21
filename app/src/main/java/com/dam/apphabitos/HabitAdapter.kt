package com.dam.apphabitos

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

        // Cuando el usuario cambia el checkbox avisamos al listener
        holder.cbDone.setOnCheckedChangeListener { _, isChecked ->
            // actualizar el modelo local
            h.completed = if (isChecked) 1 else 0
            onCheckedChanged(h, isChecked)
        }
    }

    override fun getItemCount(): Int = items.size

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
