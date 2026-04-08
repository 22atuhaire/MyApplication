package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import com.example.myapplication.api.RetrofitClient
import com.example.myapplication.models.CollectorLoginRequest
import kotlinx.coroutines.*
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class CollectorLoginActivity : AppCompatActivity() {

    private lateinit var etPhone: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var tvForgotPassword: TextView
    private lateinit var tvRegisterLink: TextView
    private lateinit var progressBar: ProgressBar
    private val coroutineScope = CoroutineScope(Dispatchers.Main + Job())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_collector_login)

        initViews()
        prefillPhoneFromIntent()
        setupTextWatchers()
        setupClickListeners()
        checkApprovalStatusOnAppStart()
    }

    private fun initViews() {
        etPhone = findViewById(R.id.etPhone)
        etPassword = findViewById(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLogin)
        tvForgotPassword = findViewById(R.id.tvForgotPassword)
        tvRegisterLink = findViewById(R.id.tvRegisterLink)
        progressBar = findViewById(R.id.progressBar)
    }

    private fun prefillPhoneFromIntent() {
        val prefilledPhone = intent.getStringExtra("phone")?.trim().orEmpty()
        if (prefilledPhone.isNotEmpty()) {
            etPhone.setText(prefilledPhone)
            etPhone.setSelection(prefilledPhone.length)
        }
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

    private fun checkApprovalStatusOnAppStart() {
        val phone = etPhone.text.toString().trim()

        if (phone.isNotBlank()) {
            coroutineScope.launch {
                try {
                    val response = withContext(Dispatchers.IO) {
                        RetrofitClient.apiService.checkCollectorStatus(phone).execute()
                    }

                    if (response.isSuccessful) {
                        val result = response.body()
                        if (result != null && result.success) {
                            val status = result.status?.lowercase()

                            if (status == "approved") {
                                Toast.makeText(
                                    this@CollectorLoginActivity,
                                    "Your account has been approved!",
                                    Toast.LENGTH_SHORT
                                ).show()
                                Log.d("ApprovalCheck", "Collector $phone is approved")
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.e("ApprovalCheck", "Error checking approval status: ${e::class.simpleName}: ${e.message}")
                }
            }
        }
    }

    private fun attemptLogin() {
        val phone = etPhone.text.toString().trim()
        val password = etPassword.text.toString().trim()

        Log.d("LoginAttempt", "Phone: $phone")

        if (phone.isNotBlank() && password.isNotBlank()) {
            performLogin(phone, password)
        } else {
            Toast.makeText(this, "Please enter phone and password", Toast.LENGTH_SHORT).show()
        }
    }

    private fun performLogin(phone: String, password: String) {
        // Show loading
        progressBar.visibility = View.VISIBLE
        btnLogin.isEnabled = false

        coroutineScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    RetrofitClient.apiService
                        .collectorLogin(CollectorLoginRequest(phone, password))
                        .execute()
                }

                // Hide loading
                progressBar.visibility = View.GONE
                btnLogin.isEnabled = true

                if (response.isSuccessful) {
                    val result = response.body()
                    Log.d("LoginSuccess", "Response: $result")

                    if (result?.isSuccessfulLogin == true) {
                        // Prefer collector status from user object when available.
                        val resolvedStatus = result.user?.status?.lowercase() ?: result.status?.lowercase()

                        val prefs = getSharedPreferences("collector_prefs", MODE_PRIVATE)
                        prefs.edit()
                            .putString("collector_token", result.token)
                            .putString("collector_name", result.user?.name ?: "Collector")
                            .putString("collector_phone", phone)
                            .putString("collector_status", resolvedStatus)
                            .apply()

                        Toast.makeText(
                            this@CollectorLoginActivity,
                            "Login successful! Welcome ${result.user?.name ?: ""}",
                            Toast.LENGTH_SHORT
                        ).show()

                        val intent = Intent(this@CollectorLoginActivity, CollectorDashboardActivity::class.java)
                        intent.putExtra("collector_name", result.user?.name ?: "Collector")
                        intent.putExtra("collector_phone", phone)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        finish()
                        return@launch
                    }

                    // Login not successful: use status/message for user feedback.
                    when (result?.status?.lowercase()) {
                        "pending" -> {
                            Toast.makeText(
                                this@CollectorLoginActivity,
                                "Your account is pending admin approval. Please wait.",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                        "rejected" -> {
                            Toast.makeText(
                                this@CollectorLoginActivity,
                                "Your account has been rejected. Please contact admin.",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                        "wrong_password" -> {
                            Toast.makeText(
                                this@CollectorLoginActivity,
                                "Incorrect password. Please try again.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        else -> {
                            Toast.makeText(
                                this@CollectorLoginActivity,
                                result?.message ?: "Login failed. Invalid credentials.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                } else {
                    val errorCode = response.code()
                    val errorBody = response.errorBody()?.string() ?: "No error details"
                    Log.e("LoginError", "HTTP $errorCode: $errorBody")

                    // Try to parse error response if it's 422 validation error
                    if (errorCode == 422) {
                        Toast.makeText(
                            this@CollectorLoginActivity,
                            "Invalid phone or password format",
                            Toast.LENGTH_LONG
                        ).show()
                    } else {
                        val msg = when (errorCode) {
                            401 -> "Incorrect phone or password."
                            403 -> "Access denied. Your account may not be approved."
                            404 -> "Account not found. Please register first."
                            500 -> "Server error. Please try again later."
                            else -> "Login failed (Error $errorCode). Please try again."
                        }
                        Toast.makeText(this@CollectorLoginActivity, msg, Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: UnknownHostException) {
                progressBar.visibility = View.GONE
                btnLogin.isEnabled = true
                Log.e("LoginError", "UnknownHostException: ${e.message}")
                Toast.makeText(this@CollectorLoginActivity,
                    "Cannot reach server. Check your internet connection.", Toast.LENGTH_LONG).show()
            } catch (e: ConnectException) {
                progressBar.visibility = View.GONE
                btnLogin.isEnabled = true
                Log.e("LoginError", "ConnectException: ${e.message}")
                Toast.makeText(this@CollectorLoginActivity,
                    "Connection refused. Make sure the server is running at ${RetrofitClient.BASE_URL}", Toast.LENGTH_LONG).show()
            } catch (e: SocketTimeoutException) {
                progressBar.visibility = View.GONE
                btnLogin.isEnabled = true
                Log.e("LoginError", "SocketTimeoutException: ${e.message}")
                Toast.makeText(this@CollectorLoginActivity,
                    "Connection timed out. Server may be slow or unreachable.", Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                // Hide loading
                progressBar.visibility = View.GONE
                btnLogin.isEnabled = true
                val errMsg = "${e::class.simpleName}: ${e.message ?: "unknown error"}"
                Log.e("LoginError", "Error: $errMsg")
                Toast.makeText(this@CollectorLoginActivity,
                    "Error: $errMsg", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Re-check approval status each time user returns to login screen
        checkApprovalStatusOnAppStart()
    }

    override fun onDestroy() {
        super.onDestroy()
        coroutineScope.cancel()
    }
}