package com.dam.apphabitos

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.dam.apphabitos.model.Habit
import com.dam.apphabitos.model.ExerciseSession

class DBHelper(context: Context) : SQLiteOpenHelper(context, DB_NAME, null, DB_VERSION) {
    companion object {
        const val DB_NAME = "habits.db"
        const val DB_VERSION = 5

        // Tabla habits
        const val TABLE_HABITS = "habits"
        const val COL_ID = "id"
        const val COL_NAME = "name"
        const val COL_EMOJIS = "emojis"
        const val COL_COMPLETED = "completed"
        const val COL_CREATED = "created_at"
        const val COL_DATE = "date"
        const val COL_TIME = "time"
        const val COL_POMODORO_MINUTES = "pomodoro_minutes"
        const val COL_GPS_ENABLED = "gps_enabled"

        // Tabla exercise sessions
        const val TABLE_EXERCISE = "exercise_sessions"
        const val COL_EXERCISE_ID = "exercise_id"
        const val COL_HABIT_ID = "habit_id"
        const val COL_DISTANCE = "distance_km"
        const val COL_DURATION = "duration_seconds"
        const val COL_CALORIES = "calories"
        const val COL_AVG_SPEED = "avg_speed"
        const val COL_SESSION_DATE = "session_date"
        const val COL_ROUTE_POINTS = "route_points"

        // Tabla users
        const val TABLE_USERS = "users"
        const val COL_USER_NAME = "username"
        const val COL_PASSWORD = "password"
        const val COL_USER_ID = "user_id"
    }

    override fun onCreate(db: SQLiteDatabase) {
        // Tabla de hábitos
        val sqlHabits = """
            CREATE TABLE $TABLE_HABITS (
                $COL_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_NAME TEXT NOT NULL,
                $COL_EMOJIS TEXT,
                $COL_COMPLETED INTEGER DEFAULT 0,
                $COL_CREATED INTEGER,
                $COL_DATE TEXT NOT NULL,
                $COL_TIME TEXT,
                $COL_POMODORO_MINUTES INTEGER DEFAULT 0,
                $COL_GPS_ENABLED INTEGER DEFAULT 0
            );
        """.trimIndent()
        db.execSQL(sqlHabits)

        // Tabla de usuarios
        val sqlUsers = """
            CREATE TABLE $TABLE_USERS (
                $COL_USER_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_USER_NAME TEXT UNIQUE NOT NULL,
                $COL_PASSWORD TEXT NOT NULL
            );
        """.trimIndent()
        db.execSQL(sqlUsers)

        // Tabla de sesiones de ejercicio
        val sqlExercise = """
            CREATE TABLE $TABLE_EXERCISE (
                $COL_EXERCISE_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_HABIT_ID INTEGER,
                $COL_DISTANCE REAL DEFAULT 0,
                $COL_DURATION INTEGER DEFAULT 0,
                $COL_CALORIES INTEGER DEFAULT 0,
                $COL_AVG_SPEED REAL DEFAULT 0,
                $COL_SESSION_DATE TEXT,
                $COL_ROUTE_POINTS TEXT,
                FOREIGN KEY($COL_HABIT_ID) REFERENCES $TABLE_HABITS($COL_ID)
            );
        """.trimIndent()
        db.execSQL(sqlExercise)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 5) {
            // Agregar columna GPS a tabla existente
            try {
                db.execSQL("ALTER TABLE $TABLE_HABITS ADD COLUMN $COL_GPS_ENABLED INTEGER DEFAULT 0")
            } catch (e: Exception) {
                // La columna ya existe
            }

            // Crear tabla de ejercicios
            val sqlExercise = """
                CREATE TABLE IF NOT EXISTS $TABLE_EXERCISE (
                    $COL_EXERCISE_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                    $COL_HABIT_ID INTEGER,
                    $COL_DISTANCE REAL DEFAULT 0,
                    $COL_DURATION INTEGER DEFAULT 0,
                    $COL_CALORIES INTEGER DEFAULT 0,
                    $COL_AVG_SPEED REAL DEFAULT 0,
                    $COL_SESSION_DATE TEXT,
                    $COL_ROUTE_POINTS TEXT,
                    FOREIGN KEY($COL_HABIT_ID) REFERENCES $TABLE_HABITS($COL_ID)
                );
            """.trimIndent()
            db.execSQL(sqlExercise)
        }
    }

    fun insertHabit(habit: Habit): Long {
        val db = writableDatabase
        val cv = ContentValues().apply {
            put(COL_NAME, habit.name)
            put(COL_EMOJIS, habit.emojis)
            put(COL_COMPLETED, habit.completed)
            put(COL_CREATED, habit.createdAt)
            put(COL_DATE, habit.date)
            put(COL_TIME, habit.time)
            put(COL_POMODORO_MINUTES, habit.pomodoroMinutes)
            put(COL_GPS_ENABLED, if (habit.gpsEnabled) 1 else 0)
        }
        return db.insert(TABLE_HABITS, null, cv)
    }

    fun updateHabitCompleted(id: Long, completed: Int) {
        val db = writableDatabase
        val cv = ContentValues().apply { put(COL_COMPLETED, completed) }
        db.update(TABLE_HABITS, cv, "$COL_ID=?", arrayOf(id.toString()))
    }

    fun getAllHabits(): MutableList<Habit> {
        val list = mutableListOf<Habit>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_HABITS ORDER BY $COL_CREATED DESC", null)
        cursor.use {
            while (cursor.moveToNext()) {
                val h = Habit(
                    id = cursor.getLong(cursor.getColumnIndexOrThrow(COL_ID)),
                    name = cursor.getString(cursor.getColumnIndexOrThrow(COL_NAME)),
                    emojis = cursor.getString(cursor.getColumnIndexOrThrow(COL_EMOJIS)) ?: "",
                    completed = cursor.getInt(cursor.getColumnIndexOrThrow(COL_COMPLETED)),
                    createdAt = cursor.getLong(cursor.getColumnIndexOrThrow(COL_CREATED)),
                    date = cursor.getString(cursor.getColumnIndexOrThrow(COL_DATE)) ?: "",
                    time = cursor.getString(cursor.getColumnIndexOrThrow(COL_TIME)) ?: "",
                    pomodoroMinutes = cursor.getInt(cursor.getColumnIndexOrThrow(COL_POMODORO_MINUTES)),
                    gpsEnabled = cursor.getInt(cursor.getColumnIndexOrThrow(COL_GPS_ENABLED)) == 1
                )
                list.add(h)
            }
        }
        return list
    }

    fun getCompletedHabits(): MutableList<Habit> {
        val list = mutableListOf<Habit>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_HABITS WHERE $COL_COMPLETED = 1 ORDER BY $COL_CREATED DESC", null)
        cursor.use {
            while (cursor.moveToNext()) {
                val h = Habit(
                    id = cursor.getLong(cursor.getColumnIndexOrThrow(COL_ID)),
                    name = cursor.getString(cursor.getColumnIndexOrThrow(COL_NAME)),
                    emojis = cursor.getString(cursor.getColumnIndexOrThrow(COL_EMOJIS)) ?: "",
                    completed = cursor.getInt(cursor.getColumnIndexOrThrow(COL_COMPLETED)),
                    createdAt = cursor.getLong(cursor.getColumnIndexOrThrow(COL_CREATED)),
                    date = cursor.getString(cursor.getColumnIndexOrThrow(COL_DATE)) ?: "",
                    time = cursor.getString(cursor.getColumnIndexOrThrow(COL_TIME)) ?: "",
                    pomodoroMinutes = cursor.getInt(cursor.getColumnIndexOrThrow(COL_POMODORO_MINUTES)),
                    gpsEnabled = cursor.getInt(cursor.getColumnIndexOrThrow(COL_GPS_ENABLED)) == 1
                )
                list.add(h)
            }
        }
        return list
    }

    fun registerUser(username: String, password: String): Long {
        val db = writableDatabase
        val cv = ContentValues().apply {
            put(COL_USER_NAME, username)
            put(COL_PASSWORD, password)
        }
        return db.insert(TABLE_USERS, null, cv)
    }

    fun authenticateUser(username: String, password: String): Boolean {
        val db = readableDatabase
        var isAuthenticated = false
        val cursor = db.rawQuery("SELECT * FROM $TABLE_USERS WHERE $COL_USER_NAME = ? AND $COL_PASSWORD = ?", arrayOf(username, password))
        cursor.use {
            isAuthenticated = cursor.moveToFirst()
        }
        return isAuthenticated
    }

    fun getHabitsByDate(date: String): MutableList<Habit> {
        val list = mutableListOf<Habit>()
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT * FROM $TABLE_HABITS WHERE $COL_DATE = ? ORDER BY $COL_TIME ASC",
            arrayOf(date)
        )
        cursor.use {
            while (cursor.moveToNext()) {
                val h = Habit(
                    id = cursor.getLong(cursor.getColumnIndexOrThrow(COL_ID)),
                    name = cursor.getString(cursor.getColumnIndexOrThrow(COL_NAME)),
                    emojis = cursor.getString(cursor.getColumnIndexOrThrow(COL_EMOJIS)) ?: "",
                    completed = cursor.getInt(cursor.getColumnIndexOrThrow(COL_COMPLETED)),
                    createdAt = cursor.getLong(cursor.getColumnIndexOrThrow(COL_CREATED)),
                    date = cursor.getString(cursor.getColumnIndexOrThrow(COL_DATE)) ?: "",
                    time = cursor.getString(cursor.getColumnIndexOrThrow(COL_TIME)) ?: "",
                    pomodoroMinutes = cursor.getInt(cursor.getColumnIndexOrThrow(COL_POMODORO_MINUTES)),
                    gpsEnabled = cursor.getInt(cursor.getColumnIndexOrThrow(COL_GPS_ENABLED)) == 1
                )
                list.add(h)
            }
        }
        return list
    }

    fun getHabitsWithScheduledDate(): MutableList<Habit> {
        val list = mutableListOf<Habit>()
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT * FROM $TABLE_HABITS WHERE $COL_DATE != '' ORDER BY $COL_DATE ASC, $COL_TIME ASC",
            null
        )
        cursor.use {
            while (cursor.moveToNext()) {
                val h = Habit(
                    id = cursor.getLong(cursor.getColumnIndexOrThrow(COL_ID)),
                    name = cursor.getString(cursor.getColumnIndexOrThrow(COL_NAME)),
                    emojis = cursor.getString(cursor.getColumnIndexOrThrow(COL_EMOJIS)) ?: "",
                    completed = cursor.getInt(cursor.getColumnIndexOrThrow(COL_COMPLETED)),
                    createdAt = cursor.getLong(cursor.getColumnIndexOrThrow(COL_CREATED)),
                    date = cursor.getString(cursor.getColumnIndexOrThrow(COL_DATE)) ?: "",
                    time = cursor.getString(cursor.getColumnIndexOrThrow(COL_TIME)) ?: "",
                    pomodoroMinutes = cursor.getInt(cursor.getColumnIndexOrThrow(COL_POMODORO_MINUTES)),
                    gpsEnabled = cursor.getInt(cursor.getColumnIndexOrThrow(COL_GPS_ENABLED)) == 1
                )
                list.add(h)
            }
        }
        return list
    }

    fun getTodayHabits(): MutableList<Habit> {
        val list = mutableListOf<Habit>()
        val db = readableDatabase

        val today = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
            .format(java.util.Date())

        val cursor = db.rawQuery(
            "SELECT * FROM $TABLE_HABITS WHERE ($COL_DATE = '' OR $COL_DATE = ?) AND $COL_COMPLETED = 0 ORDER BY $COL_CREATED DESC",
            arrayOf(today)
        )

        cursor.use {
            while (cursor.moveToNext()) {
                val h = Habit(
                    id = cursor.getLong(cursor.getColumnIndexOrThrow(COL_ID)),
                    name = cursor.getString(cursor.getColumnIndexOrThrow(COL_NAME)),
                    emojis = cursor.getString(cursor.getColumnIndexOrThrow(COL_EMOJIS)) ?: "",
                    completed = cursor.getInt(cursor.getColumnIndexOrThrow(COL_COMPLETED)),
                    createdAt = cursor.getLong(cursor.getColumnIndexOrThrow(COL_CREATED)),
                    date = cursor.getString(cursor.getColumnIndexOrThrow(COL_DATE)) ?: "",
                    time = cursor.getString(cursor.getColumnIndexOrThrow(COL_TIME)) ?: "",
                    pomodoroMinutes = cursor.getInt(cursor.getColumnIndexOrThrow(COL_POMODORO_MINUTES)),
                    gpsEnabled = cursor.getInt(cursor.getColumnIndexOrThrow(COL_GPS_ENABLED)) == 1
                )
                list.add(h)
            }
        }
        return list
    }

    fun getCompletedLast7Days(): List<Int> {
        val db = readableDatabase
        val result = IntArray(7) { 0 }

        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
        val calendar = java.util.Calendar.getInstance()

        for (i in 0 until 7) {
            val day = sdf.format(calendar.time)

            val cursor = db.rawQuery(
                "SELECT COUNT(*) FROM $TABLE_HABITS WHERE $COL_DATE = ? AND $COL_COMPLETED = 1",
                arrayOf(day)
            )
            if (cursor.moveToFirst()) result[6 - i] = cursor.getInt(0)
            cursor.close()

            calendar.add(java.util.Calendar.DAY_OF_YEAR, -1)
        }

        return result.toList()
    }

    fun getTotalCompleted(): Int {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT COUNT(*) FROM $TABLE_HABITS WHERE $COL_COMPLETED = 1",
            null
        )
        cursor.moveToFirst()
        val count = cursor.getInt(0)
        cursor.close()
        return count
    }

    fun getTotalPending(): Int {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT COUNT(*) FROM $TABLE_HABITS WHERE $COL_COMPLETED = 0",
            null
        )
        cursor.moveToFirst()
        val count = cursor.getInt(0)
        cursor.close()
        return count
    }

    fun getCompletionRateLast7Days(): Int {
        val db = readableDatabase

        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
        val calendar = java.util.Calendar.getInstance()

        calendar.add(java.util.Calendar.DAY_OF_YEAR, -6)
        val startDate = sdf.format(calendar.time)

        calendar.add(java.util.Calendar.DAY_OF_YEAR, 6)
        val endDate = sdf.format(calendar.time)

        val cursorTotal = db.rawQuery(
            "SELECT COUNT(*) FROM $TABLE_HABITS WHERE $COL_DATE BETWEEN ? AND ?",
            arrayOf(startDate, endDate)
        )
        val total = if (cursorTotal.moveToFirst()) cursorTotal.getInt(0) else 0
        cursorTotal.close()

        val cursorCompleted = db.rawQuery(
            "SELECT COUNT(*) FROM $TABLE_HABITS WHERE $COL_DATE BETWEEN ? AND ? AND $COL_COMPLETED = 1",
            arrayOf(startDate, endDate)
        )
        val completed = if (cursorCompleted.moveToFirst()) cursorCompleted.getInt(0) else 0
        cursorCompleted.close()

        return if (total == 0) 0 else ((completed.toFloat() / total.toFloat()) * 100).toInt()
    }

    fun getDaysWithCompletedHabits(): List<String> {
        val db = readableDatabase
        val days = mutableListOf<String>()

        val cursor = db.rawQuery(
            "SELECT DISTINCT $COL_DATE FROM $TABLE_HABITS WHERE $COL_COMPLETED = 1 AND $COL_DATE != '' AND $COL_DATE IS NOT NULL ORDER BY $COL_DATE ASC",
            null
        )

        while (cursor.moveToNext()) {
            val date = cursor.getString(0)
            if (!date.isNullOrEmpty()) {
                days.add(date)
            }
        }
        cursor.close()

        return days
    }

    fun getCurrentStreak(): Int {
        return try {
            val days = getDaysWithCompletedHabits()
            if (days.isEmpty()) return 0

            val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
            val calendar = java.util.Calendar.getInstance()
            val today = sdf.format(calendar.time)

            if (!days.contains(today)) return 0

            var streak = 0
            calendar.time = sdf.parse(today)!!

            while (true) {
                val checkDate = sdf.format(calendar.time)
                if (days.contains(checkDate)) {
                    streak++
                    calendar.add(java.util.Calendar.DAY_OF_YEAR, -1)
                } else {
                    break
                }
            }

            streak
        } catch (e: Exception) {
            e.printStackTrace()
            0
        }
    }

    fun getLongestStreak(): Int {
        return try {
            val days = getDaysWithCompletedHabits()
            if (days.isEmpty()) return 0

            val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
            var maxStreak = 1
            var currentStreak = 1

            for (i in 1 until days.size) {
                val prevDate = sdf.parse(days[i - 1])
                val currDate = sdf.parse(days[i])

                if (prevDate == null || currDate == null) continue

                val calendar = java.util.Calendar.getInstance()
                calendar.time = prevDate
                calendar.add(java.util.Calendar.DAY_OF_YEAR, 1)

                if (sdf.format(calendar.time) == days[i]) {
                    currentStreak++
                    maxStreak = maxOf(maxStreak, currentStreak)
                } else {
                    currentStreak = 1
                }
            }

            maxStreak
        } catch (e: Exception) {
            e.printStackTrace()
            0
        }
    }

    // ========== FUNCIONES PARA EXERCISE SESSIONS ==========

    fun insertExerciseSession(session: ExerciseSession): Long {
        val db = writableDatabase
        val cv = ContentValues().apply {
            put(COL_HABIT_ID, session.habitId)
            put(COL_DISTANCE, session.distanceKm)
            put(COL_DURATION, session.durationSeconds)
            put(COL_CALORIES, session.calories)
            put(COL_AVG_SPEED, session.avgSpeed)
            put(COL_SESSION_DATE, session.sessionDate)
            put(COL_ROUTE_POINTS, session.routePoints)
        }
        return db.insert(TABLE_EXERCISE, null, cv)
    }

    fun getAllExerciseSessions(): List<ExerciseSession> {
        val list = mutableListOf<ExerciseSession>()
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT * FROM $TABLE_EXERCISE ORDER BY $COL_SESSION_DATE DESC",
            null
        )
        cursor.use {
            while (cursor.moveToNext()) {
                val session = ExerciseSession(
                    id = cursor.getLong(cursor.getColumnIndexOrThrow(COL_EXERCISE_ID)),
                    habitId = cursor.getLong(cursor.getColumnIndexOrThrow(COL_HABIT_ID)),
                    distanceKm = cursor.getDouble(cursor.getColumnIndexOrThrow(COL_DISTANCE)),
                    durationSeconds = cursor.getInt(cursor.getColumnIndexOrThrow(COL_DURATION)),
                    calories = cursor.getInt(cursor.getColumnIndexOrThrow(COL_CALORIES)),
                    avgSpeed = cursor.getDouble(cursor.getColumnIndexOrThrow(COL_AVG_SPEED)),
                    sessionDate = cursor.getString(cursor.getColumnIndexOrThrow(COL_SESSION_DATE)),
                    routePoints = cursor.getString(cursor.getColumnIndexOrThrow(COL_ROUTE_POINTS)) ?: ""
                )
                list.add(session)
            }
        }
        return list
    }

    fun getTotalDistance(): Double {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT SUM($COL_DISTANCE) FROM $TABLE_EXERCISE", null)
        var total = 0.0
        if (cursor.moveToFirst()) {
            total = cursor.getDouble(0)
        }
        cursor.close()
        return total
    }

    fun getTotalExerciseDuration(): Int {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT SUM($COL_DURATION) FROM $TABLE_EXERCISE", null)
        var total = 0
        if (cursor.moveToFirst()) {
            total = cursor.getInt(0)
        }
        cursor.close()
        return total
    }

    fun getTotalCalories(): Int {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT SUM($COL_CALORIES) FROM $TABLE_EXERCISE", null)
        var total = 0
        if (cursor.moveToFirst()) {
            total = cursor.getInt(0)
        }
        cursor.close()
        return total
    }

    fun getExerciseSessionsLast7Days(): List<Double> {
        val db = readableDatabase
        val result = DoubleArray(7) { 0.0 }

        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
        val calendar = java.util.Calendar.getInstance()

        for (i in 0 until 7) {
            val day = sdf.format(calendar.time)

            val cursor = db.rawQuery(
                "SELECT SUM($COL_DISTANCE) FROM $TABLE_EXERCISE WHERE $COL_SESSION_DATE = ?",
                arrayOf(day)
            )
            if (cursor.moveToFirst()) {
                result[6 - i] = cursor.getDouble(0)
            }
            cursor.close()

            calendar.add(java.util.Calendar.DAY_OF_YEAR, -1)
        }

        return result.toList()
    }
}