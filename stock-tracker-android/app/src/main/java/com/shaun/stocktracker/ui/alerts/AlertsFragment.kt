package com.shaun.stocktracker.ui.alerts

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.search.SearchBar
import com.shaun.stocktracker.R
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * Alerts Tab Fragment.
 * Displays active alerts with filter chips, search, and a FAB to create new alerts.
 */
class AlertsFragment : Fragment() {

    private val viewModel: AlertsViewModel by activityViewModels()
    private lateinit var adapter: AlertAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Force the Material3 theme to prevent crashes when inflating Material components from Compose
        val themedContext = android.view.ContextThemeWrapper(requireContext(), com.google.android.material.R.style.Theme_Material3_DayNight_NoActionBar)
        val themedInflater = inflater.cloneInContext(themedContext)
        return themedInflater.inflate(R.layout.fragment_alerts, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recyclerView = view.findViewById<RecyclerView>(R.id.rv_alerts)
        val chipGroup = view.findViewById<ChipGroup>(R.id.chip_group_filters)
        val fabCreate = view.findViewById<FloatingActionButton>(R.id.fab_create_alert)
        val tvEmpty = view.findViewById<TextView>(R.id.tv_empty_state)
        val progressBar = view.findViewById<ProgressBar>(R.id.progress_alerts)

        // Setup adapter
        adapter = AlertAdapter(
            onToggle = { viewModel.toggleAlert(it) },
            onEdit = {
                viewModel.startEditing(it)
                CreateAlertDialog.newInstance().show(childFragmentManager, "edit_alert")
            },
            onDelete = { alert ->
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Delete Alert")
                    .setMessage("Delete ${alert.tradingSymbol} ${alert.alertType.displayName} at ₹${alert.targetPrice}?")
                    .setPositiveButton("Delete") { _, _ -> viewModel.deleteAlert(alert.id) }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
        )

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        // Setup filter chips
        setupFilterChips(chipGroup)

        // FAB
        fabCreate.setOnClickListener {
            viewModel.clearEditing()
            CreateAlertDialog.newInstance().show(childFragmentManager, "create_alert")
        }

        // Observe state
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.alerts.collectLatest { alerts ->
                        adapter.submitList(alerts)
                        tvEmpty.visibility = if (alerts.isEmpty()) View.VISIBLE else View.GONE
                        recyclerView.visibility = if (alerts.isEmpty()) View.GONE else View.VISIBLE
                    }
                }
                launch {
                    viewModel.isLoading.collectLatest { loading ->
                        progressBar.visibility = if (loading) View.VISIBLE else View.GONE
                    }
                }
            }
        }
    }

    private fun setupFilterChips(chipGroup: ChipGroup) {
        chipGroup.removeAllViews()
        AlertFilter.entries.forEach { filter ->
            val chip = Chip(requireContext()).apply {
                text = filter.displayName
                isCheckable = true
                isChecked = filter == AlertFilter.ALL
                setOnClickListener {
                    viewModel.setFilter(filter)
                    // Update chip checked states
                    for (i in 0 until chipGroup.childCount) {
                        (chipGroup.getChildAt(i) as? Chip)?.isChecked = false
                    }
                    isChecked = true
                }
            }
            chipGroup.addView(chip)
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadAlerts()
    }
}
