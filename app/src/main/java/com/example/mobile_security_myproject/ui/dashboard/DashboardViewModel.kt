package com.example.mobile_security_myproject.ui.dashboard


import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.mobile_security_myproject.services.PermissionAccessLog
import com.example.mobile_security_myproject.services.PermissionMonitorService
import com.github.mikephil.charting.data.PieEntry

class DashboardViewModel : ViewModel() {

    private val _permissionData = MutableLiveData<List<PieEntry>>().apply {
        value = emptyList()
    }
    val permissionData: LiveData<List<PieEntry>> = _permissionData

    private val _totalPermissionCount = MutableLiveData<Int>().apply {
        value = 0
    }
    val totalPermissionCount: LiveData<Int> = _totalPermissionCount

    private val _recentPermissionLogs = MutableLiveData<List<PermissionAccessLog>>().apply {
        value = emptyList()
    }
    val recentPermissionLogs: LiveData<List<PermissionAccessLog>> = _recentPermissionLogs

    init {
        PermissionMonitorService.permissionPieData.observeForever {
            _permissionData.postValue(it)
        }

        PermissionMonitorService.totalPermissionAccessesToday.observeForever {
            _totalPermissionCount.postValue(it)
        }

        PermissionMonitorService.recentPermissionAccessList.observeForever { logs ->
            _recentPermissionLogs.postValue(logs)
        }
    }

    fun updatePermissions(entries: List<PieEntry>) {
        _permissionData.value = entries
    }
}
