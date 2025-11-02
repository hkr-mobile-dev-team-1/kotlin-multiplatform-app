package com.teamschedulerapp.ui.screens.login

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.LocalNavigator
import com.teamschedulerapp.data.SupabaseClientManager
import com.teamschedulerapp.navigation.UpdatePassword
import io.github.jan.supabase.auth.OtpType
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.status.SessionSource
import io.github.jan.supabase.auth.user.UserSession
import kotlinx.coroutines.launch
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
@Composable
fun ResetPasswordScreen(onNavigateBack: () -> Unit) {
    var email by remember { mutableStateOf("") }
    var message by remember { mutableStateOf<String?>(null) }
    var deeplink by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val navigator = LocalNavigator.current

    Scaffold(
        topBar = { TopAppBar(title = { Text("Reset Password") }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            // --- Email input ---
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // --- Send reset link ---
            Button(
                onClick = {
                    scope.launch {
                        try {
                            val result = SupabaseClientManager.authRepository
                                .resetPasswordForEmail(email)
                            message = if (result.isSuccess) {
                                "Password reset link sent to your email."
                            } else {
                                result.exceptionOrNull()?.message ?: "An error occurred."
                            }
                        } catch (e: Exception) {
                            message = e.message ?: "An unexpected error occurred."
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Send Reset Link")
            }

            message?.let {
                Text(
                    text = it,
                    color = if (!it.contains("error"))
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // --- Manual deeplink test input ---
            OutlinedTextField(
                value = deeplink,
                onValueChange = { deeplink = it },
                label = { Text("Paste deeplink here (for testing)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // --- Verify deeplink manually ---
            Button(
                onClick = {
                    scope.launch {
                        try {
                            // ---- Helper: parse params from ?query and #fragment ----
                            fun parseParams(paramString: String?): Map<String, String> {
                                if (paramString.isNullOrBlank()) return emptyMap()
                                return paramString.split("&").mapNotNull {
                                    val parts = it.split("=")
                                    if (parts.size == 2) parts[0] to parts[1] else null
                                }.toMap()
                            }

                            val queryIndex = deeplink.indexOf("?")
                            val fragmentIndex = deeplink.indexOf("#")

                            val queryString = if (queryIndex != -1) {
                                val end = if (fragmentIndex != -1) fragmentIndex else deeplink.length
                                deeplink.substring(queryIndex + 1, end)
                            } else null

                            val fragmentString = if (fragmentIndex != -1) {
                                deeplink.substring(fragmentIndex + 1)
                            } else null

                            val allParams = parseParams(queryString) + parseParams(fragmentString)
                            val auth = SupabaseClientManager.client.auth

                            // ---- Case 1: verify OTP token (for email recovery links) ----
                            if (allParams.containsKey("token")) {
                                val token = allParams["token"]!!
                                auth.verifyEmailOtp(
                                    type = OtpType.Email.RECOVERY,
                                    token = token,
                                    email = email
                                )
                                message = "Email OTP verified — proceed to update password."
                                navigator?.push(UpdatePassword)
                                return@launch
                            }

                            // ---- Case 2: import session (for password recovery callback) ----
                            val access = allParams["access_token"]
                            val refresh = allParams["refresh_token"]

                            if (!access.isNullOrBlank() && !refresh.isNullOrBlank()) {
                                try {
                                    val session = UserSession(
                                        accessToken = access,
                                        refreshToken = refresh,
                                        expiresIn = 3600,            // 1 hour, safe default
                                        tokenType = "bearer",        // standard for Supabase tokens
                                        user = null
                                    )

                                    auth.importSession(
                                        session = session,
                                        autoRefresh = true,
                                        source = SessionSource.External  // optional but clearer
                                    )

                                    val currentSession = auth.currentSessionOrNull()
                                    if (currentSession != null) {
                                        message = "Session imported and verified — proceed to update password."
                                        navigator?.push(UpdatePassword)
                                    } else {
                                        message = "Session import attempted, but no active session found."
                                    }

                                } catch (e: Exception) {
                                    message = "Failed to import session: ${e.message}"
                                }
                                return@launch
                            }

                            // ---- Fallback: invalid link ----
                            message = "No valid token found in deeplink."

                        } catch (e: Exception) {
                            message = "Failed to process deeplink: ${e.message}"
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Manual Deeplink Verification")
            }
        }
    }
}
