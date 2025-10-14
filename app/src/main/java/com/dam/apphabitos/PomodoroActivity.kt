package com.dam.apphabitos

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.google.android.material.bottomnavigation.BottomNavigationView

class PomodoroActivity : AppCompatActivity() {

    //  Declaración de vistas (componentes del layout)
    private lateinit var tvTimer: TextView
    private lateinit var tvMode: TextView
    private lateinit var btnStart: Button
    private lateinit var btnReset: Button
    private lateinit var btnFocus: Button
    private lateinit var btnShortBreak: Button
    private lateinit var btnLongBreak: Button
    private lateinit var btnBack: ImageView

    //  Variables del temporizador
    private var timer: CountDownTimer? = null     // Objeto que maneja la cuenta regresiva
    private var isRunning = false                 // Indica si el temporizador está activo
    private var currentMode = TimerMode.FOCUS     // Modo actual (por defecto: enfocado)
    private var timeInMinutes = 45                // Minutos configurados
    private var remainingMillis: Long = 0L        // Milisegundos restantes

    //  Modos del temporizador (enfocado, descanso corto, descanso largo)
    enum class TimerMode(val displayName: String, val minutes: Int) {
        FOCUS("Enfocado", 45),
        SHORT_BREAK("Descanso corto", 5),
        LONG_BREAK("Descanso largo", 15)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pomodoro)

        // --- Configurar barra de navegación inferior ---
        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        bottomNavigation.selectedItemId = R.id.nav_timer // Marca el ícono de temporizador

        // Acciones al seleccionar un ítem del menú inferior
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    // Volver al Home
                    val intent = Intent(this, HomeActivity::class.java)
                    intent.putExtra("fromBack", true)
                    // Evita duplicar actividades en el stack
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                    startActivity(intent)
                    overridePendingTransition(0, 0)
                    finish()
                    true
                }
                R.id.nav_timer -> true // Ya estamos en esta pantalla
                else -> false
            }
        }

        // --- Inicializar vistas y comportamiento ---
        initViews()
        setupTabButtons()
        updateTimerDisplay()

        // --- Manejo del botón físico "Atrás" ---
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val intent = Intent(this@PomodoroActivity, HomeActivity::class.java)
                intent.putExtra("fromBack", true)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                startActivity(intent)
                // Animación suave al volver
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                finish()
            }
        })
    }

    //  Inicializar las vistas y sus eventos
    private fun initViews() {
        tvTimer = findViewById(R.id.tvTimer)
        tvMode = findViewById(R.id.tvMode)
        btnStart = findViewById(R.id.btnStart)
        btnReset = findViewById(R.id.btnReset)
        btnFocus = findViewById(R.id.btnFocus)
        btnShortBreak = findViewById(R.id.btnShortBreak)
        btnLongBreak = findViewById(R.id.btnLongBreak)
        btnBack = findViewById(R.id.btnBack)

        // Botón de retroceso (flecha)
        btnBack.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            intent.putExtra("fromBack", true)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            startActivity(intent)
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            finish()
        }

        // Botón de iniciar/detener
        btnStart.setOnClickListener {
            if (!isRunning) startTimer() else stopTimer()
        }

        // Botón de reiniciar
        btnReset.setOnClickListener {
            resetTimer()
        }

        // Permitir que el usuario cambie los minutos tocando el número
        tvTimer.setOnClickListener {
            if (!isRunning)
                showMinuteInputDialog()
            else
                Toast.makeText(this, "No puedes cambiar el tiempo mientras corre el temporizador", Toast.LENGTH_SHORT).show()
        }
    }

    //  Muestra un diálogo para cambiar la duración del temporizador
    private fun showMinuteInputDialog() {
        val editText = android.widget.EditText(this)
        editText.hint = "Minutos"
        editText.inputType = android.text.InputType.TYPE_CLASS_NUMBER
        editText.setText(timeInMinutes.toString())

        AlertDialog.Builder(this)
            .setTitle("Cambiar duración")
            .setMessage("Ingresa la cantidad de minutos:")
            .setView(editText)
            .setPositiveButton("Aceptar") { _, _ ->
                val newMinutes = editText.text.toString().toIntOrNull()
                if (newMinutes != null && newMinutes > 0) {
                    timeInMinutes = newMinutes
                    updateTimerDisplay()
                    Toast.makeText(this, "Duración cambiada a $newMinutes min", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Ingresa un número válido", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    //  Configurar los botones de modo (enfocado / descansos)
    private fun setupTabButtons() {
        btnFocus.setOnClickListener { if (!isRunning) switchMode(TimerMode.FOCUS) }
        btnShortBreak.setOnClickListener { if (!isRunning) switchMode(TimerMode.SHORT_BREAK) }
        btnLongBreak.setOnClickListener { if (!isRunning) switchMode(TimerMode.LONG_BREAK) }

        updateTabSelection()
    }

    //  Cambia el modo y actualiza la interfaz
    private fun switchMode(mode: TimerMode) {
        currentMode = mode
        timeInMinutes = mode.minutes
        tvMode.text = mode.displayName
        updateTimerDisplay()
        updateTabSelection()
    }

    //  Cambia el color del botón seleccionado (modo activo)
    private fun updateTabSelection() {
        val grayColor = ContextCompat.getColor(this, R.color.gray_text)
        val selectedColor = ContextCompat.getColor(this, android.R.color.white)

        // Todos los botones a gris
        listOf(btnFocus, btnShortBreak, btnLongBreak).forEach {
            it.setBackgroundResource(android.R.color.transparent)
            it.setTextColor(grayColor)
        }

        // Botón activo a blanco y con fondo azul
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

    //  Muestra el tiempo en formato mm:ss
    private fun updateTimerDisplay() {
        val minutes = (remainingMillis / 1000 / 60).toInt()
        if (remainingMillis > 0) {
            val seconds = ((remainingMillis / 1000) % 60).toInt()
            tvTimer.text = String.format("%02d:%02d", minutes, seconds)
        } else {
            tvTimer.text = String.format("%02d:00", timeInMinutes)
        }
    }

    //  Inicia el temporizador
    private fun startTimer() {
        // Si hay tiempo restante, continúa desde ahí
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

    //  Detiene el temporizador sin reiniciarlo
    private fun stopTimer() {
        timer?.cancel()
        btnStart.text = "Continuar"
        isRunning = false
    }

    //  Reinicia el temporizador desde cero
    private fun resetTimer() {
        timer?.cancel()
        remainingMillis = 0L
        btnStart.text = "Comenzar"
        isRunning = false
        enableTabs()
        updateTimerDisplay()
    }

    //  Deshabilita los botones de modo mientras corre el temporizador
    private fun disableTabs() {
        btnFocus.isEnabled = false
        btnShortBreak.isEnabled = false
        btnLongBreak.isEnabled = false
    }

    //  Habilita los botones de modo nuevamente
    private fun enableTabs() {
        btnFocus.isEnabled = true
        btnShortBreak.isEnabled = true
        btnLongBreak.isEnabled = true
    }

    //  Cancela el temporizador al destruir la actividad (buena práctica)
    override fun onDestroy() {
        super.onDestroy()
        timer?.cancel()
    }
}

