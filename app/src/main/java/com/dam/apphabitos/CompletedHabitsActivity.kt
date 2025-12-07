package com.dam.apphabitos

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlin.collections.mutableListOf

class CompletedHabitsActivity : AppCompatActivity() {

    private lateinit var db: DBHelper
    private lateinit var rvCompletedHabits: RecyclerView
    private lateinit var tvEmptyMessage: TextView
    private lateinit var adapter: HabitAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_completed_habits)

        val username = intent.getStringExtra("username") ?: "Usuario"

        db = DBHelper(this)

        tvEmptyMessage = findViewById(R.id.tvEmptyMessage)
        rvCompletedHabits = findViewById(R.id.rvCompletedHabits)
        rvCompletedHabits.layoutManager = LinearLayoutManager(this)

        // Adapter simple, sin funcionalidad de check
        adapter = HabitAdapter(this,mutableListOf()) { habit, isChecked ->
            // No hacemos nada, ya están completados
            db.updateHabitCompleted(habit.id, if (isChecked) 1 else 0)
        }

        rvCompletedHabits.adapter = adapter

        loadCompletedHabits()

        // Navegación
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigation)

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    val i = Intent(this, HomeActivity::class.java).apply {
                        putExtra("username", username)
                        addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                    }
                    startActivity(i)
                    finish()
                    true
                }
                R.id.nav_timer -> {
                    val i = Intent(this, PomodoroActivity::class.java).apply {
                        putExtra("username", username)
                        addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                    }
                    startActivity(i)
                    finish()
                    true
                }
                R.id.nav_calendar -> {
                    val i = Intent(this, CalendarActivity::class.java).apply {
                        putExtra("username", username)
                        addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                    }
                    startActivity(i)
                    finish()
                    true
                }
                R.id.nav_stats -> {
                    val i = Intent(this, StatisticsActivity::class.java).apply {
                        putExtra("username", username)
                        addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                    }
                    startActivity(i)
                    finish()
                    true
                }
                else -> false
            }
        }
    }

    private fun loadCompletedHabits() {
        val completedList = db.getCompletedHabits()

        if (completedList.isEmpty()) {
            tvEmptyMessage.visibility = android.view.View.VISIBLE
            rvCompletedHabits.visibility = android.view.View.GONE
        } else {
            tvEmptyMessage.visibility = android.view.View.GONE
            rvCompletedHabits.visibility = android.view.View.VISIBLE
            adapter.updateList(completedList)
        }
    }
}