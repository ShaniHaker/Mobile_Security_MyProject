package com.example.mobile_security_myproject.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.mobile_security_myproject.services.PermissionAccessLog
import com.example.mobile_security_myproject.services.PermissionMonitorService

class HomeViewModel : ViewModel() {

    private val _allLogs = MutableLiveData<List<PermissionAccessLog>>()
    val allLogs: LiveData<List<PermissionAccessLog>> = _allLogs

    init {
        // Connect to the service's log data
        PermissionMonitorService.recentPermissionAccessList.observeForever { logs ->
            _allLogs.postValue(logs)
        }
    }
}
