package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
//import com.example.myapplication.R.string.account_pending

class CollectorLoginActivity : AppCompatActivity() {

    private lateinit var etPhone: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var tvForgotPassword: TextView
    private lateinit var tvRegisterLink: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_collector_login)

        initViews()
        setupTextWatchers()
        setupClickListeners()
    }

    private fun initViews() {
        etPhone = findViewById(R.id.etPhone)
        etPassword = findViewById(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLogin)
        tvForgotPassword = findViewById(R.id.tvForgotPassword)
        tvRegisterLink = findViewById(R.id.tvRegisterLink)
    }

    private fun setupTextWatchers() {
        val textWatcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                validateForm()
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        }

        etPhone.addTextChangedListener(textWatcher)
        etPassword.addTextChangedListener(textWatcher)
    }

    private fun validateForm() {
        val phone = etPhone.text.toString().trim()
        val password = etPassword.text.toString().trim()

        btnLogin.isEnabled = phone.isNotBlank() && password.isNotBlank()

        Log.d("LoginValidation", "Phone: '$phone', Password: '${if (password.isNotBlank()) "***" else ""}'")
        Log.d("LoginValidation", "Button enabled: ${btnLogin.isEnabled}")
    }

    private fun setupClickListeners() {
        btnLogin.setOnClickListener {
            attemptLogin()
        }

        tvForgotPassword.setOnClickListener {
            Toast.makeText(this, "Forgot Password - Coming Soon", Toast.LENGTH_SHORT).show()
        }

        tvRegisterLink.setOnClickListener {
            // Navigate back to Registration screen
            val intent = Intent(this, CollectorRegistrationActivity::class.java)
            startActivity(intent)
        }
    }

    private fun attemptLogin() {
        val phone = etPhone.text.toString().trim()
        val password = etPassword.text.toString().trim()

        Log.d("LoginAttempt", "Phone: $phone")

        if (phone.isNotBlank() && password.isNotBlank()) {
            // FOR DEVELOPMENT: Skip approval check, go directly to dashboard
            Toast.makeText(this, "Login successful! Welcome to dashboard", Toast.LENGTH_SHORT).show()

            // Navigate to Collector Dashboard
            val intent = Intent(this, CollectorDashboardActivity::class.java)
            intent.putExtra("collector_name", "John") // You can pass the name if you have it
            startActivity(intent)
            finish()

        } else {
            Toast.makeText(this, "Please enter phone and password", Toast.LENGTH_SHORT).show()
        }
    }
    private fun checkIfCollectorIsApproved(phone: String): Boolean {
        // SIMULATION - In real app, this would check a database

        // For demo purposes:
        // - Phone numbers containing "123" are approved (like +256 712 345 678)
        // - Others are still pending

        val approved = phone.contains("123")  // Just for demo!

        Log.d("LoginAttempt", "Phone: $phone - Approved: $approved")

        return approved
    }

}