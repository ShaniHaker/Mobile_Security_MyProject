package com.example.mobile_security_myproject.ui.notifications


import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.mobile_security_myproject.services.PermissionAccessLog
import com.example.mobile_security_myproject.services.PermissionMonitorService


class NotificationsViewModel : ViewModel() {

    private val _logs = MutableLiveData<List<PermissionAccessLog>>()
    val logs: LiveData<List<PermissionAccessLog>> = _logs

    init {
        PermissionMonitorService.recentPermissionAccessList.observeForever { list ->
            _logs.postValue(list)
        }
    }
}