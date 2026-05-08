package com.shaun.stocktracker.ui.alerts

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.shaun.stocktracker.R
import com.shaun.stocktracker.data.entity.AlertCondition
import com.shaun.stocktracker.data.entity.AlertType
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * RecyclerView adapter for alert conditions.
 * Uses ListAdapter + DiffUtil for efficient updates.
 */
class AlertAdapter(
    private val onToggle: (AlertCondition) -> Unit,
    private val onEdit: (AlertCondition) -> Unit,
    private val onDelete: (AlertCondition) -> Unit
) : ListAdapter<AlertCondition, AlertAdapter.AlertViewHolder>(AlertDiffCallback) {

    private val currencyFormat = DecimalFormat("#,##,##0.00")
    private val dateFormat = SimpleDateFormat("dd MMM, hh:mm a", Locale.ENGLISH).apply {
        timeZone = TimeZone.getTimeZone("Asia/Kolkata")
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlertViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_alert, parent, false)
        return AlertViewHolder(view)
    }

    override fun onBindViewHolder(holder: AlertViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class AlertViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val tvSymbol: TextView = itemView.findViewById(R.id.tv_alert_symbol)
        private val tvType: TextView = itemView.findViewById(R.id.tv_alert_type)
        private val tvPrice: TextView = itemView.findViewById(R.id.tv_alert_price)
        private val tvStatus: TextView = itemView.findViewById(R.id.tv_alert_status)
        private val tvCreated: TextView = itemView.findViewById(R.id.tv_alert_created)
        private val btnToggle: ImageButton = itemView.findViewById(R.id.btn_alert_toggle)
        private val btnEdit: ImageButton = itemView.findViewById(R.id.btn_alert_edit)
        private val btnDelete: ImageButton = itemView.findViewById(R.id.btn_alert_delete)

        fun bind(alert: AlertCondition) {
            tvSymbol.text = alert.tradingSymbol

            // Type badge
            tvType.text = alert.alertType.displayName
            tvType.setBackgroundResource(
                if (alert.alertType == AlertType.ABOVE)
                    R.drawable.bg_badge_green
                else
                    R.drawable.bg_badge_red
            )

            // Target price
            tvPrice.text = "₹${currencyFormat.format(alert.targetPrice)}"

            // Status
            val status = getStatus(alert)
            tvStatus.text = status.first
            tvStatus.setTextColor(itemView.context.getColor(status.second))

            // Created date
            tvCreated.text = dateFormat.format(Date(alert.createdAt))

            // Alpha for disabled
            itemView.alpha = if (alert.isEnabled) 1.0f else 0.5f

            // Toggle icon
            btnToggle.setImageResource(
                if (alert.isEnabled)
                    android.R.drawable.ic_media_pause
                else
                    android.R.drawable.ic_media_play
            )
            btnToggle.contentDescription = if (alert.isEnabled) "Disable" else "Enable"

            // Click listeners
            btnToggle.setOnClickListener { onToggle(alert) }
            btnEdit.setOnClickListener { onEdit(alert) }
            btnDelete.setOnClickListener { onDelete(alert) }
        }

        private fun getStatus(alert: AlertCondition): Pair<String, Int> {
            return when {
                !alert.isEnabled -> "DISABLED" to android.R.color.darker_gray
                alert.lastTriggeredTimestamp != null -> "TRIGGERED" to android.R.color.holo_orange_dark
                else -> "ACTIVE" to android.R.color.holo_green_dark
            }
        }
    }

    object AlertDiffCallback : DiffUtil.ItemCallback<AlertCondition>() {
        override fun areItemsTheSame(a: AlertCondition, b: AlertCondition) = a.id == b.id
        override fun areContentsTheSame(a: AlertCondition, b: AlertCondition) = a == b
    }
}
