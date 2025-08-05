package com.example.mobile_security_myproject.services

import android.app.Service
import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Handler
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import com.github.mikephil.charting.data.PieEntry
import java.text.SimpleDateFormat
import java.util.*
import kotlin.concurrent.schedule

data class PermissionAccessLog(
    val appName: String,
    val permissionLabel: String,
    val time: String,
    val timestamp: Long
)

class PermissionMonitorService : Service() {

    companion object {
        val permissionPieData = MutableLiveData<List<PieEntry>>()
        val totalPermissionAccessesToday = MutableLiveData<Int>()
        val recentPermissionAccessList = MutableLiveData<List<PermissionAccessLog>>()
        private const val TAG = "PermissionMonitor"
    }

    private lateinit var usageStatsManager: UsageStatsManager
    private lateinit var packageManager: PackageManager

    private val monitoredPermissions = listOf(
        android.Manifest.permission.CAMERA,
        android.Manifest.permission.ACCESS_FINE_LOCATION,
        android.Manifest.permission.RECORD_AUDIO,
        android.Manifest.permission.READ_CONTACTS
    )

    private val permissionLabels = mapOf(
        android.Manifest.permission.CAMERA to "Camera",
        android.Manifest.permission.ACCESS_FINE_LOCATION to "Location",
        android.Manifest.permission.RECORD_AUDIO to "Microphone",
        android.Manifest.permission.READ_CONTACTS to "Contacts"
    )

    private var timer: Timer? = null
    private val recentLogs = mutableListOf<PermissionAccessLog>()
    private val appPermissionsSeen = mutableMapOf<String, MutableSet<String>>()
    private var dailyAccessCount = 0
    private var serviceStartTime: Long = 0L

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Service created and started")

        serviceStartTime = System.currentTimeMillis()
        dailyAccessCount = 0
        appPermissionsSeen.clear()
        recentLogs.clear()

        totalPermissionAccessesToday.postValue(0)
        permissionPieData.postValue(emptyList())
        recentPermissionAccessList.postValue(emptyList())

        usageStatsManager = getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        packageManager = applicationContext.packageManager
        startMonitoring()
    }

    private fun startMonitoring() {
        timer = Timer()
        timer?.schedule(object : TimerTask() {
            override fun run() {
                calculatePermissionUsage()
            }
        }, 0, 5000)
    }

    private fun calculatePermissionUsage() {
        val now = System.currentTimeMillis()
        val startTime = now - 10_000

        val recentUsage: List<UsageStats> = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            startTime,
            now
        ) ?: return

        val runningApps = recentUsage
            .filter { it.lastTimeUsed >= startTime && it.totalTimeInForeground > 0 }
            .map { it.packageName }
            .distinct()

        Log.d(TAG, "⏱ Found ${runningApps.size} running apps: $runningApps")

        val blacklistPackages = setOf(
            applicationContext.packageName,
            "com.sec.android.app.launcher",
            "com.sec.android.app.desktoplauncher",
            "com.samsung.android.app.launcher"
        )

        var currentCount = 0

        for (packageName in runningApps) {
            Log.d(TAG, "xxx0 $packageName")
            if (blacklistPackages.contains(packageName)) {
                Log.w(TAG, "⛔ Skipping blacklisted package: $packageName")
                continue
            }

            val permissionsSeenForApp = appPermissionsSeen.getOrPut(packageName) { mutableSetOf() }

            Log.d(TAG, "🟢 Checking app: $packageName (already seen: ${permissionsSeenForApp.joinToString()})")

            for (permission in monitoredPermissions) {
                Log.d(TAG, "xxx1 permission - $permission, array - $permissionLabels")
                Log.d(TAG, "xxx2 ${permissionLabels[permission]}")
                Log.d(TAG, "xxx3 $permissionsSeenForApp")
                val label = permissionLabels[permission] ?: continue
                if (permissionsSeenForApp.contains(label)) {
                    Log.d(TAG, "🔁 Already logged $label for $packageName — skipping")
                    continue
                }

                val hasPermission = packageManager.checkPermission(permission, packageName) == PackageManager.PERMISSION_GRANTED
                Log.d(TAG, "🔍 $packageName has permission $label: $hasPermission")

                if (hasPermission) {
                    currentCount++

                    val appLabel = try {
                        packageManager.getApplicationLabel(
                            packageManager.getApplicationInfo(packageName, 0)
                        ).toString()
                    } catch (e: Exception) {
                        packageName
                    }

                    val currentMillis = System.currentTimeMillis()
                    val formattedTime = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(currentMillis)
                    val log = PermissionAccessLog(appLabel, label, formattedTime, currentMillis)

                    synchronized(recentLogs) {
                        if (!recentLogs.any { it.appName == appLabel && it.permissionLabel == label }) {
                            recentLogs.add(log)
                            Log.i(TAG, "📦 Logged permission $label used by $appLabel at $formattedTime")

                            // toast
                            val toastText = "$appLabel used $label permission"
                            Handler(mainLooper).post {
                                Toast.makeText(applicationContext, toastText, Toast.LENGTH_LONG).show()
                            }
                        } else {
                            Log.d(TAG, "⚠️ Skipped duplicate log for $appLabel $label")
                        }
                    }

                    permissionsSeenForApp.add(label)
                }

            }
        }

        synchronized(recentLogs) {
            recentLogs.sortByDescending { it.timestamp }
        }

        //  use actual log data for pie chart
        val permissionCounts = recentLogs
            .groupingBy { it.permissionLabel }
            .eachCount()

        if (permissionCounts.isEmpty()) {
            Log.d(TAG, "🚫 No permission usage found in logs")
            return
        }

        dailyAccessCount += currentCount

        val total = permissionCounts.values.sum().takeIf { it != 0 } ?: 1
        val entries = permissionCounts.map { (label, count) ->
            val percent = count.toFloat() / total * 100
            val roundedPercent = String.format(Locale.US, "%.2f", percent).toFloat() // או %.2f לשתי ספרות
            Log.d(TAG, "📊 Pie entry: $label -> $roundedPercent%")
            PieEntry(roundedPercent, label)
        }


        permissionPieData.postValue(entries)
        totalPermissionAccessesToday.postValue(dailyAccessCount)
        recentPermissionAccessList.postValue(recentLogs.toList())

        Log.d(TAG, "✅ Updated pie chart with ${entries.size} entries and total $dailyAccessCount accesses")
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        timer?.cancel()
        Log.d(TAG, "Service destroyed")
    }
}
