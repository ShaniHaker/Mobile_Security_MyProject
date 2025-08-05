package com.example.mobile_security_myproject.adapters


import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.mobile_security_myproject.databinding.ItemPermissionLogBinding
import com.example.mobile_security_myproject.services.PermissionAccessLog

class PermissionLogAdapter(
    private var logList: List<PermissionAccessLog>
) : RecyclerView.Adapter<PermissionLogAdapter.LogViewHolder>() {

    inner class LogViewHolder(private val binding: ItemPermissionLogBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(log: PermissionAccessLog) {
            Log.d("BIND", "📄 Binding log: ${log.appName}, ${log.permissionLabel}, ${log.time}")
            binding.tvAppName.text = log.appName
            binding.tvPermissionLabel.text = log.permissionLabel
            binding.tvAccessTime.text = log.time
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LogViewHolder {
        val binding = ItemPermissionLogBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return LogViewHolder(binding)
    }

    override fun onBindViewHolder(holder: LogViewHolder, position: Int) {
        holder.bind(logList[position])
    }

    override fun getItemCount(): Int = logList.size

    fun updateLogs(newLogs: List<PermissionAccessLog>) {
        logList = newLogs
        notifyDataSetChanged()
    }
}
