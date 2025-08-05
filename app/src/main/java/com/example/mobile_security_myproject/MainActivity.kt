package com.example.mobile_security_myproject

import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.mobile_security_myproject.databinding.ActivityMainBinding
import com.example.mobile_security_myproject.services.PermissionMonitorService

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d("MainActivity1", "onCreate called")

        checkUsageStatsPermission() // 🔍 checking for access usage stats

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // new service each time we open the app
        Log.d("MainActivity1", "Restarting PermissionMonitorService")
        val serviceIntent = Intent(this, PermissionMonitorService::class.java)
        stopService(serviceIntent) // stp service from before
        startService(serviceIntent) // starting new one

        val navView: BottomNavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
    }

    private fun checkUsageStatsPermission() {
        val appOps = getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = appOps.checkOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            android.os.Process.myUid(),
            packageName
        )

        if (mode != AppOpsManager.MODE_ALLOWED) {
            Log.w("MainActivity1", "Usage stats permission NOT granted - opening settings")
            startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
            Toast.makeText(this, "Please allow Usage Access for this app", Toast.LENGTH_LONG).show()
        } else {
            Log.d("MainActivity1", "Usage stats permission granted")
            Toast.makeText(this, "✅ Usage Access permission granted", Toast.LENGTH_SHORT).show()
        }
    }
}