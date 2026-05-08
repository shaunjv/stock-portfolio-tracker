package com.shaun.stocktracker.util

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import com.shaun.stocktracker.data.entity.AlertType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * Appends alert entries and daily summaries to a persistent text file.
 *
 * File location: /Documents/PortfolioAlerts.txt
 * - Uses MediaStore API on Android 10+ (scoped storage compliant)
 * - Uses direct file I/O on Android 9 and below
 * - Never overwrites — append only
 * - Adds a dated header separator once per calendar day
 */
class NotepadWriter(private val context: Context) {

    companion object {
        private const val TAG = "NotepadWriter"
        private const val FILE_NAME = "PortfolioAlerts.txt"
        private const val MIME_TYPE = "text/plain"

        private const val HEADER_SEPARATOR = "════════════════════════════════════"

        private val IST = TimeZone.getTimeZone("Asia/Kolkata")
        private val DATE_HEADER_FORMAT = SimpleDateFormat("EEE, dd MMM yyyy", Locale.ENGLISH)
        private val TIME_FORMAT = SimpleDateFormat("hh:mm a", Locale.ENGLISH)
        private val DATE_KEY_FORMAT = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
        private val CURRENCY_FORMAT = DecimalFormat("#,##,##0.00")
        private val CURRENCY_INT_FORMAT = DecimalFormat("#,##,##0")

        init {
            DATE_HEADER_FORMAT.timeZone = IST
            TIME_FORMAT.timeZone = IST
            DATE_KEY_FORMAT.timeZone = IST
        }
    }

    // Tracks whether today's date header has been written this session
    private var lastWrittenDate: String? = null

    /**
     * Writes an alert entry (ABOVE or BELOW) to the log file.
     *
     * @param symbol        Stock symbol (e.g., "RELIANCE")
     * @param alertType     "ABOVE" or "BELOW"
     * @param currentPrice  The LTP that triggered the alert
     * @param targetPrice   The target/stop-loss value
     */
    suspend fun writeAlertEntry(
        symbol: String,
        alertType: AlertType,
        currentPrice: Double,
        targetPrice: Double
    ) {
        val now = Date()
        val time = TIME_FORMAT.format(now)
        val priceStr = "₹${CURRENCY_FORMAT.format(currentPrice)}"
        val targetStr = "₹${CURRENCY_FORMAT.format(targetPrice)}"

        val entry = when (alertType) {
            AlertType.ABOVE -> buildString {
                appendLine()
                appendLine("🟢 $time — $symbol crossed above $targetStr")
                appendLine("   Triggered at: $priceStr | Target was: $targetStr")
            }
            AlertType.BELOW -> buildString {
                appendLine()
                appendLine("🔴 $time — $symbol dropped below $targetStr")
                appendLine("   Triggered at: $priceStr | Stop loss was: $targetStr")
            }
        }

        appendToFile(entry, now)
    }

    /**
     * Writes the 3:30 PM daily portfolio summary.
     *
     * @param portfolioValue  Total portfolio value
     * @param todayPnl        Today's P&L amount
     * @param pnlPercentage   Today's P&L percentage
     * @param winners         List of winning stock symbols
     * @param losers          List of losing stock symbols
     */
    suspend fun writeDailySummary(
        portfolioValue: Double,
        todayPnl: Double,
        pnlPercentage: Double,
        winners: List<String>,
        losers: List<String>
    ) {
        val now = Date()
        val time = TIME_FORMAT.format(now)
        val sign = if (todayPnl >= 0) "+" else ""
        val valueStr = "₹${CURRENCY_INT_FORMAT.format(portfolioValue)}"
        val pnlStr = "${sign}₹${CURRENCY_INT_FORMAT.format(todayPnl)}"
        val pctStr = "${sign}${String.format(Locale.ENGLISH, "%.1f", pnlPercentage)}%"

        val entry = buildString {
            appendLine()
            appendLine("📊 $time — Daily Summary")
            appendLine("   Portfolio Value : $valueStr")
            appendLine("   Today's P&L    : $pnlStr ($pctStr)")
            appendLine("   Winners        : ${winners.joinToString(", ").ifEmpty { "—" }}")
            appendLine("   Losers         : ${losers.joinToString(", ").ifEmpty { "—" }}")
        }

        appendToFile(entry, now)
    }

    /**
     * Core append logic. Ensures the date header exists, then appends the entry.
     * Routes to MediaStore (API 29+) or direct file I/O (API 26-28).
     */
    private suspend fun appendToFile(entry: String, now: Date) {
        val todayKey = DATE_KEY_FORMAT.format(now)
        val contentToWrite = buildString {
            if (lastWrittenDate != todayKey) {
                appendLine()
                appendLine(HEADER_SEPARATOR)
                appendLine("📅 ${DATE_HEADER_FORMAT.format(now)}")
                appendLine(HEADER_SEPARATOR)
                lastWrittenDate = todayKey
            }
            append(entry)
        }

        withContext(Dispatchers.IO) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    appendViaMediaStore(contentToWrite)
                } else {
                    appendViaDirectFile(contentToWrite)
                }
                EventLogger.log(EventLogger.Event.FILE_WRITE, "Appended ${contentToWrite.length} chars to notepad")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to write to notepad file", e)
                EventLogger.log(EventLogger.Event.FILE_WRITE_ERROR, "Write failed: ${e.message}")
            }
        }
    }

    /**
     * Android 10+ (API 29+): Uses MediaStore to find or create the file,
     * then appends content via ContentResolver.
     */
    private fun appendViaMediaStore(content: String) {
        val resolver = context.contentResolver
        val existingUri = findExistingFile()

        if (existingUri != null) {
            // File exists — open in append mode
            resolver.openOutputStream(existingUri, "wa")?.use { stream ->
                stream.write(content.toByteArray(Charsets.UTF_8))
                stream.flush()
            } ?: Log.e(TAG, "Failed to open output stream for existing file")
        } else {
            // File doesn't exist — create it with header
            val values = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, FILE_NAME)
                put(MediaStore.MediaColumns.MIME_TYPE, MIME_TYPE)
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOCUMENTS)
            }
            val uri = resolver.insert(MediaStore.Files.getContentUri("external"), values)
            if (uri != null) {
                resolver.openOutputStream(uri)?.use { stream ->
                    stream.write(content.toByteArray(Charsets.UTF_8))
                    stream.flush()
                } ?: Log.e(TAG, "Failed to open output stream for new file")
            } else {
                Log.e(TAG, "Failed to create file via MediaStore")
            }
        }
    }

    /**
     * Queries MediaStore for the existing PortfolioAlerts.txt in Documents.
     * @return Uri if found, null otherwise
     */
    private fun findExistingFile(): Uri? {
        val resolver = context.contentResolver
        val collection = MediaStore.Files.getContentUri("external")
        val projection = arrayOf(MediaStore.MediaColumns._ID)
        val selection = "${MediaStore.MediaColumns.DISPLAY_NAME} = ? AND " +
                "${MediaStore.MediaColumns.RELATIVE_PATH} LIKE ?"
        val selectionArgs = arrayOf(FILE_NAME, "%${Environment.DIRECTORY_DOCUMENTS}%")

        resolver.query(collection, projection, selection, selectionArgs, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID))
                return Uri.withAppendedPath(collection, id.toString())
            }
        }
        return null
    }

    /**
     * Android 9 and below (API 26-28): Direct file system access.
     * Requires WRITE_EXTERNAL_STORAGE permission.
     */
    @Suppress("DEPRECATION")
    private fun appendViaDirectFile(content: String) {
        val documentsDir = Environment.getExternalStoragePublicDirectory(
            Environment.DIRECTORY_DOCUMENTS
        )
        if (!documentsDir.exists()) documentsDir.mkdirs()

        val file = File(documentsDir, FILE_NAME)
        FileOutputStream(file, true).use { stream ->
            stream.write(content.toByteArray(Charsets.UTF_8))
            stream.flush()
        }
    }
}
