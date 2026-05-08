package com.shaun.stocktracker.ui.alerts

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.shaun.stocktracker.R
import com.shaun.stocktracker.data.entity.AlertCondition
import com.shaun.stocktracker.data.entity.AlertType

/**
 * Material dialog for creating or editing an alert condition.
 * Uses activityViewModels() to share state with AlertsFragment.
 */
class CreateAlertDialog : DialogFragment() {

    private val viewModel: AlertsViewModel by activityViewModels()

    companion object {
        fun newInstance() = CreateAlertDialog()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val inflater = LayoutInflater.from(requireContext())
        val view = inflater.inflate(R.layout.dialog_create_alert, null)

        val etSymbol = view.findViewById<TextInputEditText>(R.id.et_alert_symbol)
        val dropdownType = view.findViewById<AutoCompleteTextView>(R.id.dropdown_alert_type)
        val etPrice = view.findViewById<TextInputEditText>(R.id.et_alert_price)

        // Setup alert type dropdown
        val typeItems = AlertType.entries.map { it.displayName }
        val typeAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, typeItems)
        dropdownType.setAdapter(typeAdapter)
        dropdownType.setText(typeItems[0], false)

        // If editing, populate fields
        val editing = viewModel.editingAlert.value
        val isEdit = editing != null
        if (isEdit && editing != null) {
            etSymbol.setText(editing.tradingSymbol)
            etSymbol.isEnabled = false // Don't allow symbol change in edit mode
            dropdownType.setText(editing.alertType.displayName, false)
            etPrice.setText(editing.targetPrice.toString())
        }

        val title = if (isEdit) "Edit Alert" else "Create Alert"
        val action = if (isEdit) "Update" else "Create"

        return MaterialAlertDialogBuilder(requireContext())
            .setTitle(title)
            .setView(view)
            .setPositiveButton(action) { _, _ ->
                val symbol = etSymbol.text?.toString()?.trim()?.uppercase() ?: ""
                val typeText = dropdownType.text?.toString() ?: ""
                val priceText = etPrice.text?.toString()?.trim() ?: ""

                // Validation
                if (symbol.isEmpty()) {
                    Toast.makeText(context, "Enter a stock symbol", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                val price = priceText.toDoubleOrNull()
                if (price == null || price <= 0) {
                    Toast.makeText(context, "Enter a valid price", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                val alertType = AlertType.entries.find { it.displayName == typeText } ?: AlertType.ABOVE

                if (isEdit && editing != null) {
                    viewModel.updateAlert(
                        editing.copy(
                            alertType = alertType,
                            targetPrice = price
                        )
                    )
                } else {
                    viewModel.createAlert(symbol, alertType, price)
                }
                viewModel.clearEditing()
            }
            .setNegativeButton("Cancel") { _, _ ->
                viewModel.clearEditing()
            }
            .create()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.clearEditing()
    }
}
