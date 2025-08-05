package com.example.mobile_security_myproject.ui.notifications

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.mobile_security_myproject.databinding.FragmentNotificationsBinding
import com.example.mobile_security_myproject.services.PermissionAccessLog
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.LegendEntry
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter

class NotificationsFragment : Fragment() {

    private var _binding: FragmentNotificationsBinding? = null
    private val binding get() = _binding!!

    private lateinit var barChart: BarChart
    private lateinit var viewModel: NotificationsViewModel

    private var hasAnimated = false // flag
    private var lastDisplayedLabels: List<String> = emptyList()

    private val TAG = "NotificationsFragment"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNotificationsBinding.inflate(inflater, container, false)
        barChart = binding.barChart

        viewModel = ViewModelProvider(this)[NotificationsViewModel::class.java]

        viewModel.logs.observe(viewLifecycleOwner) { logs ->
            Log.d(TAG, "Received ${logs.size} logs")
            updateChart(logs)
        }

        return binding.root
    }

    private fun updateChart(logs: List<PermissionAccessLog>) {
        if (logs.isEmpty()) {
            binding.tvNoData.visibility = View.VISIBLE
            Log.d(TAG, "No logs found. Showing empty chart.")

            // empty bars chart
            val dummyEntry = BarEntry(0f, 0f)
            val dummyDataSet = BarDataSet(listOf(dummyEntry), "")
            dummyDataSet.setDrawValues(false)
            dummyDataSet.color = Color.TRANSPARENT

            val barData = BarData(dummyDataSet)
            barData.barWidth = 0.8f

            barChart.data = barData

            barChart.xAxis.apply {
                valueFormatter = IndexAxisValueFormatter(listOf("")) // empty lable in x
                position = XAxis.XAxisPosition.BOTTOM
                setDrawGridLines(false)
                granularity = 1f
                isGranularityEnabled = true
                textSize = 12f
                axisMinimum = -0.5f
                axisMaximum = 0.5f
                setLabelCount(1, false)
            }

            barChart.axisLeft.apply {
                axisMinimum = 0f
                axisMaximum = 4f
                granularity = 1f
                isGranularityEnabled = true
            }

            barChart.axisRight.isEnabled = false
            barChart.description.isEnabled = false
            barChart.legend.isEnabled = false // no empty legend

            barChart.setTouchEnabled(false)
            barChart.setDragEnabled(false)
            barChart.setScaleEnabled(false)
            barChart.setPinchZoom(false)
            barChart.setHighlightPerTapEnabled(false)
            barChart.setHighlightPerDragEnabled(false)
            barChart.setExtraBottomOffset(40f)
            barChart.setFitBars(true)
            barChart.setNoDataText("📊 No permission activity yet")
            barChart.invalidate()
            return // exit function
        }
        binding.tvNoData.visibility = View.GONE
        val grouped = logs.groupBy { it.appName }
        val sortedApps = grouped.entries
            .sortedByDescending { it.value.map { log -> log.permissionLabel }.distinct().count() }
            .take(4)

        val entries = mutableListOf<BarEntry>()
        val labels = mutableListOf<String>()
        val colors = mutableListOf<Int>()

        sortedApps.forEachIndexed { index, entry ->
            val permissionCount = entry.value.map { it.permissionLabel }.distinct().count()
            val appName = if (entry.key.isBlank()) "Unknown" else entry.key

            entries.add(BarEntry(index.toFloat(), permissionCount.toFloat()))
            labels.add(appName)

            val color = when (permissionCount) {
                in 0..1 -> Color.GREEN
                2, 3 -> Color.rgb(255, 165, 0)
                else -> Color.RED
            }
            colors.add(color)
        }

        // if no updated then skipping update
        if (labels == lastDisplayedLabels) {
            Log.d(TAG, "Chart data unchanged. Skipping update.")
            return
        }
        lastDisplayedLabels = labels

        barChart.clear()

        val dataSet = BarDataSet(entries, "Risk Level").apply {
            setColors(colors)
            valueTextSize = 14f
            valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    return value.toInt().toString()
                }
            }
        }

        val barData = BarData(dataSet)
        barData.barWidth = 0.8f
        barChart.data = barData

        barChart.xAxis.apply {
            valueFormatter = IndexAxisValueFormatter(labels)
            position = XAxis.XAxisPosition.BOTTOM
            setDrawGridLines(false)
            granularity = 1f
            isGranularityEnabled = true
            textSize = 12f
            labelRotationAngle = 45f
            axisMinimum = -0.5f
            axisMaximum = (labels.size - 1) + 0.5f
            setLabelCount(labels.size, false)
        }

        barChart.axisLeft.apply {
            axisMinimum = 0f
            axisMaximum = 4f
            granularity = 1f
            isGranularityEnabled = true
        }

        barChart.axisRight.isEnabled = false
        barChart.description.isEnabled = false

        barChart.legend.apply {
            isEnabled = true
            form = Legend.LegendForm.SQUARE
            textSize = 14f
            verticalAlignment = Legend.LegendVerticalAlignment.TOP
            horizontalAlignment = Legend.LegendHorizontalAlignment.RIGHT
            orientation = Legend.LegendOrientation.VERTICAL
            setDrawInside(false)
            setCustom(
                listOf(
                    LegendEntry("Low Risk (1)", Legend.LegendForm.SQUARE, 10f, 2f, null, Color.GREEN),
                    LegendEntry("Medium Risk (2–3)", Legend.LegendForm.SQUARE, 10f, 2f, null, Color.rgb(255, 165, 0)),
                    LegendEntry("High Risk (4)", Legend.LegendForm.SQUARE, 10f, 2f, null, Color.RED)
                )
            )
        }

        barChart.setTouchEnabled(false)
        barChart.setDragEnabled(false)
        barChart.setScaleEnabled(false)
        barChart.setPinchZoom(false)
        barChart.setHighlightPerTapEnabled(false)
        barChart.setHighlightPerDragEnabled(false)
        barChart.setExtraBottomOffset(40f)
        barChart.setFitBars(true)

        // Animation once only
        if (!hasAnimated) {
            barChart.animateY(500)
            hasAnimated = true
        }

        barChart.post { barChart.invalidate() }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

