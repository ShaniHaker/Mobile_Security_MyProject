package com.example.mobile_security_myproject.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mobile_security_myproject.adapters.PermissionLogAdapter
import com.example.mobile_security_myproject.databinding.FragmentDashboardBinding
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.utils.ColorTemplate
import android.graphics.Color
import android.util.Log
import androidx.recyclerview.widget.DividerItemDecoration
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.formatter.ValueFormatter
import java.util.Locale
import com.github.mikephil.charting.components.LegendEntry




class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    private lateinit var pieChart: PieChart
    private lateinit var dashboardViewModel: DashboardViewModel
    private lateinit var recentLogsAdapter: PermissionLogAdapter

    val permissionColorMap = mapOf(
        "Location" to Color.parseColor("#4CAF50"),     // Green
        "Camera" to Color.parseColor("#F44336"),       // Red
        "Microphone" to Color.parseColor("#FFEB3B"),   // Yellow
        "Contacts" to Color.parseColor("#2196F3")      // Blue
    )


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        dashboardViewModel = ViewModelProvider(requireActivity())[DashboardViewModel::class.java]
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        val root: View = binding.root

        pieChart = binding.pieChart
        setupPieChart()

        // observer for changes
        dashboardViewModel.permissionData.observe(viewLifecycleOwner) { entries ->
            updatePieChart(entries)
        }

        // text for total amount of permissions
        dashboardViewModel.totalPermissionCount.observe(viewLifecycleOwner) { count ->
            if (count != null && count > 0) {
                binding.tvTotalUsage.text = "Total permission accesses today: $count"
            }
        }

        // recycleview
        recentLogsAdapter = PermissionLogAdapter(emptyList())
        binding.recyclerRecentActivity.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerRecentActivity.adapter = recentLogsAdapter

        // adding lines between rows in logs table
        val divider = DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL)
        binding.recyclerRecentActivity.addItemDecoration(divider)

        dashboardViewModel.recentPermissionLogs.observe(viewLifecycleOwner) { logs ->
            val sortedLogs = logs.sortedByDescending { it.timestamp }

            if (logs.isEmpty()) {
                binding.recyclerRecentActivity.visibility = View.GONE
                binding.tvNoLogs.visibility = View.VISIBLE
            } else {
                binding.recyclerRecentActivity.visibility = View.VISIBLE
                binding.tvNoLogs.visibility = View.GONE
                recentLogsAdapter.updateLogs(sortedLogs.take(5))
            }
        }

        return root
    }


    private fun setupPieChart() {
        pieChart.description.isEnabled = false
        pieChart.setNoDataText("") // no default text
        pieChart.centerText = "Permissions"
        pieChart.setEntryLabelTextSize(12f)
        pieChart.animateY(1000)
    }

    private fun updatePieChart(entries: List<PieEntry>) {
        if (entries.isEmpty()) {
            showEmptyPieChart()
            return
        }

        val safeEntries = entries.mapNotNull { entry ->
            val rawLabel = entry.label?.trim()
            val normalizedLabel = rawLabel?.replaceFirstChar { it.uppercaseChar() }
            val color = permissionColorMap[normalizedLabel]
            if (normalizedLabel != null && color != null) {
                Pair(PieEntry(entry.value, normalizedLabel), color)
            } else {
                Log.w("Dashboard", "⚠️ Skipping unknown label: $rawLabel")
                null
            }
        }

        if (safeEntries.isEmpty()) {
            showEmptyPieChart("📊\nNo recognizable data")
            return
        }

        val pieDataSet = PieDataSet(safeEntries.map { it.first }, "")
        pieDataSet.colors = safeEntries.map { it.second }
        pieDataSet.valueTextSize = 14f

        val pieData = PieData(pieDataSet)
        pieData.setValueFormatter(object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                return if (value % 1.0 == 0.0) {
                    String.format(Locale.US, "%.0f%%", value)
                } else {
                    String.format(Locale.US, "%.2f%%", value)
                }
            }
        })

        pieChart.data = pieData
        pieChart.setDrawEntryLabels(false)
        pieChart.centerText = "📊\nTotal"
        pieChart.setCenterTextSize(16f)
        pieChart.setCenterTextColor(Color.GRAY)
        pieChart.setHighlightPerTapEnabled(false)

        pieChart.legend.isEnabled = true


        pieChart.invalidate()
    }


    private fun showEmptyPieChart(message: String = "📊\nNo data") {
        val dummyEntry = PieEntry(1f, "")
        val dummyDataSet = PieDataSet(listOf(dummyEntry), "")
        dummyDataSet.colors = listOf(Color.LTGRAY)
        dummyDataSet.valueTextColor = Color.TRANSPARENT
        dummyDataSet.setDrawValues(false)

        val dummyData = PieData(dummyDataSet)
        pieChart.data = dummyData
        pieChart.centerText = message
        pieChart.setCenterTextSize(16f)
        pieChart.setCenterTextColor(Color.GRAY)
        pieChart.legend.isEnabled = false
        pieChart.invalidate()
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}