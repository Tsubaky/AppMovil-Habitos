package com.dam.apphabitos

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.dam.apphabitos.ui.theme.AppHabitosTheme

import android.content.Intent
import androidx.compose.material3.Button

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AppHabitosTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MainScreen(
                        modifier = Modifier.padding(innerPadding),
                        onRegisterClick = {
                            val intent = Intent(this, RegisterActivity::class.java)
                            startActivity(intent)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun MainScreen(modifier: Modifier = Modifier, onRegisterClick: () -> Unit) {
    Button(
        onClick = onRegisterClick,
        modifier = modifier
    ) {
        Text("Ir a Registro")
    }
}
