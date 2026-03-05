package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.toColorInt

class ConfirmationActivity : AppCompatActivity() {

    // Views
    private lateinit var tvProgress: TextView
    private lateinit var tvSuccessHeader: TextView

    // Searching views
    private lateinit var llSearching: LinearLayout
    private lateinit var tvSearchIcon: TextView
    private lateinit var tvSearchTitle: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var tvSearchMessage: TextView

    // Found views
    private lateinit var llFound: LinearLayout
    private lateinit var tvFoundIcon: TextView
    private lateinit var tvFoundTitle: TextView
    private lateinit var tvFoundMessage: TextView
    private lateinit var tvEta: TextView

    // Summary views
    private lateinit var tvWasteTypes: TextView
    private lateinit var tvAddress: TextView
    private lateinit var tvPickupTime: TextView

    // Contact views
    private lateinit var llContact: LinearLayout
    private lateinit var btnCall: Button
    private lateinit var btnChat: Button

    // Bottom button
    private lateinit var btnAction: Button

    // Data
    private var isCollectorFound = false
    private val handler = Handler(Looper.getMainLooper())
    private var searchRunnable: Runnable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_confirmation)

        initViews()
        loadPostSummary()
        setupClickListeners()
        startSearching()
    }

    private fun initViews() {
        tvProgress = findViewById(R.id.tvProgress)
        tvSuccessHeader = findViewById(R.id.tvSuccessHeader)

        // Searching views
        llSearching = findViewById(R.id.llSearching)
        tvSearchIcon = findViewById(R.id.tvSearchIcon)
        tvSearchTitle = findViewById(R.id.tvSearchTitle)
        progressBar = findViewById(R.id.progressBar)
        tvSearchMessage = findViewById(R.id.tvSearchMessage)

        // Found views
        llFound = findViewById(R.id.llFound)
        tvFoundIcon = findViewById(R.id.tvFoundIcon)
        tvFoundTitle = findViewById(R.id.tvFoundTitle)
        tvFoundMessage = findViewById(R.id.tvFoundMessage)
        tvEta = findViewById(R.id.tvEta)

        // Summary views
        tvWasteTypes = findViewById(R.id.tvWasteTypes)
        tvAddress = findViewById(R.id.tvAddress)
        tvPickupTime = findViewById(R.id.tvPickupTime)

        // Contact views
        llContact = findViewById(R.id.llContact)
        btnCall = findViewById(R.id.btnCall)
        btnChat = findViewById(R.id.btnChat)

        // Bottom button
        btnAction = findViewById(R.id.btnAction)
    }

    private fun loadPostSummary() {
        // Get data from previous screen
        val selectedTypes = intent.getStringArrayExtra("selected_types") ?: arrayOf()
        val quantity = intent.getDoubleExtra("quantity", 0.0)
        val pickupTime = intent.getStringExtra("pickup_time") ?: "ASAP"
        val address = intent.getStringExtra("address") ?: "123 Main Street"

        // Format waste types
        val wasteTypesText = if (selectedTypes.isNotEmpty()) {
            selectedTypes.joinToString(", ") { type ->
                when (type) {
                    "cooked" -> "Cooked Food"
                    "vegetables" -> "Vegetables"
                    "bakery" -> "Bakery"
                    "meat" -> "Meat/Dairy"
                    "mixed" -> "Mixed/Other"
                    else -> type
                }
            }
        } else {
            "Mixed Food"
        }

        // Set summary
        tvWasteTypes.text = "📦 $wasteTypesText • $quantity kg"
        tvAddress.text = "📍 $address"
        tvPickupTime.text = "⏱️ $pickupTime"
    }

    private fun setupClickListeners() {
        // Bottom action button (Cancel or OK)
        btnAction.setOnClickListener {
            if (isCollectorFound) {
                // OK, GOT IT - Go to next screen or back to dashboard
                Toast.makeText(this, "Collector confirmed!", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                // Cancel post
                showCancelDialog()
            }
        }

        // Call button
        btnCall.setOnClickListener {
            Toast.makeText(this, "Calling collector... (demo)", Toast.LENGTH_SHORT).show()
        }

        // Chat button
        btnChat.setOnClickListener {
            Toast.makeText(this, "Opening chat... (demo)", Toast.LENGTH_SHORT).show()
        }
    }

    private fun startSearching() {
        // Show searching state
        showSearchingState()

        // Simulate search for 5 seconds then find collector
        searchRunnable = Runnable {
            findCollector()
        }
        handler.postDelayed(searchRunnable!!, 5000)
    }

    private fun showSearchingState() {
        isCollectorFound = false
        llSearching.visibility = android.view.View.VISIBLE
        llFound.visibility = android.view.View.GONE
        llContact.visibility = android.view.View.GONE

        // Change bottom button to Cancel
        btnAction.text = getString(R.string.cancel_post)
        btnAction.backgroundTintList = android.content.res.ColorStateList.valueOf(
            "#FFFFFF".toColorInt()
        )
        btnAction.setTextColor("#FF4444".toColorInt())

        // Fix: Use setStrokeColor method
        (btnAction as com.google.android.material.button.MaterialButton).setStrokeColor(
            android.content.res.ColorStateList.valueOf(
                "#FF4444".toColorInt()
            )
        )
    }

    private fun findCollector() {
        // Simulate finding a collector
        isCollectorFound = true

        // Sample collector data
        val collectorName = "John Doe"
        val etaMinutes = 8

        // Update found views
        tvFoundMessage.text = String.format(
            getString(R.string.collector_will_collect),
            collectorName
        )
        tvEta.text = String.format(getString(R.string.arriving_in), etaMinutes)

        // Show found state
        llSearching.visibility = android.view.View.GONE
        llFound.visibility = android.view.View.VISIBLE
        llContact.visibility = android.view.View.VISIBLE

        // Change bottom button to OK, GOT IT
        btnAction.text = getString(R.string.ok_got_it)
        btnAction.backgroundTintList = android.content.res.ColorStateList.valueOf(
            "#2E7D32".toColorInt()
        )
        btnAction.setTextColor("#FFFFFF".toColorInt())

        // Fix: Remove stroke
        (btnAction as com.google.android.material.button.MaterialButton).setStrokeColor(null)

        // Show success toast
        Toast.makeText(this, "Collector found! 🚴", Toast.LENGTH_SHORT).show()
    }

    private fun showCancelDialog() {
        AlertDialog.Builder(this)
            .setTitle("Cancel Post")
            .setMessage(R.string.cancel_confirm)
            .setPositiveButton("Yes, Cancel") { _, _ ->
                // Cancel the search
                handler.removeCallbacks(searchRunnable!!)
                Toast.makeText(this, R.string.post_cancelled, Toast.LENGTH_SHORT).show()
                finish()
            }
            .setNegativeButton("No", null)
            .show()
    }

    override fun onDestroy() {
        super.onDestroy()
        // Remove callbacks to prevent memory leaks
        searchRunnable?.let { handler.removeCallbacks(it) }
    }
}