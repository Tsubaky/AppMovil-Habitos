package com.dam.apphabitos

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

class CalendarActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calendar)

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigation)

        // Marcar el ítem actual como seleccionado
        bottomNav.selectedItemId = R.id.nav_calendar

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, MainActivity::class.java))
                    true
                }
                R.id.nav_calendar -> {
                    // Ya estás en CalendarActivity, no hacer nada
                    true
                }
//                R.id.nav_stats -> {
//                    startActivity(Intent(this, StatisticsActivity::class.java))
//                    true
//                }
//                R.id.nav_timer -> {
//                    startActivity(Intent(this, PomodoroActivity::class.java))
//                    true
//                }
                else -> false
            }
        }
    }
}

