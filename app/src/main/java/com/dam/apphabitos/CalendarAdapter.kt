package com.dam.apphabitos

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.dam.apphabitos.R
import com.dam.apphabitos.model.DayModel
import com.google.android.material.card.MaterialCardView


//class CalendarAdapter(
//    private val days: List<DayModel>,
//    private val onClick: (DayModel) -> Unit
//) : RecyclerView.Adapter<CalendarAdapter.DayViewHolder>() {
//
//    var selectedPosition = 0
//
//    inner class DayViewHolder(val view: View) : RecyclerView.ViewHolder(view)
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DayViewHolder {
//        val view = LayoutInflater.from(parent.context)
//            .inflate(R.layout.item_day, parent, false)
//        return DayViewHolder(view)
//    }
//
//    override fun onBindViewHolder(holder: DayViewHolder, position: Int) {
//        val item = days[position]
//        val card = holder.view as MaterialCardView
//        val txtDay = card.findViewById<TextView>(R.id.txtDayNumber)
//        val txtName = card.findViewById<TextView>(R.id.txtDayName)
//
//        txtDay.text = item.dayNumber.toString()
//        txtName.text = item.dayName
//
//        if (position == selectedPosition) {
//            card.setCardBackgroundColor(Color.parseColor("#3D7BFF"))
//        } else {
//            card.setCardBackgroundColor(Color.parseColor("#2F2F2F"))
//        }
//
//        card.setOnClickListener {
//            val oldPos = selectedPosition
//            selectedPosition = position
//            notifyItemChanged(oldPos)
//            notifyItemChanged(position)
//            onClick(item)
//        }
//    }
//
//    override fun getItemCount() = days.size
//}

class CalendarAdapter(
    private val days: List<DayModel>,
    private val onClick: (DayModel) -> Unit
) : RecyclerView.Adapter<CalendarAdapter.DayViewHolder>() {

    inner class DayViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvDayNumber: TextView = itemView.findViewById(R.id.tvDayNumber)
        val tvDayName: TextView = itemView.findViewById(R.id.tvDayName)
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

        holder.itemView.setOnClickListener { onClick(item) }
    }

    override fun getItemCount() = days.size
}

