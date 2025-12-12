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

class HomeActivity : BaseSwipeActivity() {
    private lateinit var bottomNav: BottomNavigationView
    private lateinit var db: DBHelper
    private lateinit var adapter: HabitAdapter
    private lateinit var rvHabits: RecyclerView
    private lateinit var tvCounter: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        supportActionBar?.hide()

        val username = getUsername()
        val tvName = findViewById<TextView>(R.id.tvName)
        tvName.text = username

        db = DBHelper(this)
        tvCounter = findViewById(R.id.tvCounter)
        rvHabits = findViewById(R.id.rvHabits)

        adapter = HabitAdapter(mutableListOf()) { habit, isChecked ->
            // Actualizar en la base de datos
            db.updateHabitCompleted(habit.id, if (isChecked) 1 else 0)
            habit.completed = if (isChecked) 1 else 0

            if (isChecked) {
                //Completado: remover de Home
                adapter.remove(habit)
            }

            //Actualizar contador
            updateCounterUI()
        }

        rvHabits.layoutManager = LinearLayoutManager(this)
        rvHabits.adapter = adapter

        loadHabitsFromDb()

        val fab = findViewById<com.google.android.material.floatingactionbutton.FloatingActionButton>(R.id.fabAddHabit)
        fab.setOnClickListener { showAddHabitDialog() }

        //Hacer el contador clickeable
        val counterContainer = findViewById<FrameLayout>(R.id.counterContainer)
        counterContainer.setOnClickListener {
            val intent = Intent(this, CompletedHabitsActivity::class.java).apply {
                putExtra("username", username)
            }
            startActivity(intent)
        }

        bottomNav = findViewById(R.id.bottomNavigation)
        bottomNav.selectedItemId = R.id.nav_home

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
        //Contar todos los completados directamente de la BD
        val allHabits = db.getAllHabits()
        val completedCount = allHabits.count { it.completed == 1 }
        tvCounter.text = completedCount.toString()
    }

    private fun loadHabitsFromDb() {
        // Solo cargar hábitos de HOY o sin fecha programada
        val todayHabits = db.getTodayHabits()
        adapter.updateList(todayHabits)

        //Actualizar contador
        updateCounterUI()
    }

    private fun showAddHabitDialog() {
        val inflater = LayoutInflater.from(this)
        val view = inflater.inflate(R.layout.dialog_add_habit, null)

        val emojiSelector = view.findViewById<androidx.cardview.widget.CardView>(R.id.emojiSelector)
        val tvSelectedEmoji = view.findViewById<TextView>(R.id.tvSelectedEmoji)
        val etName = view.findViewById<EditText>(R.id.etHabitName)
        val btnSelectDateTime = view.findViewById<androidx.cardview.widget.CardView>(R.id.btnSelectDateTime)
        val tvDateTimeDisplay = view.findViewById<TextView>(R.id.tvDateTimeDisplay)
        val spinnerPomodoro = view.findViewById<Spinner>(R.id.spinnerPomodoroMinutes)
        val switchGPS = view.findViewById<androidx.appcompat.widget.SwitchCompat>(R.id.switchGPS) // ← NUEVO
        val btnAdd = view.findViewById<androidx.cardview.widget.CardView>(R.id.btnAdd)
        val btnCancel = view.findViewById<androidx.cardview.widget.CardView>(R.id.btnCancel)

        val emojis = listOf("🔥","🌙","💪","🧘","📚","☕","🏃","🍎","🛌","🧹","🎧","✍️","💡","🎯","⭐","🌟","💎","🚀")

        var selectedEmoji = "😊"
        var selectedTimestamp = System.currentTimeMillis()

        // Configurar Spinner Pomodoro
        val pomodoroOptions = listOf("Sin temporizador", "5 min", "10 min", "15 min", "20 min", "25 min", "30 min", "45 min", "60 min")
        val pomodoroValues = listOf(0, 5, 10, 15, 20, 25, 30, 45, 60)

        val spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, pomodoroOptions)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerPomodoro.adapter = spinnerAdapter
        spinnerPomodoro.setSelection(0)

        //Mostrar fecha y hora inicial
        updateDateTimeDisplay(tvDateTimeDisplay, selectedTimestamp)

        //Click en selector de emoji
        emojiSelector.setOnClickListener {
            showEmojiPicker(emojis) { selectedEmojiFromPicker ->
                selectedEmoji = selectedEmojiFromPicker
                tvSelectedEmoji.text = selectedEmoji
            }
        }

        // Click en selector de fecha/hora
        btnSelectDateTime.setOnClickListener {
            showDatePickerDialog { timestamp ->
                selectedTimestamp = timestamp
                updateDateTimeDisplay(tvDateTimeDisplay, timestamp)
            }
        }

        //Crear el diálogo
        val dialog = AlertDialog.Builder(this)
            .setView(view)
            .setCancelable(true)
            .create()

        dialog.window?.setBackgroundDrawable(android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT))

        //Click en Cancelar
        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        //Click en Añadir
        btnAdd.setOnClickListener {
            val name = etName.text.toString().trim()
            if (name.isEmpty()) {
                Toast.makeText(this, "Ingresa un nombre para el hábito", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val selectedPomodoroPosition = spinnerPomodoro.selectedItemPosition
            val pomodoroMinutes = pomodoroValues[selectedPomodoroPosition]
            val gpsEnabled = switchGPS.isChecked // ← NUEVO

            val calendar = Calendar.getInstance().apply {
                timeInMillis = selectedTimestamp
            }
            val dateFormat = java.text.SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val timeFormat = java.text.SimpleDateFormat("HH:mm", Locale.getDefault())

            val habit = Habit(
                name = name,
                emojis = selectedEmoji,
                completed = 0,
                createdAt = selectedTimestamp,
                pomodoroMinutes = pomodoroMinutes,
                date = dateFormat.format(calendar.time),
                time = timeFormat.format(calendar.time),
                gpsEnabled = gpsEnabled // ← NUEVO
            )

            val id = db.insertHabit(habit)
            habit.id = id

            adapter.add(habit)
            dialog.dismiss()

            Toast.makeText(this, "Hábito añadido ✅", Toast.LENGTH_SHORT).show()
        }

        dialog.show()
    }

    //NUEVO: Mostrar selector de emojis tipo WhatsApp
    private fun showEmojiPicker(emojis: List<String>, onEmojiSelected: (String) -> Unit) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_emoji_picker, null)
        val gvEmojis = dialogView.findViewById<GridView>(R.id.gvEmojis)

        val emojiAdapter = object : BaseAdapter() {
            override fun getCount() = emojis.size
            override fun getItem(position: Int) = emojis[position]
            override fun getItemId(position: Int) = position.toLong()
            override fun getView(position: Int, convertView: android.view.View?, parent: android.view.ViewGroup?): android.view.View {
                val tv = (convertView as? TextView) ?: TextView(this@HomeActivity).apply {
                    val size = (48 * resources.displayMetrics.density).toInt()
                    layoutParams = android.view.ViewGroup.LayoutParams(size, size)
                    textSize = 32f
                    gravity = android.view.Gravity.CENTER
                }
                tv.text = emojis[position]
                return tv
            }
        }

        gvEmojis.adapter = emojiAdapter

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(true)
            .create()

        dialog.window?.setBackgroundDrawable(android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT))

        gvEmojis.setOnItemClickListener { _, _, position, _ ->
            onEmojiSelected(emojis[position])
            dialog.dismiss()
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