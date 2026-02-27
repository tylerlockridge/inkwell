package com.obsidiancapture.auth

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GoogleSignInManager @Inject constructor(
    @ApplicationContext private val appContext: Context,
) {
    /**
     * Launch the Google Sign-In bottom sheet and return the ID token.
     * The caller must pass an Activity context for the credential picker UI.
     *
     * @param activityContext An Activity context (required for the system bottom sheet)
     * @param webClientId The Google Cloud web client ID (from server config)
     * @return Result containing the ID token string on success
     */
    suspend fun signIn(activityContext: Context, webClientId: String): Result<String> {
        return try {
            val credentialManager = CredentialManager.create(appContext)

            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(webClientId)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val result = credentialManager.getCredential(activityContext, request)
            val credential = result.credential
            val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)

            Result.success(googleIdTokenCredential.idToken)
        } catch (_: GetCredentialCancellationException) {
            Result.failure(Exception("Sign-in cancelled"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
