package com.dam.apphabitos.model


import androidx.room.*
import androidx.room.Dao

@Dao
interface HabitDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(habit: Habit): Long

    @Update
    fun update(habit: Habit)

    @Delete
    fun delete(habit: Habit)

    @Query("SELECT * FROM habits ORDER BY id DESC")
    fun getAll(): List<Habit>

    @Query("UPDATE habits SET completed = :completed WHERE id = :id")
    fun updateCompleted(id: Int, completed: Int)
}
