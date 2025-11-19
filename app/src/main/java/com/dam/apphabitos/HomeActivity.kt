package com.dam.apphabitos

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.dam.apphabitos.model.Habit

class HomeActivity : AppCompatActivity() {
    private lateinit var bottomNav: BottomNavigationView
    private lateinit var db: DBHelper
    private lateinit var adapter: HabitsAdapter
    private lateinit var rvHabits: RecyclerView
    private lateinit var tvCounter: TextView
    private var completedCount = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        supportActionBar?.hide()

        // --- Leer username pasado desde login (fallback "Usuario") ---
        val username = intent.getStringExtra("username") ?: "Usuario"
        val tvName = findViewById<TextView>(R.id.tvName)
        tvName.text = username
        // ----------------------------------------------------------

        // Inicializaciones
        db = DBHelper(this)
        tvCounter = findViewById(R.id.tvCounter)
        rvHabits = findViewById(R.id.rvHabits)

        adapter = HabitsAdapter(mutableListOf()) { habit, isChecked ->
            // Al marcar/desmarcar actualizamos DB y contador
            db.updateHabitCompleted(habit.id, if (isChecked) 1 else 0)
            habit.completed = if (isChecked) 1 else 0
            if (isChecked) completedCount++ else completedCount--
            updateCounterUI()
        }

        rvHabits.layoutManager = LinearLayoutManager(this)
        rvHabits.adapter = adapter

        // Cargar datos
        loadHabitsFromDb()

        // FAB para abrir modal
        val fab = findViewById<com.google.android.material.floatingactionbutton.FloatingActionButton>(R.id.fabAddHabit)
        fab.setOnClickListener { showAddHabitDialog() }

        // Bottom navigation
        bottomNav = findViewById(R.id.bottomNavigation)

        // Listener: abrimos actividades y re-pasamos username. Usamos FLAG_ACTIVITY_REORDER_TO_FRONT
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> true
                R.id.nav_timer -> {
                    val i = Intent(this, PomodoroActivity::class.java).apply {
                        putExtra("username", username)
                        addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                    }
                    startActivity(i)
                    true
                }
                R.id.nav_calendar -> {
                    val i = Intent(this, CalendarActivity::class.java).apply {
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

    private fun updateCounterUI() {
        tvCounter.text = completedCount.toString()
    }

    private fun loadHabitsFromDb() {
        val list = db.getAllHabits()
        adapter.updateList(list)
        // calcular completados
        completedCount = list.count { it.completed == 1 }
        updateCounterUI()
    }

    private fun showAddHabitDialog() {
        val inflater = LayoutInflater.from(this)
        val view = inflater.inflate(R.layout.dialog_add_habit, null)
        val etName = view.findViewById<EditText>(R.id.etHabitName)
        val gvEmojis = view.findViewById<GridView>(R.id.gvEmojis)
        val btnAdd = view.findViewById<Button>(R.id.btnAdd)
        val btnCancel = view.findViewById<Button>(R.id.btnCancel)

        // Lista de emojis ejemplo (puedes añadir más)
        val emojis = listOf("🔥","🌙","💪","🧘","📚","☕","🏃","🍎","🛌","🧹","🎧","✍️")

        // Selección única: guardamos solo el índice seleccionado
        var selectedIndex = -1

        // Adapter simple para emojis (TextView)
        val emojiAdapter = object : BaseAdapter() {
            override fun getCount() = emojis.size
            override fun getItem(position: Int) = emojis[position]
            override fun getItemId(position: Int) = position.toLong()
            override fun getView(position: Int, convertView: android.view.View?, parent: android.view.ViewGroup?): android.view.View {
                val tv = (convertView as? TextView) ?: TextView(this@HomeActivity).apply {
                    val pad = (8 * resources.displayMetrics.density).toInt()
                    setPadding(pad, pad, pad, pad)
                    textSize = 20f
                    gravity = android.view.Gravity.CENTER
                }
                tv.text = emojis[position]
                // Fondo si está seleccionado (selección única)
                if (position == selectedIndex) {
                    tv.setBackgroundResource(android.R.drawable.dialog_holo_light_frame)
                } else {
                    tv.setBackgroundResource(0)
                }
                return tv
            }
        }

        gvEmojis.adapter = emojiAdapter

        gvEmojis.setOnItemClickListener { _, _, position, _ ->
            // Selección única: si tocas el mismo índice lo deseleccionas, si no lo seleccionas
            selectedIndex = if (selectedIndex == position) -1 else position
            emojiAdapter.notifyDataSetChanged()
        }

        val dialog = AlertDialog.Builder(this)
            .setView(view)
            .create()

        btnCancel.setOnClickListener { dialog.dismiss() }
        btnAdd.setOnClickListener {
            val name = etName.text.toString().trim()
            if (name.isEmpty()) {
                etName.error = "Ingresa un nombre"
                return@setOnClickListener
            }
            // Si no hay emoji seleccionado, puedes forzar a elegir uno o dejar vacío
            val chosen = if (selectedIndex != -1) emojis[selectedIndex] else ""

            val habit = Habit(name = name, emojis = chosen, completed = 0)
            val id = db.insertHabit(habit)
            habit.id = id
            adapter.add(habit)
            dialog.dismiss()
            // recargar contador (no cambia hasta marcar)
            updateCounterUI()
            Toast.makeText(this, "Hábito añadido", Toast.LENGTH_SHORT).show()
        }

        dialog.show()

    }

}
