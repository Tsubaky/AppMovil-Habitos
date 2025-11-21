package com.dam.apphabitos


import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.GridView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dam.apphabitos.model.DayModel
import com.dam.apphabitos.model.Habit

import java.time.LocalDate


class CalendarActivity : AppCompatActivity() {

    private lateinit var recyclerCalendar: RecyclerView
    private lateinit var rvHabits: RecyclerView
    private lateinit var habitAdapter: HabitAdapter
    private lateinit var db: DBHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calendar)

        db = DBHelper(this)

        // ----------------- CALENDARIO -----------------
        recyclerCalendar = findViewById(R.id.recyclerCalendar)

        val today = LocalDate.now()
        val days = (0..14).map {
            val date = today.plusDays(it.toLong())
            DayModel(
                dayNumber = date.dayOfMonth,
                dayName = date.dayOfWeek.name.take(3),
                date = date
            )
        }

        recyclerCalendar.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        val calendarAdapter = CalendarAdapter(days) { selected ->
            Toast.makeText(this, "Elegiste: ${selected.date}", Toast.LENGTH_SHORT).show()
        }

        recyclerCalendar.adapter = calendarAdapter


        // ----------------- HÁBITOS -----------------
        rvHabits = findViewById(R.id.rvHabits)
        rvHabits.layoutManager = LinearLayoutManager(this)

        // CARGAMOS HÁBITOS DESDE LA BASE DE DATOS
        val habitsFromDb = db.getAllHabits()

        habitAdapter = HabitAdapter(habitsFromDb.toMutableList()) { habit, isChecked ->
            db.updateHabitCompleted(habit.id, if (isChecked) 1 else 0)
        }

        rvHabits.adapter = habitAdapter
    }

    private fun showAddHabitDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_habit, null)
        val etName = dialogView.findViewById<EditText>(R.id.etHabitName)
        val gvEmojis = dialogView.findViewById<GridView>(R.id.gvEmojis)
        val btnCancel = dialogView.findViewById<Button>(R.id.btnCancel)
        val btnAdd = dialogView.findViewById<Button>(R.id.btnAdd)

        // Lista básica de emojis
        val emojisList = listOf("🔥","💪","📚","🧠","🏋️","👣","❤️","🌟","😴","🚰","🥗","🧘‍♂️")

        val selectedEmojis = mutableListOf<String>()
        gvEmojis.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, emojisList)

        gvEmojis.setOnItemClickListener { _, v, pos, _ ->
            val emoji = emojisList[pos]
            if (selectedEmojis.contains(emoji)) {
                selectedEmojis.remove(emoji)
                v.setBackgroundColor(Color.TRANSPARENT)
            } else {
                selectedEmojis.add(emoji)
                v.setBackgroundColor(Color.LTGRAY)
            }
        }

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false)
            .create()

        btnCancel.setOnClickListener { dialog.dismiss() }

        btnAdd.setOnClickListener {
            val name = etName.text.toString().trim()

            if (name.isEmpty()) {
                Toast.makeText(this, "Escribe un nombre para el hábito", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val emojis = selectedEmojis.joinToString("")



        }

        dialog.show()
    }

}

