package com.dam.apphabitos

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

class HomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        supportActionBar?.hide()

        // Mostrar username si fue pasado (si está vacío, muestra "Alex Turner")
        val tvName = findViewById<TextView>(R.id.tvName)
        val username = intent.getStringExtra("username")
        tvName.text = if (!username.isNullOrEmpty()) username else "Alex Turner"

        val btnAdd = findViewById<Button>(R.id.btnAddHabit)
        btnAdd.setOnClickListener {
            Toast.makeText(this, "Agregar hábito (implementa aquí)", Toast.LENGTH_SHORT).show()
        }

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        bottomNav.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_home -> {
                    // Ya estás en Home
                    true
                }
                R.id.nav_calendar -> {
                    startActivity(Intent(this, CalendarActivity::class.java))
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
