package com.example.data.remote

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.example.R
import com.example.data.model.*
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

class FirebaseManager(private val context: Context) {

    val auth: FirebaseAuth = FirebaseAuth.getInstance()

    val firestore: FirebaseFirestore by lazy {
        try {
            val dbId = context.getString(R.string.firestore_database_id)
            FirebaseFirestore.getInstance(FirebaseApp.getInstance(), dbId)
        } catch (e: Exception) {
            Log.e("FirebaseManager", "Failed to get named firestore database", e)
            FirebaseFirestore.getInstance()
        }
    }

    suspend fun syncBookingToFirestore(booking: Booking) {
        try {
            val data = hashMapOf(
                "id" to booking.id,
                "customerName" to booking.customerName,
                "customerPhone" to booking.customerPhone,
                "address" to booking.address,
                "area" to booking.area,
                "serviceName" to booking.serviceName,
                "units" to booking.units,
                "modelType" to booking.modelType,
                "issueNotes" to booking.issueNotes,
                "couponCode" to booking.couponCode,
                "discountAmount" to booking.discountAmount,
                "status" to booking.status,
                "technicianName" to booking.technicianName,
                "createdAt" to booking.createdAt
            )
            firestore.collection("bookings")
                .document(booking.id)
                .set(data, SetOptions.merge())
                .await()
            Log.d("FirebaseManager", "Booking synced to Firestore: ${booking.id}")
        } catch (e: Exception) {
            Log.w("FirebaseManager", "Failed to sync booking to Firestore", e)
        }
    }

    suspend fun syncUserToFirestore(user: User) {
        try {
            val data = hashMapOf(
                "phone" to user.phone,
                "name" to user.name,
                "address" to user.address,
                "area" to user.area,
                "role" to user.role,
                "createdAt" to user.createdAt
            )
            firestore.collection("users")
                .document(user.phone)
                .set(data, SetOptions.merge())
                .await()
            Log.d("FirebaseManager", "User synced to Firestore: ${user.phone}")
        } catch (e: Exception) {
            Log.w("FirebaseManager", "Failed to sync user to Firestore", e)
        }
    }

    suspend fun signInWithGoogle(
        activity: Activity,
        onSuccess: (String, String) -> Unit,
        onError: (String) -> Unit
    ) {
        try {
            val credentialManager = CredentialManager.create(activity)
            val serverClientId = activity.getString(R.string.default_web_client_id)

            val signInOption = GetSignInWithGoogleOption.Builder(serverClientId = serverClientId).build()
            val request = GetCredentialRequest.Builder()
                .addCredentialOption(signInOption)
                .build()

            val result = credentialManager.getCredential(activity, request)
            val credential = result.credential
            if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
                val authCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
                val authResult = auth.signInWithCredential(authCredential).await()
                val firebaseUser = authResult.user
                val displayName = firebaseUser?.displayName ?: "Google User"
                val phoneOrUid = firebaseUser?.phoneNumber ?: firebaseUser?.uid?.take(10) ?: "GoogleAuth"
                onSuccess(phoneOrUid, displayName)
            } else {
                onError("Unexpected credential format")
            }
        } catch (e: GetCredentialCancellationException) {
            Log.w("FirebaseManager", "Google Sign-In cancelled: ${e.message}", e)
            onError("Sign-In cancelled")
        } catch (e: Exception) {
            Log.e("FirebaseManager", "Google Sign-In failed", e)
            onError(e.localizedMessage ?: "Sign-In error")
        }
    }
}
