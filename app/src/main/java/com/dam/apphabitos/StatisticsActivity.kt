package com.dam.apphabitos

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.PercentFormatter
import com.google.android.material.bottomnavigation.BottomNavigationView

class StatisticsActivity : BaseSwipeActivity() {
    private lateinit var bottomNav: BottomNavigationView
    private lateinit var barChart: BarChart
    private lateinit var pieChart: PieChart

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_statistics)

        val username = getUsername()
        bottomNav = findViewById(R.id.bottomNavigation)
        bottomNav.selectedItemId = R.id.nav_stats

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

                R.id.nav_stats -> true
                else -> false
            }
        }

        //Inicializar las gráficas
        barChart = findViewById(R.id.barChart)
        pieChart = findViewById(R.id.pieChart)

        setupBarChart()
        setupPieChart()
        updateTotalCompleted()
        updateCompletionRate()
        updateStreaks()
    }

    private fun updateTotalCompleted() {
        val tvTotalCompleted = findViewById<TextView>(R.id.tvTotalCompleted)
        val db = DBHelper(this)
        val totalCompleted = db.getTotalCompleted()
        tvTotalCompleted.text = totalCompleted.toString()
    }

    private fun updateCompletionRate() {
        val tvCompletionRate = findViewById<TextView>(R.id.tvCompletionRate)
        val tvCompletionPeriod = findViewById<TextView>(R.id.tvCompletionPeriod)

        val db = DBHelper(this)
        val rate = db.getCompletionRateLast7Days()

        tvCompletionRate.text = "$rate%"
        tvCompletionPeriod.text = "Últimos 7 días"
    }

    private fun updateStreaks() {
        val tvCurrentStreak = findViewById<TextView>(R.id.tvCurrentStreak)
        val tvLongestStreak = findViewById<TextView>(R.id.tvLongestStreak)

        val db = DBHelper(this)
        val currentStreak = db.getCurrentStreak()
        val longestStreak = db.getLongestStreak()

        tvCurrentStreak.text = "$currentStreak días"
        tvLongestStreak.text = "$longestStreak días"
    }

    private fun setupBarChart() {
        val db = DBHelper(this)
        val values = db.getCompletedLast7Days()  // ← DATOS REALES

        val entries = ArrayList<BarEntry>()
        values.forEachIndexed { index, value ->
            entries.add(BarEntry(index.toFloat(), value.toFloat()))
        }

        val barDataSet = BarDataSet(entries, "Hábitos completados")
        barDataSet.color = Color.parseColor("#3B82F6")
        barDataSet.valueTextColor = Color.WHITE
        barDataSet.valueTextSize = 12f

        val barData = BarData(barDataSet)
        barData.barWidth = 0.6f

        barChart.data = barData
        barChart.description.isEnabled = false
        barChart.legend.isEnabled = false
        barChart.setDrawValueAboveBar(true)
        barChart.setFitBars(true)
        barChart.animateY(800)

        barChart.setBackgroundColor(Color.parseColor("#1E293B"))

        val xAxis = barChart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(false)
        xAxis.textColor = Color.parseColor("#94A3B8")
        xAxis.granularity = 1f

        xAxis.valueFormatter = IndexAxisValueFormatter(getDayLabels())

        barChart.axisLeft.apply {
            textColor = Color.parseColor("#94A3B8")
            setDrawGridLines(true)
            gridColor = Color.parseColor("#334155")
            axisMinimum = 0f
        }

        barChart.axisRight.isEnabled = false
        barChart.invalidate()
    }
    private fun getDayLabels(): Array<String> {
        val calendar = java.util.Calendar.getInstance()
        val labels = Array(7) { "" }
        val dayNames = arrayOf("D", "L", "M", "M", "J", "V", "S")

        // Retrocede 6 días para empezar desde hace una semana
        calendar.add(java.util.Calendar.DAY_OF_YEAR, -6)

        for (i in 0..6) {
            val dayOfWeek = calendar.get(java.util.Calendar.DAY_OF_WEEK)
            // Calendar.DAY_OF_WEEK: 1=Domingo, 2=Lunes, ..., 7=Sábado
            labels[i] = dayNames[dayOfWeek - 1]
            calendar.add(java.util.Calendar.DAY_OF_YEAR, 1)
        }

        return labels
    }

    private fun setupPieChart() {
        val db = DBHelper(this)

        val completed = db.getTotalCompleted().toFloat()
        val pending = db.getTotalPending().toFloat()
        val total = completed + pending
        val percent = if (total == 0f) 0 else (completed / total * 100).toInt()

        val entries = ArrayList<PieEntry>()
        entries.add(PieEntry(completed, "Completados"))
        entries.add(PieEntry(pending, "Pendientes"))

        val pieDataSet = PieDataSet(entries, "")
        pieDataSet.colors = arrayListOf(
            Color.parseColor("#3B82F6"),
            Color.parseColor("#475569")
        )
        pieDataSet.valueTextColor = Color.WHITE
        pieDataSet.valueTextSize = 14f

        val pieData = PieData(pieDataSet)
        pieData.setValueFormatter(PercentFormatter(pieChart))

        pieChart.data = pieData
        pieChart.description.isEnabled = false
        pieChart.setHoleColor(Color.parseColor("#1E293B"))
        pieChart.holeRadius = 50f
        pieChart.setDrawCenterText(true)
        pieChart.centerText = "$percent%\nCompletados"
        pieChart.setCenterTextColor(Color.WHITE)
        pieChart.setCenterTextSize(18f)

        pieChart.animateY(800)

        pieChart.legend.textColor = Color.parseColor("#94A3B8")
        pieChart.legend.textSize = 12f

        pieChart.setUsePercentValues(true)
        pieChart.invalidate()
    }

    override fun onResume() {
        super.onResume()

        setupBarChart()
        setupPieChart()
        updateTotalCompleted()
        updateCompletionRate()
        updateStreaks()
    }
}