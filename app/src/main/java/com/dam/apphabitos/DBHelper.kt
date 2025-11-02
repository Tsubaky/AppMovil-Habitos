package com.dam.apphabitos

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.dam.apphabitos.model.Habit

class DBHelper(context: Context) : SQLiteOpenHelper(context, DB_NAME, null, DB_VERSION) {
    companion object {
        const val DB_NAME = "habits.db"
        const val DB_VERSION = 2
        const val TABLE_HABITS = "habits"
        const val COL_ID = "id"
        const val COL_NAME = "name"
        const val COL_EMOJIS = "emojis"
        const val COL_COMPLETED = "completed"
        const val COL_CREATED = "created_at"

        //CONSTANTES PARA USUARIO

        const val TABLE_USERS = "users"
        const val COL_USER_NAME = "username"
        const val COL_PASSWORD = "password"
        const val COL_USER_ID = "user_id"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val sqlHabits = """
            CREATE TABLE $TABLE_HABITS (
                $COL_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_NAME TEXT NOT NULL,
                $COL_EMOJIS TEXT,
                $COL_COMPLETED INTEGER DEFAULT 0,
                $COL_CREATED INTEGER
            );
        """.trimIndent()
        db.execSQL(sqlHabits)

        val sqlUsers = """
            CREATE TABLE $TABLE_USERS (
                $COL_USER_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_USER_NAME TEXT UNIQUE NOT NULL,
                $COL_PASSWORD TEXT NOT NULL
            );
        """.trimIndent()
        db.execSQL(sqlUsers)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_HABITS")
        onCreate(db)
    }

    fun insertHabit(habit: Habit): Long {
        val db = writableDatabase
        val cv = ContentValues().apply {
            put(COL_NAME, habit.name)
            put(COL_EMOJIS, habit.emojis)
            put(COL_COMPLETED, habit.completed)
            put(COL_CREATED, habit.createdAt)
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
                    createdAt = cursor.getLong(cursor.getColumnIndexOrThrow(COL_CREATED))
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
        //INTENTA INSERTAR UN NUEVO USUARIO
        return db.insert(TABLE_USERS, null, cv)
    }

    fun authenticateUser(username: String, password: String): Boolean {
        val db = readableDatabase
        var isAuthenticated = false

        //BUSCA UN REGISTRO DONDE EL NOMBRE DE USUARIO Y LA CONTRASEÑA COINCIDAN
        val cursor = db.rawQuery("SELECT * FROM $TABLE_USERS WHERE $COL_USER_NAME = ? AND $COL_PASSWORD = ?", arrayOf(username, password))

        cursor.use {
            //SI moveToFirst() es true, significa que encontró un usuario
            isAuthenticated = cursor.moveToFirst()
        }
        return isAuthenticated
    }


}
