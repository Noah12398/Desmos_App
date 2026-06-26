package com.noah.desmos.auth.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.noah.desmos.navigation.Screen

@Composable
fun CompleteProfileScreen(
    navController: NavController,
    viewModel: AuthViewModel
) {
    // Navigate to home when profile is completed (loggedInUser is set)
    LaunchedEffect(viewModel.loggedInUser) {
        if (viewModel.loggedInUser != null) {
            navController.navigate(Screen.Home.route) {
                popUpTo(Screen.Login.route) { inclusive = true }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Complete Profile",
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = viewModel.name,
            onValueChange = { viewModel.onNameChanged(it) },
            label = { Text("Enter Name") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(20.dp))
        Button(
            onClick = { viewModel.register() },
            modifier = Modifier.fillMaxWidth(),
            enabled = !viewModel.loading
        ) {
            Text("Submit")
        }
        if (viewModel.loading) {
            Spacer(modifier = Modifier.height(20.dp))
            CircularProgressIndicator()
        }
        viewModel.errorMessage?.let { msg ->
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = msg, color = MaterialTheme.colorScheme.error)
        }
        Spacer(modifier = Modifier.height(24.dp))
        TextButton(onClick = { navController.popBackStack() }) {
            Text("Back to Login")
        }
    }
}
