package com.dam.apphabitos

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.os.SystemClock
import android.widget.Button
import android.widget.Chronometer
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import com.dam.apphabitos.model.ExerciseSession
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.round

class ExerciseTrackingActivity : AppCompatActivity() {

    private lateinit var chronometer: Chronometer
    private lateinit var tvDistance: TextView
    private lateinit var tvSpeed: TextView
    private lateinit var tvCalories: TextView
    private lateinit var btnPause: Button
    private lateinit var btnStop: Button

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback

    private var isTracking = false
    private var isPaused = false
    private var totalDistance = 0.0 // en metros
    private var lastLocation: Location? = null
    private val routePoints = mutableListOf<Pair<Double, Double>>()

    private var startTime = 0L
    private var pauseOffset = 0L

    private var habitId: Long = 0
    private var habitName: String = ""

    companion object {
        private const val LOCATION_PERMISSION_REQUEST = 100
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_exercise_tracking)

        // Obtener datos del intent
        habitId = intent.getLongExtra("habitId", 0)
        habitName = intent.getStringExtra("habitName") ?: "Ejercicio"

        // Inicializar vistas
        chronometer = findViewById(R.id.chronometer)
        tvDistance = findViewById(R.id.tvDistance)
        tvSpeed = findViewById(R.id.tvSpeed)
        tvCalories = findViewById(R.id.tvCalories)
        btnPause = findViewById(R.id.btnPause)
        btnStop = findViewById(R.id.btnStop)

        val tvTitle = findViewById<TextView>(R.id.tvTrackingTitle)
        tvTitle.text = habitName

        // Inicializar location client
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Configurar botones
        btnPause.setOnClickListener {
            if (isPaused) {
                resumeTracking()
            } else {
                pauseTracking()
            }
        }

        btnStop.setOnClickListener {
            stopTracking()
        }

        // Solicitar permisos y empezar
        checkLocationPermission()
    }

    private fun checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                LOCATION_PERMISSION_REQUEST
            )
        } else {
            startTracking()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startTracking()
            } else {
                Toast.makeText(this, "Permiso de ubicación necesario", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun startTracking() {
        isTracking = true
        isPaused = false

        // Iniciar cronómetro
        startTime = SystemClock.elapsedRealtime()
        chronometer.base = SystemClock.elapsedRealtime()
        chronometer.start()

        // Configurar callback de ubicación
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                if (!isTracking || isPaused) return

                for (location in locationResult.locations) {
                    updateLocation(location)
                }
            }
        }

        // Solicitar actualizaciones de ubicación
        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            3000 // cada 3 segundos
        ).apply {
            setMinUpdateIntervalMillis(1000)
            setMaxUpdateDelayMillis(5000)
        }.build()

        try {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                fusedLocationClient.requestLocationUpdates(
                    locationRequest,
                    locationCallback,
                    null
                )
            }
        } catch (e: SecurityException) {
            e.printStackTrace()
        }

        Toast.makeText(this, "Tracking iniciado", Toast.LENGTH_SHORT).show()
    }

    private fun updateLocation(location: Location) {
        // Guardar punto de ruta
        routePoints.add(Pair(location.latitude, location.longitude))

        // Calcular distancia
        lastLocation?.let { last ->
            val distance = last.distanceTo(location)

            // Solo sumar si el movimiento es significativo (más de 5 metros)
            if (distance > 5 && distance < 100) {
                totalDistance += distance
                updateUI()
            }
        }

        lastLocation = location
    }

    private fun updateUI() {
        // Distancia en km
        val distanceKm = totalDistance / 1000.0
        tvDistance.text = String.format("%.2f km", distanceKm)

        // Velocidad promedio en km/h
        val elapsedSeconds = (SystemClock.elapsedRealtime() - chronometer.base) / 1000.0
        val speedKmh = if (elapsedSeconds > 0) {
            (distanceKm / elapsedSeconds) * 3600
        } else {
            0.0
        }
        tvSpeed.text = String.format("%.1f km/h", speedKmh)

        // Calorías estimadas (aproximación: 60 kcal por km)
        val calories = (distanceKm * 60).toInt()
        tvCalories.text = "$calories kcal"
    }

    private fun pauseTracking() {
        isPaused = true
        pauseOffset = SystemClock.elapsedRealtime() - chronometer.base
        chronometer.stop()
        btnPause.text = "▶ REANUDAR"
        Toast.makeText(this, "Pausado", Toast.LENGTH_SHORT).show()
    }

    private fun resumeTracking() {
        isPaused = false
        chronometer.base = SystemClock.elapsedRealtime() - pauseOffset
        chronometer.start()
        btnPause.text = "⏸ PAUSAR"
        Toast.makeText(this, "Reanudado", Toast.LENGTH_SHORT).show()
    }

    private fun stopTracking() {
        isTracking = false
        chronometer.stop()
        fusedLocationClient.removeLocationUpdates(locationCallback)

        // Guardar sesión
        saveExerciseSession()
    }

    private fun saveExerciseSession() {
        val elapsedSeconds = ((SystemClock.elapsedRealtime() - chronometer.base) / 1000).toInt()
        val distanceKm = totalDistance / 1000.0
        val avgSpeed = if (elapsedSeconds > 0) {
            (distanceKm / elapsedSeconds) * 3600
        } else {
            0.0
        }
        val calories = (distanceKm * 60).toInt()

        // Convertir ruta a JSON simple
        val routeJson = routePoints.joinToString(";") { "${it.first},${it.second}" }

        val session = ExerciseSession(
            habitId = habitId,
            distanceKm = round(distanceKm * 100) / 100,
            durationSeconds = elapsedSeconds,
            calories = calories,
            avgSpeed = round(avgSpeed * 10) / 10,
            sessionDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()),
            routePoints = routeJson
        )

        val db = DBHelper(this)
        db.insertExerciseSession(session)

        // Marcar hábito como completado
        db.updateHabitCompleted(habitId, 1)

        Toast.makeText(this, "¡Sesión guardada!", Toast.LENGTH_LONG).show()
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isTracking) {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }
    }
}