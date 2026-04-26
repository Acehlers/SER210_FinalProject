package com.example.lfrivalsggiteration1.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lfrivalsggiteration1.ui.theme.LightSurface
import com.example.lfrivalsggiteration1.ui.theme.RivalsRed

@Composable
fun AuthManager(vm: MainViewModel, onAuthComplete: () -> Unit) {
    var isLoginScreen by remember { mutableStateOf(true) }
    if (isLoginScreen) {
        LoginView(vm = vm, onLoginSuccess = onAuthComplete, onNavigateToSignUp = { isLoginScreen = false })
    } else {
        SignUpView(onSignUpSuccess = { isLoginScreen = true }, onNavigateToLogin = { isLoginScreen = true })
    }
}

@Composable
fun LoginView(vm: MainViewModel, onLoginSuccess: () -> Unit, onNavigateToSignUp: () -> Unit) {
    var username by remember { mutableStateOf(vm.getSavedUsername()) }
    var password by remember { mutableStateOf("") }
    var rememberMe by remember { mutableStateOf(vm.isRememberMeEnabled()) }

    Column(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("LFRIVALS.GG", color = RivalsRed, fontSize = 42.sp, fontWeight = FontWeight.ExtraBold)
        Text("SIGN IN TO PLAY", color = Color.Gray, fontSize = 14.sp, modifier = Modifier.padding(bottom = 40.dp))

        AuthInput(value = username, onValueChange = { username = it }, label = "Username")
        Spacer(modifier = Modifier.height(16.dp))
        AuthInput(value = password, onValueChange = { password = it }, label = "Password", isPassword = true)

        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
            Checkbox(checked = rememberMe, onCheckedChange = { rememberMe = it }, colors = CheckboxDefaults.colors(checkedColor = RivalsRed))
            Text("Remember Password", color = MaterialTheme.colorScheme.tertiary, fontSize = 14.sp)
        }

        Button(
            onClick = { vm.setRememberMe(username, rememberMe); onLoginSuccess() },
            modifier = Modifier.fillMaxWidth().padding(top = 24.dp).height(56.dp),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = RivalsRed)
        ) {
            Text("LOGIN", fontWeight = FontWeight.Bold, color = Color.White)
        }

        Text(text = "Need an account? Create one", color = MaterialTheme.colorScheme.secondary, modifier = Modifier.padding(top = 24.dp).clickable { onNavigateToSignUp() })
    }
}

@Composable
fun SignUpView(onSignUpSuccess: () -> Unit, onNavigateToLogin: () -> Unit) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxSize().background(Color.White).padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("JOIN RIVALS", color = RivalsRed, fontSize = 36.sp, fontWeight = FontWeight.ExtraBold)
        Text("CREATE YOUR ACCOUNT", color = Color.Gray, modifier = Modifier.padding(bottom = 40.dp))

        AuthInput(value = username, onValueChange = { username = it }, label = "Username")
        Spacer(modifier = Modifier.height(16.dp))
        AuthInput(value = password, onValueChange = { password = it }, label = "Password", isPassword = true)

        Button(
            onClick = onSignUpSuccess,
            modifier = Modifier.fillMaxWidth().padding(top = 32.dp).height(56.dp),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = RivalsRed)
        ) {
            Text("SIGN UP", fontWeight = FontWeight.Bold, color = Color.White)
        }

        Text(text = "Already have an account? Log In", color = MaterialTheme.colorScheme.secondary, modifier = Modifier.padding(top = 24.dp).clickable { onNavigateToLogin() })
    }
}

@Composable
fun AuthInput(value: String, onValueChange: (String) -> Unit, label: String, isPassword: Boolean = false) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = RivalsRed,
            unfocusedBorderColor = Color.LightGray,
            focusedTextColor = Color.Black,
            unfocusedTextColor = Color.Black,
            focusedContainerColor = LightSurface,
            unfocusedContainerColor = LightSurface
        )
    )
}