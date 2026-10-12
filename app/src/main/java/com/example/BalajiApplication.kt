package com.example

import android.app.Application
import android.util.Log
import com.example.data.remote.FirebaseManager

class BalajiApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        try {
            FirebaseManager.ensureInitialized(this)
            Log.d("BalajiApplication", "Firebase initialized successfully in Application")
        } catch (e: Exception) {
            Log.e("BalajiApplication", "Firebase initialization error: ${e.message}", e)
        }
    }
}
