package com.dam.apphabitos

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.dam.apphabitos.model.DayModel

class CalendarAdapter(
    private val days: List<DayModel>,
    private val onClick: (DayModel) -> Unit
) : RecyclerView.Adapter<CalendarAdapter.DayViewHolder>() {

    private var selectedPosition = 0  // Por defecto el primer día (hoy) está seleccionado

    inner class DayViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvDayNumber: TextView = itemView.findViewById(R.id.tvDayNumber)
        val tvDayName: TextView = itemView.findViewById(R.id.tvDayName)
        val container: View = itemView  //Referencia al contenedor
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DayViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_day_calendar, parent, false)
        return DayViewHolder(view)
    }

    override fun onBindViewHolder(holder: DayViewHolder, position: Int) {
        val item = days[position]
        holder.tvDayNumber.text = item.dayNumber.toString()
        holder.tvDayName.text = item.dayName

// Destacar el día seleccionado
        if (position == selectedPosition) {
            holder.container.setBackgroundResource(R.drawable.day_selected_background)  // ⭐ CAMBIO
            holder.tvDayName.setTextColor(Color.WHITE)
            holder.tvDayNumber.setTextColor(Color.WHITE)
        } else {
            holder.container.setBackgroundResource(R.drawable.day_normal_background)
            holder.tvDayName.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.light_text))
            holder.tvDayNumber.setTextColor(Color.WHITE)
        }

        // Al hacer clic, actualizar selección
        holder.itemView.setOnClickListener {
            val previousPosition = selectedPosition
            selectedPosition = holder.adapterPosition

            // Actualizar UI
            notifyItemChanged(previousPosition)
            notifyItemChanged(selectedPosition)

            // Notificar al listener
            onClick(item)
        }
    }

    override fun getItemCount() = days.size
}