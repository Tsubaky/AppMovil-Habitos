package com.dam.apphabitos

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val etUsername = findViewById<EditText>(R.id.etUsername)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val btnGoRegister = findViewById<Button>(R.id.btnGoRegister)

        // POR AHORA: aceptar cualquier usuario/contraseña y navegar a HomeActivity
        btnLogin.setOnClickListener {
            // Si quieres pasar el username a la HomeActivity (para mostrar "Alex Turner" dinámicamente)
            val intent = Intent(this, HomeActivity::class.java)
            intent.putExtra("username", etUsername.text.toString())
            startActivity(intent)
            finish()
        }

        btnGoRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }
}
