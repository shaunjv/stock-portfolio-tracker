package com.shaun.stocktracker.util

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

/**
 * Manages session state and encrypted credential storage.
 * Uses EncryptedSharedPreferences — all sensitive data is AES-256 encrypted at rest.
 */
class SessionManager(context: Context) {

    companion object {
        private const val PREFS_NAME = "stock_tracker_secure_prefs"
        private const val KEY_BASE_URL = "base_url"
        private const val KEY_SESSION_ACTIVE = "session_active"
        private const val KEY_LAST_LOGIN_TIME = "last_login_time"

        // Default: Android emulator localhost. Change for physical device.
        private const val DEFAULT_BASE_URL = "https://stock-portfolio-tracker-production-8b3d.up.railway.app"
    }

    private val prefs: SharedPreferences

    init {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        prefs = EncryptedSharedPreferences.create(
            context,
            PREFS_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    // ── Base URL ──────────────────────────────────────────────

    fun getBaseUrl(): String =
        prefs.getString(KEY_BASE_URL, DEFAULT_BASE_URL) ?: DEFAULT_BASE_URL

    fun setBaseUrl(url: String) {
        prefs.edit().putString(KEY_BASE_URL, url).apply()
    }

    // ── Session State ─────────────────────────────────────────

    fun isSessionActive(): Boolean =
        prefs.getBoolean(KEY_SESSION_ACTIVE, false)

    fun setSessionActive(active: Boolean) {
        prefs.edit().putBoolean(KEY_SESSION_ACTIVE, active).apply()
    }

    // ── Login Tracking ────────────────────────────────────────

    fun getLastLoginTime(): Long =
        prefs.getLong(KEY_LAST_LOGIN_TIME, 0L)

    fun updateLastLoginTime() {
        prefs.edit().putLong(KEY_LAST_LOGIN_TIME, System.currentTimeMillis()).apply()
    }
}
