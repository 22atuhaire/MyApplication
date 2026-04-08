package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.api.RetrofitClient
import kotlinx.coroutines.*

class ApprovalPendingActivity : AppCompatActivity() {

    // Views
    private lateinit var tvName: TextView
    private lateinit var tvPhone: TextView
    private lateinit var tvStatus: TextView
    private lateinit var tvMessage: TextView
    private lateinit var btnBackToHome: Button

    // Data
    private val coroutineScope = CoroutineScope(Dispatchers.Main + Job())
    private var checkJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_approval_pending)

        initViews()
        loadData()
        setupClickListeners()
        startPeriodicStatusCheck()
    }

    private fun initViews() {
        tvName = findViewById(R.id.tvName)
        tvPhone = findViewById(R.id.tvPhone)
        tvStatus = findViewById(R.id.tvStatus)
        tvMessage = findViewById(R.id.tvMessage)
        btnBackToHome = findViewById(R.id.btnBackToHome)
    }

    private fun loadData() {
        // Get data from intent
        val name = intent.getStringExtra("name") ?: "Collector"
        val phone = intent.getStringExtra("phone") ?: ""

        // Set basic info immediately
        tvName.text = "${getString(R.string.name_label)} $name"
        tvPhone.text = "${getString(R.string.phone_label)} $phone"
        tvStatus.text = getString(R.string.pending_review)
        tvMessage.text = "Your application is being reviewed. You will be redirected to login once approved."
    }

    private fun startPeriodicStatusCheck() {
        val phone = intent.getStringExtra("phone") ?: return

        // Check status every 5 seconds
        checkJob = coroutineScope.launch {
            while (true) {
                delay(5000) // Wait 5 seconds
                checkApprovalStatus(phone)
            }
        }
    }

    private fun checkApprovalStatus(phone: String) {
        coroutineScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    RetrofitClient.apiService.checkCollectorStatus(phone).execute()
                }

                if (response.isSuccessful) {
                    val result = response.body()
                    if (result != null && result.success) {
                        val status = result.status ?: "pending"

                        withContext(Dispatchers.Main) {
                            when (status.lowercase()) {
                                "approved" -> {
                                    // Stop checking and route to login so token/session is created properly.
                                    checkJob?.cancel()

                                    Toast.makeText(
                                        this@ApprovalPendingActivity,
                                        "Account approved. Please log in.",
                                        Toast.LENGTH_LONG
                                    ).show()

                                    val intent = Intent(
                                        this@ApprovalPendingActivity,
                                        CollectorLoginActivity::class.java
                                    )
                                    intent.putExtra("phone", phone)
                                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                    startActivity(intent)
                                    finish()
                                }
                                "rejected" -> {
                                    // Stop checking
                                    checkJob?.cancel()

                                    // Show rejection message
                                    tvStatus.text = "Rejected"
                                    tvMessage.text = "Sorry, your application was not approved. Please contact support."
                                    Toast.makeText(
                                        this@ApprovalPendingActivity,
                                        "Application rejected",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                                else -> {
                                    // Still pending - update UI
                                    tvStatus.text = "Pending Review"
                                    tvMessage.text = "Still waiting for approval. You will be auto-logged in when approved."
                                }
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                // Silently fail - will retry in 5 seconds
            }
        }
    }

    private fun setupClickListeners() {
        btnBackToHome.setOnClickListener {
            // Stop checking before leaving
            checkJob?.cancel()

            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        checkJob?.cancel()
        coroutineScope.cancel()
    }
}