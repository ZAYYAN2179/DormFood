package com.zayyan.dormfood.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.zayyan.dormfood.R
import com.zayyan.dormfood.ui.viewmodel.AuthState
import com.zayyan.dormfood.ui.viewmodel.AuthViewModel
import com.zayyan.dormfood.ui.viewmodel.PesananViewModel
import com.zayyan.dormfood.ui.navigation.Screen
import com.zayyan.dormfood.ui.theme.Background
import com.zayyan.dormfood.ui.theme.DormRed

@Composable
fun LoginScreen(
    navController: NavHostController,
    viewModel: AuthViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    pesananViewModel: PesananViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    val authState by viewModel.authState.collectAsState()
    val focusManager = LocalFocusManager.current

    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.LoginSuccess -> {
                val loginState = authState as AuthState.LoginSuccess
                val destination = when (loginState.role) {
                    "mahasiswa" -> Screen.BerandaMahasiswaScreen.route
                    "warung" -> Screen.ProfilWarungScreen.route
                    else -> Screen.BerandaMahasiswaScreen.route
                }
                pesananViewModel.clearAndReload()

                navController.navigate(destination) {
                    popUpTo(Screen.LoginScreen.route) { inclusive = true }
                }
                viewModel.resetAuthState()
            }

            else -> {}
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Spacer(modifier = Modifier.height(48.dp))

        Box(
            modifier = Modifier
                .size(100.dp)
                .background(DormRed, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo_dorm_food),
                contentDescription = "Logo",
                modifier = Modifier.size(56.dp),
                contentScale = ContentScale.Fit
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Selamat Datang Kembali",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Masuk untuk melanjutkan.",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
        )

        Spacer(modifier = Modifier.height(32.dp))

        if (authState is AuthState.Loading) {
            CircularProgressIndicator(color = DormRed)
            Spacer(modifier = Modifier.height(16.dp))
        }

        if (authState is AuthState.Error) {
            Text(
                text = (authState as AuthState.Error).message,
                color = MaterialTheme.colorScheme.error,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        Text(
            text = "Email",
            modifier = Modifier.fillMaxWidth(),
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            modifier = Modifier.fillMaxWidth().testTag("inputEmail"),
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) }
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Kata Sandi",
            modifier = Modifier.fillMaxWidth(),
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            modifier = Modifier.fillMaxWidth().testTag("inputPassword"),
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    focusManager.clearFocus()
                    viewModel.login(email, password)
                }
            )
        )
        Spacer(modifier = Modifier.height(8.dp))

        TextButton(
            onClick = {
                // TODO: Implement forgot password
                navController.navigate(Screen.LandingPage.route)
            },
            modifier = Modifier.align(Alignment.End).testTag("btnForgotPassword")
        ) {
            Text(
                text = "Lupa Kata Sandi?",
                color = DormRed
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                viewModel.login(email, password)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("tombolMasuk"),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = DormRed
            ),
            enabled = authState !is AuthState.Loading
        ) {
            Text(
                text = "Masuk",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row {
            Text(text = "Belum punya akun? ")
            Text(
                text = "Daftar",
                color = DormRed,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable {
                    navController.navigate(Screen.RegisterScreen.route)
                }
            )
        }
    }
}