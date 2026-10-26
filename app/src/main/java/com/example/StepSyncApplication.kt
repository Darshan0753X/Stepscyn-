package com.example

import android.app.Application
import android.util.Log
import com.example.auth.FirebaseHelper
import com.google.firebase.FirebaseApp

class StepSyncApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        try {
            // Guarantee Firebase is initialized at process start before any Activity runs
            FirebaseHelper.initialize(this)
            Log.i("StepSyncApplication", "Firebase successfully initialized in Application.onCreate")
        } catch (e: Exception) {
            Log.e("StepSyncApplication", "Failed to initialize Firebase in Application: ${e.message}", e)
        }
    }
}
