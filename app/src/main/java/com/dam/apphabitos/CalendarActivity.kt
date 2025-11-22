package com.dam.apphabitos

import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.content.Intent
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dam.apphabitos.model.DayModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class CalendarActivity : BaseSwipeActivity() {

    private lateinit var recyclerCalendar: RecyclerView
    private lateinit var rvHabits: RecyclerView
    private lateinit var habitAdapter: HabitTimelineAdapter
    private lateinit var db: DBHelper
    private lateinit var tvHabitsTitle: TextView
    private lateinit var emptyState: LinearLayout
    private lateinit var tvWeeklyProgress: TextView
    private lateinit var tvStreak: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calendar)

        val username = getUsername()
        db = DBHelper(this)

        tvHabitsTitle = findViewById(R.id.habitsTitle)
        emptyState = findViewById(R.id.emptyState)
        tvWeeklyProgress = findViewById(R.id.tvWeeklyProgress)
        tvStreak = findViewById(R.id.tvStreak)

        // Calcular estadísticas semanales
        updateWeeklyStats()

        // Calendario
        recyclerCalendar = findViewById(R.id.recyclerCalendar)

        val today = LocalDate.now()
        val days = (0..14).map {
            val date = today.plusDays(it.toLong())
            DayModel(
                dayNumber = date.dayOfMonth,
                dayName = date.dayOfWeek.name.take(3),
                date = date,
                isSelected = (it == 0)
            )
        }

        recyclerCalendar.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        val calendarAdapter = CalendarAdapter(days) { selectedDay ->
            loadHabitsForDate(selectedDay.date)
        }

        recyclerCalendar.adapter = calendarAdapter

        // Hábitos
        rvHabits = findViewById(R.id.rvHabits)
        rvHabits.layoutManager = LinearLayoutManager(this)

        habitAdapter = HabitTimelineAdapter(mutableListOf()) { habit, isChecked ->
            db.updateHabitCompleted(habit.id, if (isChecked) 1 else 0)
            updateWeeklyStats()
        }

        rvHabits.adapter = habitAdapter

        loadHabitsForDate(today)

        // Navegación
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        bottomNav.selectedItemId = R.id.nav_calendar

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, HomeActivity::class.java).apply {
                        putExtra("username", username)
                        addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                    })
                    true
                }
                R.id.nav_calendar -> true
                R.id.nav_timer -> {
                    startActivity(Intent(this, PomodoroActivity::class.java).apply {
                        putExtra("username", username)
                        addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                    })
                    true
                }
                R.id.nav_stats -> {
                    startActivity(Intent(this, StatisticsActivity::class.java).apply {
                        putExtra("username", username)
                        addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                    })
                    true
                }
                else -> false
            }
        }
    }

    private fun updateWeeklyStats() {
        val allHabits = db.getAllHabits()
        val completedThisWeek = allHabits.count { it.completed == 1 }
        val totalThisWeek = allHabits.size

        tvWeeklyProgress.text = "$completedThisWeek/$totalThisWeek"
        tvStreak.text = "🔥 5 días" // Puedes calcular la racha real aquí
    }

    override fun onResume() {
        super.onResume()
        loadHabitsForDate(LocalDate.now())
        updateWeeklyStats()
    }

    private fun loadHabitsForDate(date: LocalDate) {
        val dateString = date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        val habitsForDate = db.getHabitsByDate(dateString)

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
            "Hábitos de hoy"
        } else {
            "Hábitos del $dayName ${date.dayOfMonth}"
        }

        if (habitsForDate.isEmpty()) {
            emptyState.visibility = View.VISIBLE
            rvHabits.visibility = View.GONE
        } else {
            emptyState.visibility = View.GONE
            rvHabits.visibility = View.VISIBLE
            habitAdapter.updateList(habitsForDate)
        }
    }
}