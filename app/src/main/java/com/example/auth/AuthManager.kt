package com.example.auth

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.example.R
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AuthManager(private val context: Context) {
    private val auth: FirebaseAuth? by lazy {
        try {
            FirebaseHelper.initialize(context)
            Firebase.auth
        } catch (e: Exception) {
            Log.w("AuthManager", "FirebaseAuth not available: ${e.message}")
            null
        }
    }

    private val credentialManager = try {
        CredentialManager.create(context)
    } catch (_: Exception) {
        null
    }

    private val _currentUser = MutableStateFlow<FirebaseUser?>(null)
    val currentUser = _currentUser.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        try {
            auth?.let { fbAuth ->
                _currentUser.value = fbAuth.currentUser
                fbAuth.addAuthStateListener { firebaseAuth ->
                    _currentUser.value = firebaseAuth.currentUser
                }
            }
        } catch (e: Exception) {
            Log.w("AuthManager", "Failed to register auth state listener: ${e.message}")
        }
    }

    fun signInWithGoogle(
        activity: Activity,
        scope: CoroutineScope,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val fbAuth = auth
        val credMgr = credentialManager
        if (fbAuth == null || credMgr == null) {
            onError("Authentication services are unavailable on this device.")
            return
        }

        val clientId = try {
            val resId = context.resources.getIdentifier("default_web_client_id", "string", context.packageName)
            if (resId != 0) context.getString(resId) else "587849588879-tj00eop7iob5duvjcfsmg2bkpns3jcgt.apps.googleusercontent.com"
        } catch (_: Exception) {
            "587849588879-tj00eop7iob5duvjcfsmg2bkpns3jcgt.apps.googleusercontent.com"
        }

        val signInOption = GetSignInWithGoogleOption.Builder(serverClientId = clientId).build()
        val request = GetCredentialRequest.Builder()
            .addCredentialOption(signInOption)
            .build()

        _isLoading.value = true

        scope.launch {
            try {
                val result = credMgr.getCredential(activity, request)
                val credential = result.credential
                if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
                    val authCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
                    fbAuth.signInWithCredential(authCredential).await()
                    _isLoading.value = false
                    onSuccess()
                } else {
                    _isLoading.value = false
                    onError("Unexpected credential format received.")
                }
            } catch (e: GetCredentialCancellationException) {
                Log.w("AuthManager", "Google Sign-In cancelled: ${e.message}")
                _isLoading.value = false
            } catch (e: Exception) {
                Log.e("AuthManager", "Google Sign-In failed", e)
                _isLoading.value = false
                onError(e.localizedMessage ?: "Google Sign-In failed")
            }
        }
    }

    fun signOut(scope: CoroutineScope, onComplete: () -> Unit) {
        try {
            auth?.signOut()
        } catch (_: Exception) {
        }

        scope.launch {
            try {
                credentialManager?.clearCredentialState(ClearCredentialStateRequest())
            } catch (e: Exception) {
                Log.e("AuthManager", "Failed to clear credential state", e)
            } finally {
                onComplete()
            }
        }
    }
}
