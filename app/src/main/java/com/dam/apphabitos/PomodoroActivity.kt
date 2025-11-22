package com.dam.apphabitos

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.bottomnavigation.BottomNavigationView

class PomodoroActivity : AppCompatActivity() {

    // Vistas
    private lateinit var tvTimer: TextView
    private lateinit var tvMode: TextView
    private lateinit var btnStart: Button
    private lateinit var btnReset: Button
    private lateinit var btnFocus: Button
    private lateinit var btnShortBreak: Button
    private lateinit var btnLongBreak: Button
    private lateinit var btnBack: ImageView
    private lateinit var bottomNav: BottomNavigationView

    // Temporizador
    private var timer: CountDownTimer? = null
    private var isRunning = false
    private var currentMode = TimerMode.FOCUS
    private var timeInMinutes = 45
    private var remainingMillis: Long = 0L

    enum class TimerMode(val displayName: String, val minutes: Int) {
        FOCUS("Enfocado", 45),
        SHORT_BREAK("Descanso corto", 5),
        LONG_BREAK("Descanso largo", 15)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pomodoro)

        val username = intent.getStringExtra("username") ?: "Usuario"

        // ⭐ NUEVO: Recibir minutos del hábito
        val pomodoroMinutes = intent.getIntExtra("pomodoroMinutes", 0)
        val habitName = intent.getStringExtra("habitName") ?: ""

        // Si vino desde un hábito con temporizador
        if (pomodoroMinutes > 0) {
            timeInMinutes = pomodoroMinutes
            currentMode = TimerMode.FOCUS
            // Opcional: mostrar el nombre del hábito
            Toast.makeText(this, "Temporizador para: $habitName", Toast.LENGTH_LONG).show()
        }

        bottomNav = findViewById(R.id.bottomNavigation)

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
                R.id.nav_timer -> true
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

        // Inicializar vistas (debe hacerse después de setContentView)
        initViews()
        setupTabButtons()
        updateTimerDisplay()

        // Manejo del botón físico "Atrás" (traer Home)
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val intent = Intent(this@PomodoroActivity, HomeActivity::class.java).apply {
                    putExtra("username", username)
                    addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                }
                startActivity(intent)
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                finish()
            }
        })
    }

    // Inicializa las vistas y listeners básicos
    private fun initViews() {
        tvTimer = findViewById(R.id.tvTimer)
        tvMode = findViewById(R.id.tvMode)
        btnStart = findViewById(R.id.btnStart)
        btnReset = findViewById(R.id.btnReset)
        btnFocus = findViewById(R.id.btnFocus)
        btnShortBreak = findViewById(R.id.btnShortBreak)
        btnLongBreak = findViewById(R.id.btnLongBreak)
        btnBack = findViewById(R.id.btnBack)

        btnBack.setOnClickListener {
            // Si el usuario pulsa la flecha, volvemos a Home
            val intent = Intent(this, HomeActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
            }
            startActivity(intent)
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            finish()
        }

        btnStart.setOnClickListener {
            if (!isRunning) startTimer() else stopTimer()
        }

        btnReset.setOnClickListener { resetTimer() }

        tvTimer.setOnClickListener {
            if (!isRunning) {
                // permitir cambio de minutos si quieres (puedes implementar dialog)
                Toast.makeText(this, "Mantén presionado o implementa diálogo para cambiar minutos", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "No puedes cambiar el tiempo mientras corre", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Configura los botones de modo (Focus / Short / Long)
    private fun setupTabButtons() {
        btnFocus.setOnClickListener { if (!isRunning) switchMode(TimerMode.FOCUS) }
        btnShortBreak.setOnClickListener { if (!isRunning) switchMode(TimerMode.SHORT_BREAK) }
        btnLongBreak.setOnClickListener { if (!isRunning) switchMode(TimerMode.LONG_BREAK) }

        updateTabSelection()
    }

    private fun switchMode(mode: TimerMode) {
        currentMode = mode
        timeInMinutes = mode.minutes
        tvMode.text = mode.displayName
        updateTimerDisplay()
        updateTabSelection()
    }

    private fun updateTabSelection() {
        val grayColor = ContextCompat.getColor(this, R.color.gray_text)
        val selectedColor = ContextCompat.getColor(this, android.R.color.white)

        listOf(btnFocus, btnShortBreak, btnLongBreak).forEach {
            it.setBackgroundResource(android.R.color.transparent)
            it.setTextColor(grayColor)
        }

        when (currentMode) {
            TimerMode.FOCUS -> {
                btnFocus.setBackgroundResource(R.drawable.tab_selected)
                btnFocus.setTextColor(selectedColor)
            }
            TimerMode.SHORT_BREAK -> {
                btnShortBreak.setBackgroundResource(R.drawable.tab_selected)
                btnShortBreak.setTextColor(selectedColor)
            }
            TimerMode.LONG_BREAK -> {
                btnLongBreak.setBackgroundResource(R.drawable.tab_selected)
                btnLongBreak.setTextColor(selectedColor)
            }
        }
    }

    private fun updateTimerDisplay() {
        if (remainingMillis > 0) {
            val minutes = (remainingMillis / 1000 / 60).toInt()
            val seconds = ((remainingMillis / 1000) % 60).toInt()
            tvTimer.text = String.format("%02d:%02d", minutes, seconds)
        } else {
            tvTimer.text = String.format("%02d:00", timeInMinutes)
        }
        tvMode.text = currentMode.displayName
    }

    private fun startTimer() {
        val totalMillis = if (remainingMillis > 0) remainingMillis else timeInMinutes * 60 * 1000L
        btnStart.text = "Detener"
        isRunning = true
        disableTabs()

        timer = object : CountDownTimer(totalMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                remainingMillis = millisUntilFinished
                val minutesLeft = (millisUntilFinished / 1000) / 60
                val secondsLeft = (millisUntilFinished / 1000) % 60
                tvTimer.text = String.format("%02d:%02d", minutesLeft, secondsLeft)
            }

            override fun onFinish() {
                tvTimer.text = "00:00"
                Toast.makeText(applicationContext, "¡Tiempo terminado! 🎉", Toast.LENGTH_SHORT).show()
                resetTimer()
            }
        }.start()
    }

    private fun stopTimer() {
        timer?.cancel()
        btnStart.text = "Continuar"
        isRunning = false
    }

    private fun resetTimer() {
        timer?.cancel()
        remainingMillis = 0L
        btnStart.text = "Comenzar"
        isRunning = false
        enableTabs()
        updateTimerDisplay()
    }

    private fun disableTabs() {
        btnFocus.isEnabled = false
        btnShortBreak.isEnabled = false
        btnLongBreak.isEnabled = false
    }

    private fun enableTabs() {
        btnFocus.isEnabled = true
        btnShortBreak.isEnabled = true
        btnLongBreak.isEnabled = true
    }

    override fun onDestroy() {
        super.onDestroy()
        timer?.cancel()
    }
}
