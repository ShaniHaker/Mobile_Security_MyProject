package com.example.mobile_security_myproject.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mobile_security_myproject.adapters.PermissionLogAdapter
import com.example.mobile_security_myproject.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var homeViewModel: HomeViewModel
    private lateinit var adapter: PermissionLogAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        homeViewModel = ViewModelProvider(this)[HomeViewModel::class.java]
        adapter = PermissionLogAdapter(emptyList())

        binding.recyclerAllLogs.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerAllLogs.adapter = adapter

        // adding separate lines
        val divider = androidx.recyclerview.widget.DividerItemDecoration(
            requireContext(),
            LinearLayoutManager.VERTICAL
        )
        binding.recyclerAllLogs.addItemDecoration(divider)

        homeViewModel.allLogs.observe(viewLifecycleOwner) { logs ->
            val sortedLogs = logs.sortedByDescending { it.timestamp }
            adapter.updateLogs(sortedLogs)
        }

        return root
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}