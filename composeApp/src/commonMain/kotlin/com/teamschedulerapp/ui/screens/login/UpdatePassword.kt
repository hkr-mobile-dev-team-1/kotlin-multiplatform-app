package com.teamschedulerapp.ui.screens.login

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.teamschedulerapp.data.SupabaseClientManager
import com.teamschedulerapp.navigation.Login
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun UpdatePasswordScreen(onPasswordUpdated: () -> Unit = {}) {
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val navigator = LocalNavigator.currentOrThrow

    Scaffold(
        topBar = { TopAppBar(title = { Text("Update Password") }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // --- New password input ---
            OutlinedTextField(
                value = newPassword,
                onValueChange = { newPassword = it },
                label = { Text("New Password") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = if (passwordVisible) "Hide password" else "Show password"
                        )
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // --- Confirm password input ---
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = { Text("Confirm New Password") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = if (passwordVisible) "Hide password" else "Show password"
                        )
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // --- Update button ---
            Button(
                onClick = {
                    if (newPassword.length < 8) {
                        message = "Password must be at least 8 characters long."
                        return@Button
                    }
                    if (newPassword != confirmPassword) {
                        message = "Passwords do not match."
                        return@Button
                    }

                    scope.launch {
                        isLoading = true
                        try {
                            val result = SupabaseClientManager.authRepository.updatePassword(newPassword)
                            if (result.isSuccess) {
                                isLoading = false
                                message = "Password updated successfully!"

                                delay(2000)

                                onPasswordUpdated()
                                navigator.replaceAll(Login)
                            } else {
                                message = result.exceptionOrNull()?.message ?: "An error occurred."
                                isLoading = false
                            }
                        } catch (e: Exception) {
                            message = e.message ?: "An unexpected error occurred."
                            isLoading = false
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading && newPassword.isNotBlank() && confirmPassword.isNotBlank()
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                } else {
                    Text("Update Password")
                }
            }

            // --- Feedback message ---
            message?.let {
                Text(
                    text = it,
                    color = if (it.contains("success", ignoreCase = true))
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 12.dp)
                )
            }
        }
    }
}
