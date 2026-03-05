package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class ProfileActivity : AppCompatActivity() {

    // Top Bar
    private lateinit var tvBack: TextView

    // Profile Info
    private lateinit var tvProfileName: TextView
    private lateinit var tvProfilePhone: TextView
    private lateinit var tvProfileRating: TextView
    private lateinit var tvMemberSince: TextView

    // Stats
    private lateinit var tvTotalJobs: TextView
    private lateinit var tvTotalKg: TextView
    private lateinit var tvTotalEarnings: TextView

    // Settings Options
    private lateinit var tvEditProfile: TextView
    private lateinit var tvNotifications: TextView
    private lateinit var tvLanguage: TextView
    private lateinit var tvHelpSupport: TextView
    private lateinit var tvTermsConditions: TextView

    // Logout
    private lateinit var btnLogout: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        initViews()
        loadProfileData()
        setupClickListeners()
    }

    private fun initViews() {
        // Top Bar
        tvBack = findViewById(R.id.tvBack)

        // Profile Info
        tvProfileName = findViewById(R.id.tvProfileName)
        tvProfilePhone = findViewById(R.id.tvProfilePhone)
        tvProfileRating = findViewById(R.id.tvProfileRating)
        tvMemberSince = findViewById(R.id.tvMemberSince)

        // Stats
        tvTotalJobs = findViewById(R.id.tvTotalJobs)
        tvTotalKg = findViewById(R.id.tvTotalKg)
        tvTotalEarnings = findViewById(R.id.tvTotalEarnings)

        // Settings Options
        tvEditProfile = findViewById(R.id.tvEditProfile)
        tvNotifications = findViewById(R.id.tvNotifications)
        tvLanguage = findViewById(R.id.tvLanguage)
        tvHelpSupport = findViewById(R.id.tvHelpSupport)
        tvTermsConditions = findViewById(R.id.tvTermsConditions)

        // Logout
        btnLogout = findViewById(R.id.btnLogout)
    }

    private fun loadProfileData() {
        // Get data from intent (passed from login/dashboard)
        val name = intent.getStringExtra("collector_name") ?: "John Doe"
        val phone = intent.getStringExtra("collector_phone") ?: "+256 712 345 678"

        // Set profile info
        tvProfileName.text = name
        tvProfilePhone.text = phone
        tvProfileRating.text = "⭐ 4.8"
        tvMemberSince.text = "📅 Member since Jan 2026"

        // Set stats (these would come from a real database)
        tvTotalJobs.text = "24"
        tvTotalKg.text = "156"
        tvTotalEarnings.text = "KES 24,960"
    }

    private fun setupClickListeners() {
        // Back button
        tvBack.setOnClickListener {
            finish()
        }

        // Settings options
        tvEditProfile.setOnClickListener {
            Toast.makeText(this, "Edit Profile - Coming Soon", Toast.LENGTH_SHORT).show()
        }

        tvNotifications.setOnClickListener {
            Toast.makeText(this, "Notifications Settings - Coming Soon", Toast.LENGTH_SHORT).show()
        }

        tvLanguage.setOnClickListener {
            Toast.makeText(this, "Language Settings - Coming Soon", Toast.LENGTH_SHORT).show()
        }

        tvHelpSupport.setOnClickListener {
            Toast.makeText(this, "Help & Support - Coming Soon", Toast.LENGTH_SHORT).show()
        }

        tvTermsConditions.setOnClickListener {
            Toast.makeText(this, "Terms & Conditions - Coming Soon", Toast.LENGTH_SHORT).show()
        }

        // Logout button
        btnLogout.setOnClickListener {
            showLogoutDialog()
        }
    }

    private fun showLogoutDialog() {
        AlertDialog.Builder(this)
            .setTitle(R.string.logout)
            .setMessage(R.string.logout_confirmation)
            .setPositiveButton(R.string.yes_logout) { _, _ ->
                performLogout()
            }
            .setNegativeButton(R.string.cancel) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun performLogout() {
        // Clear any session data here if needed

        Toast.makeText(this, R.string.logout_success, Toast.LENGTH_SHORT).show()

        // Navigate back to login screen
        val intent = Intent(this, CollectorLoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}