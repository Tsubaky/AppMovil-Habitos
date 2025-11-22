package com.dam.apphabitos

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.dam.apphabitos.model.Habit
import java.util.Calendar
import java.util.Locale

class HomeActivity : AppCompatActivity() {
    private lateinit var bottomNav: BottomNavigationView
    private lateinit var db: DBHelper
    private lateinit var adapter: HabitAdapter
    private lateinit var rvHabits: RecyclerView
    private lateinit var tvCounter: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        supportActionBar?.hide()

        val username = intent.getStringExtra("username") ?: "Usuario"
        val tvName = findViewById<TextView>(R.id.tvName)
        tvName.text = username

        db = DBHelper(this)
        tvCounter = findViewById(R.id.tvCounter)
        rvHabits = findViewById(R.id.rvHabits)

        adapter = HabitAdapter(mutableListOf()) { habit, isChecked ->
            // ⭐ Actualizar en la base de datos
            db.updateHabitCompleted(habit.id, if (isChecked) 1 else 0)
            habit.completed = if (isChecked) 1 else 0

            if (isChecked) {
                // ⭐ Completado: remover de Home
                adapter.remove(habit)
            }

            // ⭐ Actualizar contador
            updateCounterUI()
        }

        rvHabits.layoutManager = LinearLayoutManager(this)
        rvHabits.adapter = adapter

        loadHabitsFromDb()

        val fab = findViewById<com.google.android.material.floatingactionbutton.FloatingActionButton>(R.id.fabAddHabit)
        fab.setOnClickListener { showAddHabitDialog() }

        // ⭐ Hacer el contador clickeable
        val counterContainer = findViewById<FrameLayout>(R.id.counterContainer)
        counterContainer.setOnClickListener {
            val intent = Intent(this, CompletedHabitsActivity::class.java).apply {
                putExtra("username", username)
            }
            startActivity(intent)
        }

        bottomNav = findViewById(R.id.bottomNavigation)

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

    override fun onResume() {
        super.onResume()
        loadHabitsFromDb()
    }

    private fun updateCounterUI() {
        // ⭐ Contar TODOS los completados directamente de la BD
        val allHabits = db.getAllHabits()
        val completedCount = allHabits.count { it.completed == 1 }
        tvCounter.text = completedCount.toString()
    }

    private fun loadHabitsFromDb() {
        // ⭐ Solo cargar hábitos de HOY o sin fecha programada
        val todayHabits = db.getTodayHabits()
        adapter.updateList(todayHabits)

        // ⭐ Actualizar contador
        updateCounterUI()
    }

    private fun showAddHabitDialog() {
        val inflater = LayoutInflater.from(this)
        val view = inflater.inflate(R.layout.dialog_add_habit, null)
        val etName = view.findViewById<EditText>(R.id.etHabitName)
        val gvEmojis = view.findViewById<GridView>(R.id.gvEmojis)
        val btnAdd = view.findViewById<Button>(R.id.btnAdd)
        val btnCancel = view.findViewById<Button>(R.id.btnCancel)
        val tvDateTimeDisplay = view.findViewById<TextView>(R.id.tvDateTimeDisplay)
        val btnSelectDateTime = view.findViewById<Button>(R.id.btnSelectDateTime)
        val spinnerPomodoro = view.findViewById<Spinner>(R.id.spinnerPomodoroMinutes)

        val emojis = listOf("🔥","🌙","💪","🧘","📚","☕","🏃","🍎","🛌","🧹","🎧","✍️")

        var selectedIndex = -1
        var selectedTimestamp = System.currentTimeMillis()

        val pomodoroOptions = listOf("Sin temporizador", "5 min", "10 min", "15 min", "20 min", "25 min", "30 min", "45 min", "60 min")
        val pomodoroValues = listOf(0, 5, 10, 15, 20, 25, 30, 45, 60)

        val spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, pomodoroOptions)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerPomodoro.adapter = spinnerAdapter
        spinnerPomodoro.setSelection(0)

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
            selectedIndex = if (selectedIndex == position) -1 else position
            emojiAdapter.notifyDataSetChanged()
        }

        updateDateTimeDisplay(tvDateTimeDisplay, selectedTimestamp)

        btnSelectDateTime?.setOnClickListener {
            showDatePickerDialog { timestamp ->
                selectedTimestamp = timestamp
                updateDateTimeDisplay(tvDateTimeDisplay, timestamp)
            }
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

            val chosen = if (selectedIndex != -1) emojis[selectedIndex] else ""
            val selectedPomodoroPosition = spinnerPomodoro.selectedItemPosition
            val pomodoroMinutes = pomodoroValues[selectedPomodoroPosition]

            // ⭐ FORMATEAR FECHA Y HORA CORRECTAMENTE
            val calendar = Calendar.getInstance().apply {
                timeInMillis = selectedTimestamp
            }
            val dateFormat = java.text.SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val timeFormat = java.text.SimpleDateFormat("HH:mm", Locale.getDefault())

            val habit = Habit(
                name = name,
                emojis = chosen,
                completed = 0,
                createdAt = selectedTimestamp,
                pomodoroMinutes = pomodoroMinutes,
                date = dateFormat.format(calendar.time),  // ⭐ AGREGAR FECHA
                time = timeFormat.format(calendar.time)   // ⭐ AGREGAR HORA
            )

            val id = db.insertHabit(habit)
            habit.id = id

            adapter.add(habit)
            dialog.dismiss()

            Toast.makeText(this, "Hábito añadido", Toast.LENGTH_SHORT).show()
        }

        dialog.show()
    }

    private fun showDatePickerDialog(onDateTimeSelected: (Long) -> Unit) {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
            showTimePickerDialog(selectedYear, selectedMonth, selectedDay, onDateTimeSelected)
        }, year, month, day).show()
    }

    private fun showTimePickerDialog(year: Int, month: Int, day: Int, onDateTimeSelected: (Long) -> Unit) {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        TimePickerDialog(this, { _, selectedHour, selectedMinute ->
            val finalCalendar = Calendar.getInstance().apply {
                set(year, month, day, selectedHour, selectedMinute, 0)
            }
            onDateTimeSelected(finalCalendar.timeInMillis)
        }, hour, minute, true).show()
    }

    private fun updateDateTimeDisplay(tvDisplay: TextView?, timestamp: Long) {
        if (tvDisplay == null) return
        val calendar = Calendar.getInstance().apply {
            timeInMillis = timestamp
        }
        val dateFormat = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        tvDisplay.text = dateFormat.format(calendar.time)
    }
}