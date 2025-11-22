package com.dam.apphabitos

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.GridView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dam.apphabitos.model.DayModel
import com.dam.apphabitos.model.Habit
import android.content.Intent
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class CalendarActivity : AppCompatActivity() {

    private lateinit var recyclerCalendar: RecyclerView
    private lateinit var rvHabits: RecyclerView
    private lateinit var habitAdapter: HabitAdapter
    private lateinit var db: DBHelper
    private lateinit var tvHabitsTitle: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calendar)

        db = DBHelper(this)

        tvHabitsTitle = findViewById(R.id.habitsTitle)

        // ----------------- CALENDARIO -----------------
        recyclerCalendar = findViewById(R.id.recyclerCalendar)

        val today = LocalDate.now()
        val days = (0..14).map {
            val date = today.plusDays(it.toLong())
            DayModel(
                dayNumber = date.dayOfMonth,
                dayName = date.dayOfWeek.name.take(3),
                date = date,
                isSelected = (it == 0)  // El primer día (hoy) está seleccionado por defecto
            )
        }

        recyclerCalendar.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        val calendarAdapter = CalendarAdapter(days) { selectedDay ->
            // ⭐ Al hacer clic en un día, filtrar hábitos por esa fecha
            loadHabitsForDate(selectedDay.date)
        }

        recyclerCalendar.adapter = calendarAdapter

        // ----------------- HÁBITOS -----------------
        rvHabits = findViewById(R.id.rvHabits)
        rvHabits.layoutManager = LinearLayoutManager(this)

        habitAdapter = HabitAdapter(mutableListOf()) { habit, isChecked ->
            db.updateHabitCompleted(habit.id, if (isChecked) 1 else 0)
        }

        rvHabits.adapter = habitAdapter

        // ⭐ Cargar hábitos del día de hoy al iniciar
        loadHabitsForDate(today)

        // ----------------- NAVEGACIÓN -----------------
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        val username = intent.getStringExtra("username") ?: "Usuario"

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    val i = Intent(this, HomeActivity::class.java).apply {
                        putExtra("username", username)
                        addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                    }
                    startActivity(i)
                    true
                }
                R.id.nav_calendar -> true
                R.id.nav_timer -> {
                    val i = Intent(this, PomodoroActivity::class.java).apply {
                        putExtra("username", username)
                        addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                    }
                    startActivity(i)
                    true
                }
                R.id.nav_stats -> {
                    val i = Intent(this, StatisticsActivity::class.java).apply {
                        putExtra("username", username)
                        addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                    }
                    startActivity(i)
                    true
                }
                else -> false
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // ⭐ Recargar hábitos del día actual al volver a la vista
        loadHabitsForDate(LocalDate.now())
    }

    // ⭐ NUEVO: Cargar hábitos de una fecha específica
    private fun loadHabitsForDate(date: LocalDate) {
        val dateString = date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        val habitsForDate = db.getHabitsByDate(dateString)

        // Actualizar título
        val dayName = when (date.dayOfWeek.value) {
            1 -> "Lunes"
            2 -> "Martes"
            3 -> "Miércoles"
            4 -> "Jueves"
            5 -> "Viernes"
            6 -> "Sábado"
            7 -> "Domingo"
            else -> ""
        }

        tvHabitsTitle.text = if (date == LocalDate.now()) {
            "Tus hábitos de hoy"
        } else {
            "Hábitos del $dayName ${date.dayOfMonth}"
        }

        if (habitsForDate.isEmpty()) {
            Toast.makeText(this, "No hay hábitos para este día", Toast.LENGTH_SHORT).show()
        }

        habitAdapter.updateList(habitsForDate)
    }
}