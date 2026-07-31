package com.example.testing1.screens.auth

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.CustomCredential
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.testing1.R
import kotlinx.coroutines.launch

@Composable
fun LoginRoute(
    onNavigateToSignUp: () -> Unit,
    onLoginSuccess: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.isAuthenticated) {
        if (uiState.isAuthenticated) {
            onLoginSuccess()
        }
    }

    LoginScreen(
        uiState = uiState,
        onEmailChange = viewModel::onEmailChange,
        onPasswordChange = viewModel::onPasswordChange,
        onLoginClick = viewModel::login,
        onSignUpClick = onNavigateToSignUp,
        onGoogleLoginClick = { idToken, name, email, avatarUrl ->
            viewModel.onGoogleSignIn(idToken, name, email, avatarUrl)
        },
        onAuthError = viewModel::onError
    )
}

@Composable
fun LoginScreen(
    uiState: AuthUiState,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onLoginClick: () -> Unit,
    onSignUpClick: () -> Unit,
    onGoogleLoginClick: (String, String?, String?, String?) -> Unit,
    onAuthError: (String) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val credentialManager = CredentialManager.create(context)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(90.dp)
                .clip(CircleShape)
                .background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_app_logo),
                contentDescription = "Coffee App Logo",
                modifier = Modifier.fillMaxSize()
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Welcome Back",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Sign in to continue your coffee journey",
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = uiState.email,
            onValueChange = onEmailChange,
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = uiState.password,
            onValueChange = onPasswordChange,
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onLoginClick,
            modifier = Modifier.fillMaxWidth(),
            enabled = !uiState.isLoading
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = MaterialTheme.colorScheme.onPrimary)
            } else {
                Text("Login")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedButton(
            onClick = {
                val googleIdOption = GetSignInWithGoogleOption.Builder(com.example.testing1.util.AuthConstants.GOOGLE_WEB_CLIENT_ID)
                    .build()

                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOption)
                    .build()

                coroutineScope.launch {
                    Log.d("LoginScreen", "Starting Google Sign-In request...")
                    try {
                        val result = credentialManager.getCredential(context, request)
                        val credential = result.credential
                        Log.d("LoginScreen", "Received credential type: ${credential.type}")
                        
                        if (credential is CustomCredential && 
                            credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                            try {
                                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                                val detectedEmail = googleIdTokenCredential.id.takeIf { it.contains("@") }
                                Log.d("LoginScreen", "Parsed Google ID Token. Name: ${googleIdTokenCredential.displayName}, ID: ${googleIdTokenCredential.id}")
                                onGoogleLoginClick(
                                    googleIdTokenCredential.idToken,
                                    googleIdTokenCredential.displayName,
                                    detectedEmail,
                                    googleIdTokenCredential.profilePictureUri?.toString()
                                )
                            } catch (e: GoogleIdTokenParsingException) {
                                Log.e("LoginScreen", "Error parsing Google ID Token: ${e.message}")
                            }
                        } else {
                            Log.w("LoginScreen", "Unexpected credential type: ${credential.type}")
                        }
                    } catch (e: Exception) {
                        Log.e("LoginScreen", "Google Sign-In failed: ${e.message}", e)
                        onAuthError(e.message ?: "Google Sign-In failed. Check internet or configuration.")
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Sign in with Google")
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Don't have an account?")
            TextButton(onClick = onSignUpClick) {
                Text("Sign Up", fontWeight = FontWeight.Bold)
            }
        }

        uiState.successMessage?.let { message ->
            Text(
                text = message,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 16.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }

        if (uiState.error != null) {
            Text(
                text = uiState.error,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 16.dp)
            )
        }
    }
}
