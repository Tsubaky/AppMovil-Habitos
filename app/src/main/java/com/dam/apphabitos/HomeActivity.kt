package com.dam.apphabitos

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

class HomeActivity : AppCompatActivity() {
    private lateinit var bottomNav: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        supportActionBar?.hide()

        val tvName = findViewById<TextView>(R.id.tvName)
        val username = intent.getStringExtra("username")
        tvName.text = if (!username.isNullOrEmpty()) username else "Alex Turner"

        val btnAdd = findViewById<Button>(R.id.btnAddHabit)
        btnAdd.setOnClickListener {
            Toast.makeText(this, "Agregar hábito (implementa aquí)", Toast.LENGTH_SHORT).show()
        }

        bottomNav = findViewById(R.id.bottomNavigation)
        bottomNav.selectedItemId = R.id.nav_home

        bottomNav.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_home -> true
                R.id.nav_timer -> {
                    val intent = Intent(this, PomodoroActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                    startActivity(intent)
                    overridePendingTransition(0, 0)
                    true
                }
                R.id.nav_calendar -> {
                    Toast.makeText(this, "Calendario", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.nav_stats -> {
                    Toast.makeText(this, "Estadísticas", Toast.LENGTH_SHORT).show()
                    true
                }
                else -> false
            }
        }
    }

    // Se llama cuando la actividad ya está en top y recibe un nuevo Intent
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        // Actualizamos el intent de la Activity para que getIntent() devuelva el nuevo Intent
        setIntent(intent)

        val cameFromBack = intent.getBooleanExtra("fromBack", false)
        if (cameFromBack) {
            bottomNav.selectedItemId = R.id.nav_home
            // opcional: limpiar el extra para no reutilizarlo después
            this.intent.removeExtra("fromBack")
        }
    }
}
