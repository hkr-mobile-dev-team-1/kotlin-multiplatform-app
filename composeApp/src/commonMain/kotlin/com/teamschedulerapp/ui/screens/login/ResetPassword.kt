package com.teamschedulerapp.ui.screens.login

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.teamschedulerapp.data.SupabaseClientManager
import kotlinx.coroutines.launch

@Composable
fun ResetPasswordScreen(onNavigateBack: () -> Unit) {
    var email by remember { mutableStateOf("") }
    var message by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

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
        }
    }
}
