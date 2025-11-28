package com.dam.apphabitos

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
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
    }

    private fun setupBarChart() {
        //Datos de ejemplo - Última semana
        val entries = ArrayList<BarEntry>()
        entries.add(BarEntry(0f, 3f)) //Lunes
        entries.add(BarEntry(1f, 5f)) //Martes
        entries.add(BarEntry(2f, 7f)) //Miércoles
        entries.add(BarEntry(3f, 4f)) //Jueves
        entries.add(BarEntry(4f, 5f)) //Viernes
        entries.add(BarEntry(5f, 3f)) //Sábado
        entries.add(BarEntry(6f, 6f)) //Domingo

        val barDataSet = BarDataSet(entries, "Hábitos completados")

        //Colores del diseño de la app
        barDataSet.color = Color.parseColor("#3B82F6")
        barDataSet.valueTextColor = Color.WHITE
        barDataSet.valueTextSize = 12f

        val barData = BarData(barDataSet)
        barData.barWidth = 0.6f

        //Configuración del gráfico
        barChart.data = barData
        barChart.description.isEnabled = false
        barChart.legend.isEnabled = false
        barChart.setDrawValueAboveBar(true)
        barChart.setFitBars(true)
        barChart.animateY(1000)

        //Fondo oscuro
        barChart.setBackgroundColor(Color.parseColor("#1E293B"))
        barChart.setDrawGridBackground(false)

        //Configurar eje X (días de la semana)
        val xAxis = barChart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(false)
        xAxis.textColor = Color.parseColor("#94A3B8")
        xAxis.textSize = 11f
        xAxis.granularity = 1f
        xAxis.valueFormatter = IndexAxisValueFormatter(arrayOf("L", "M", "M", "J", "V", "S", "D"))

        //Configurar eje Y izquierdo
        val leftAxis = barChart.axisLeft
        leftAxis.textColor = Color.parseColor("#94A3B8")
        leftAxis.setDrawGridLines(true)
        leftAxis.gridColor = Color.parseColor("#334155")
        leftAxis.axisMinimum = 0f

        //Desactivar eje Y Derecho
        barChart.axisRight.isEnabled = false

        barChart.invalidate()
    }

    private fun setupPieChart() {
        //Datos de ejemplo
        val completeHabits = 15f
        val pendingHabits = 5f
        val totalHabits = completeHabits + pendingHabits

        val entries = ArrayList<PieEntry>()
        entries.add(PieEntry(completeHabits, "Completados"))
        entries.add(PieEntry(pendingHabits, "Pendientes"))

        val pieDataSet = PieDataSet(entries, "")

        //Colores del diseño de la app
        val colors = ArrayList<Int>()
        colors.add(Color.parseColor("#3B82F6"))
        colors.add(Color.parseColor("#475569"))
        pieDataSet.colors = colors

        pieDataSet.valueTextColor = Color.WHITE
        pieDataSet.valueTextSize = 16f
        pieDataSet.sliceSpace = 3f
        pieDataSet.selectionShift = 8f

        val pieData = PieData(pieDataSet)
        pieData.setValueFormatter(PercentFormatter(pieChart))

        //Configuración del gráfico circular
        pieChart.data = pieData
        pieChart.description.isEnabled = false
        pieChart.isRotationEnabled = true
        pieChart.setHoleColor(Color.parseColor("#1E293B"))
        pieChart.setTransparentCircleColor(Color.WHITE)
        pieChart.setTransparentCircleAlpha(50)
        pieChart.holeRadius = 50f
        pieChart.transparentCircleRadius = 55f
        pieChart.setDrawCenterText(true)
        pieChart.centerText = "85%\nCompletados"
        pieChart.setCenterTextColor(Color.WHITE)
        pieChart.setCenterTextSize(18f)
        pieChart.animateY(1000)

        //Fondo oscuro
        pieChart.setBackgroundColor(Color.parseColor("#1E293B"))

        //Leyenda
        val legend = pieChart.legend
        legend.textColor = Color.parseColor("#94A3B8")
        legend.textSize = 12f
        legend.isEnabled = true

        pieChart.setUsePercentValues(true)
        pieChart.invalidate()
    }
}