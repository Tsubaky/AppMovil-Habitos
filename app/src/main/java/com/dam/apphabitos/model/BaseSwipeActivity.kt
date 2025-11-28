package com.dam.apphabitos

import android.content.Intent
import android.os.Bundle
import android.view.GestureDetector
import android.view.MotionEvent
import androidx.appcompat.app.AppCompatActivity
import kotlin.math.abs

abstract class BaseSwipeActivity : AppCompatActivity() {

    private lateinit var gestureDetector: GestureDetector
    private var username: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        username = intent.getStringExtra("username") ?: "Usuario"
        setupSwipeGesture()
    }

    private fun setupSwipeGesture() {
        gestureDetector = GestureDetector(this, object : GestureDetector.SimpleOnGestureListener() {
            private val SWIPE_THRESHOLD = 100
            private val SWIPE_VELOCITY_THRESHOLD = 100

            override fun onFling(
                e1: MotionEvent?,
                e2: MotionEvent,
                velocityX: Float,
                velocityY: Float
            ): Boolean {
                if (e1 == null) return false

                val diffX = e2.x - e1.x
                val diffY = e2.y - e1.y

                if (abs(diffX) > abs(diffY)) {
                    if (abs(diffX) > SWIPE_THRESHOLD && abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                        if (diffX > 0) {
                            onSwipeRight()
                        } else {
                            onSwipeLeft()
                        }
                        return true
                    }
                }
                return false
            }
        })
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        gestureDetector.onTouchEvent(ev)
        return super.dispatchTouchEvent(ev)
    }

    private fun onSwipeRight() {
        // Orden: Home ← Calendar ← Stats ← Pomodoro
        when (this) {
            is CalendarActivity -> navigateTo(HomeActivity::class.java)
            is StatisticsActivity -> navigateTo(CalendarActivity::class.java)
            is PomodoroActivity -> navigateTo(StatisticsActivity::class.java)
        }
    }

    private fun onSwipeLeft() {
        // Orden: Home → Calendar → Stats → Pomodoro
        when (this) {
            is HomeActivity -> navigateTo(CalendarActivity::class.java)
            is CalendarActivity -> navigateTo(StatisticsActivity::class.java)
            is StatisticsActivity -> navigateTo(PomodoroActivity::class.java)
        }
    }

    private fun navigateTo(activityClass: Class<*>) {
        val intent = Intent(this, activityClass).apply {
            putExtra("username", username)
            addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
        }
        startActivity(intent)
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
    }

    protected fun getUsername(): String = username
}