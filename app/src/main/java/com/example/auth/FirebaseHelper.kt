package com.example.auth

import android.content.Context
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions

object FirebaseHelper {
    @Volatile
    private var initialized = false

    fun initialize(context: Context) {
        if (initialized) return
        synchronized(this) {
            if (initialized) return
            try {
                val appContext = context.applicationContext ?: context
                if (FirebaseApp.getApps(appContext).isEmpty()) {
                    // Try auto-initialization from google-services.json resources first
                    try {
                        FirebaseApp.initializeApp(appContext)
                    } catch (_: Exception) {
                    }

                    // Fallback to explicit FirebaseOptions if auto-init did not create the default app
                    if (FirebaseApp.getApps(appContext).isEmpty()) {
                        val options = FirebaseOptions.Builder()
                            .setApplicationId("1:587849588879:android:8143c38392b0e52f4c50fa")
                            .setProjectId("gen-lang-client-0275946414")
                            .setApiKey("AIzaSyAubabzAITvDiUkeUpQrs6htoDmOm2ImNk")
                            .setStorageBucket("gen-lang-client-0275946414.firebasestorage.app")
                            .setGcmSenderId("587849588879")
                            .build()
                        FirebaseApp.initializeApp(appContext, options)
                        Log.i("FirebaseHelper", "FirebaseApp initialized with explicit fallback options")
                    } else {
                        Log.i("FirebaseHelper", "FirebaseApp auto-initialized from resources")
                    }
                }
                initialized = true
            } catch (e: Exception) {
                Log.e("FirebaseHelper", "Firebase initialization caught error: ${e.message}", e)
            }
        }
    }
}
