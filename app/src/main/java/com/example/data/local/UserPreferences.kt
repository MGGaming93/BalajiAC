package com.example.data.local

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class UserPreferences(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("balaji_prefs", Context.MODE_PRIVATE)

    private val _isLoggedIn = MutableStateFlow(prefs.getString("user_phone", null) != null)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _currentUserPhone = MutableStateFlow(prefs.getString("user_phone", ""))
    val currentUserPhone: StateFlow<String?> = _currentUserPhone.asStateFlow()

    private val _currentUserName = MutableStateFlow(prefs.getString("user_name", ""))
    val currentUserName: StateFlow<String?> = _currentUserName.asStateFlow()

    private val _currentUserAddress = MutableStateFlow(prefs.getString("user_address", ""))
    val currentUserAddress: StateFlow<String?> = _currentUserAddress.asStateFlow()

    private val _currentUserArea = MutableStateFlow(prefs.getString("user_area", ""))
    val currentUserArea: StateFlow<String?> = _currentUserArea.asStateFlow()

    private val _appsScriptUrl = MutableStateFlow(
        prefs.getString("apps_script_url", "") ?: ""
    )
    val appsScriptUrl: StateFlow<String> = _appsScriptUrl.asStateFlow()

    fun saveUserSession(phone: String, name: String, address: String = "", area: String = "") {
        prefs.edit()
            .putString("user_phone", phone)
            .putString("user_name", name)
            .putString("user_address", address)
            .putString("user_area", area)
            .apply()
        _isLoggedIn.value = true
        _currentUserPhone.value = phone
        _currentUserName.value = name
        _currentUserAddress.value = address
        _currentUserArea.value = area
    }

    fun clearSession() {
        prefs.edit()
            .remove("user_phone")
            .remove("user_name")
            .remove("user_address")
            .remove("user_area")
            .apply()
        _isLoggedIn.value = false
        _currentUserPhone.value = null
        _currentUserName.value = null
        _currentUserAddress.value = null
        _currentUserArea.value = null
    }

    fun saveAppsScriptUrl(url: String) {
        prefs.edit().putString("apps_script_url", url).apply()
        _appsScriptUrl.value = url
    }

    fun getAppsScriptUrl(): String {
        return prefs.getString("apps_script_url", "") ?: ""
    }

    fun getSavedPhone(): String? = prefs.getString("user_phone", null)
    fun getSavedName(): String? = prefs.getString("user_name", null)
    fun getSavedAddress(): String? = prefs.getString("user_address", null)
    fun getSavedArea(): String? = prefs.getString("user_area", null)
}
