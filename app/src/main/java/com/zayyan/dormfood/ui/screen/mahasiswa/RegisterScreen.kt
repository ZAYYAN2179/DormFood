package com.zayyan.dormfood.ui.screen.mahasiswa

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.zayyan.dormfood.ui.model.AuthState
import com.zayyan.dormfood.ui.model.AuthViewModel
import com.zayyan.dormfood.ui.navigation.Screen
import com.zayyan.dormfood.ui.theme.Background
import com.zayyan.dormfood.ui.theme.DormRed

@Composable
fun RegisterScreen(
    navController: NavHostController,
    viewModel: AuthViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    var name by remember { mutableStateOf("") }
    var building by remember { mutableStateOf("") }
    var roomNumber by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    val authState by viewModel.authState.collectAsState()
    val focusManager = LocalFocusManager.current

    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.Success -> {
                navController.navigate(Screen.LoginScreen.route) {
                    popUpTo(Screen.RegisterScreen.route) { inclusive = true }
                }
            }

            else -> {}
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .verticalScroll(rememberScrollState())
            .imePadding()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(40.dp))

        Text(
            text = "Buat Akun Baru",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(32.dp))

        if (authState is AuthState.Loading) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(16.dp))
        }

        if (authState is AuthState.Error) {
            Text(
                text = (authState as AuthState.Error).message,
                color = MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        RegisterField(
            label = "Nama",
            value = name,
            onValueChange = { name = it },
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) }
            )
        )

        RegisterField(
            label = "Gedung",
            value = building,
            onValueChange = { building = it },
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) }
            )
        )

        RegisterField(
            label = "Nomor kamar",
            value = roomNumber,
            onValueChange = { roomNumber = it },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) }
            )
        )

        RegisterField(
            label = "Email",
            value = email,
            onValueChange = { email = it },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) }
            )
        )

        RegisterPasswordField(
            label = "Kata Sandi",
            value = password,
            onValueChange = { password = it },
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) }
            )
        )

        RegisterPasswordField(
            label = "Konfirmasi Kata Sandi",
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    focusManager.clearFocus()
                }
            )
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                viewModel.register(
                    name = name,
                    building = building,
                    roomNumber = roomNumber,
                    email = email,
                    password = password,
                    confirmPassword = confirmPassword
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = DormRed
            ),
            enabled = authState !is AuthState.Loading
        ) {
            Text("Daftar", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Sudah punya akun? ")
            Text(
                text = "Masuk",
                color = DormRed,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable {
                    navController.navigate(Screen.LoginScreen.route)
                }
            )
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun RegisterField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default
) {
    Text(
        text = label,
        modifier = Modifier.fillMaxWidth(),
        fontWeight = FontWeight.Medium
    )
    Spacer(modifier = Modifier.height(8.dp))
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        singleLine = true,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions
    )
    Spacer(modifier = Modifier.height(16.dp))
}

@Composable
private fun RegisterPasswordField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default
) {
    Text(
        text = label,
        modifier = Modifier.fillMaxWidth(),
        fontWeight = FontWeight.Medium
    )
    Spacer(modifier = Modifier.height(8.dp))
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        singleLine = true,
        visualTransformation = PasswordVisualTransformation(),
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions
    )
    Spacer(modifier = Modifier.height(16.dp))
}