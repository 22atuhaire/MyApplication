package com.example.myapplication

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.toColorInt
import com.example.myapplication.api.RetrofitClient
import com.example.myapplication.models.CollectorInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ConfirmationActivity : AppCompatActivity() {

    private enum class SearchState {
        SEARCHING, FOUND, NO_MATCH, ERROR
    }

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
    private var searchState = SearchState.SEARCHING
    private var postId: Int = 0
    private var collectorPhone: String? = null
    private val coroutineScope = CoroutineScope(Dispatchers.Main + Job())
    private var pollingJob: Job? = null

    private companion object {
        const val POLL_INTERVAL_MS = 5000L
        const val MAX_POLL_ATTEMPTS = 12
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_confirmation)

        postId = intent.getIntExtra("post_id", 0)
        Log.d("Confirmation", "Post created with ID: $postId")

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
            when (searchState) {
                SearchState.FOUND -> {
                    Toast.makeText(this, "Collector confirmed!", Toast.LENGTH_SHORT).show()
                    finish()
                }
                SearchState.SEARCHING -> showCancelDialog()
                SearchState.NO_MATCH, SearchState.ERROR -> startSearching()
            }
        }

        // Call button
        btnCall.setOnClickListener {
            val phone = collectorPhone
            if (phone.isNullOrBlank()) {
                Toast.makeText(this, "Collector phone not available", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone")))
        }

        // Chat button
        btnChat.setOnClickListener {
            Toast.makeText(this, "Opening chat... (demo)", Toast.LENGTH_SHORT).show()
        }
    }

    private fun startSearching() {
        if (postId <= 0) {
            showErrorState("Missing post ID. Please repost and try again.")
            return
        }

        pollingJob?.cancel()
        showSearchingState(getString(R.string.searching_message))

        pollingJob = coroutineScope.launch {
            var attempts = 0

            while (isActive && attempts < MAX_POLL_ATTEMPTS && !isCollectorFound) {
                attempts++
                val found = checkCollectorMatchOnce()

                if (found) {
                    return@launch
                }

                val remaining = MAX_POLL_ATTEMPTS - attempts
                if (remaining > 0) {
                    showSearchingState("Looking for available collector nearby... ($remaining)")
                    delay(POLL_INTERVAL_MS)
                }
            }

            if (!isCollectorFound) {
                showNoCollectorState()
            }
        }
    }

    private suspend fun checkCollectorMatchOnce(): Boolean {
        return try {
            val response = withContext(Dispatchers.IO) {
                RetrofitClient.apiService.checkCollectorMatch(postId).execute()
            }

            if (!response.isSuccessful) {
                Log.e("Confirmation", "checkCollectorMatch HTTP ${response.code()}")
                return false
            }

            val body = response.body()
            val collector = body?.data?.collector

            if (collector != null) {
                showFoundState(collector)
                true
            } else {
                Log.d("Confirmation", "No collector yet for post $postId")
                false
            }
        } catch (e: Exception) {
            Log.e("Confirmation", "Collector match polling failed", e)
            showErrorState("Could not check nearby collectors. Tap Retry.")
            true
        }
    }

    private fun showSearchingState(message: String) {
        isCollectorFound = false
        searchState = SearchState.SEARCHING
        collectorPhone = null
        llSearching.visibility = View.VISIBLE
        llFound.visibility = View.GONE
        llContact.visibility = View.GONE
        tvSearchTitle.text = getString(R.string.searching)
        tvSearchMessage.text = message
        progressBar.visibility = View.VISIBLE

        // Bottom action: cancel while searching.
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

    private fun showFoundState(collector: CollectorInfo) {
        isCollectorFound = true
        searchState = SearchState.FOUND
        collectorPhone = collector.phone

        val collectorName = collector.name.ifBlank { "Collector" }
        val etaMinutes = collector.eta_minutes ?: 0

        tvFoundMessage.text = String.format(
            getString(R.string.collector_will_collect),
            collectorName
        )
        tvEta.text = String.format(getString(R.string.arriving_in), etaMinutes)

        llSearching.visibility = View.GONE
        llFound.visibility = View.VISIBLE
        llContact.visibility = View.VISIBLE

        btnAction.text = getString(R.string.ok_got_it)
        btnAction.backgroundTintList = android.content.res.ColorStateList.valueOf(
            "#2E7D32".toColorInt()
        )
        btnAction.setTextColor("#FFFFFF".toColorInt())

        (btnAction as com.google.android.material.button.MaterialButton).setStrokeColor(null)

        Toast.makeText(this, "Collector found nearby", Toast.LENGTH_SHORT).show()
    }

    private fun showNoCollectorState() {
        searchState = SearchState.NO_MATCH
        llSearching.visibility = View.VISIBLE
        llFound.visibility = View.GONE
        llContact.visibility = View.GONE

        tvSearchTitle.text = getString(R.string.searching)
        tvSearchMessage.text = getString(R.string.no_collectors_nearby)
        progressBar.visibility = View.GONE

        btnAction.text = getString(R.string.retry_search)
        btnAction.backgroundTintList = android.content.res.ColorStateList.valueOf(
            "#2E7D32".toColorInt()
        )
        btnAction.setTextColor("#FFFFFF".toColorInt())
        (btnAction as com.google.android.material.button.MaterialButton).setStrokeColor(null)
    }

    private fun showErrorState(message: String) {
        pollingJob?.cancel()
        searchState = SearchState.ERROR
        llSearching.visibility = View.VISIBLE
        llFound.visibility = View.GONE
        llContact.visibility = View.GONE

        tvSearchTitle.text = getString(R.string.searching)
        tvSearchMessage.text = message
        progressBar.visibility = View.GONE

        btnAction.text = getString(R.string.retry_search)
        btnAction.backgroundTintList = android.content.res.ColorStateList.valueOf(
            "#2E7D32".toColorInt()
        )
        btnAction.setTextColor("#FFFFFF".toColorInt())
        (btnAction as com.google.android.material.button.MaterialButton).setStrokeColor(null)
    }

    private fun showCancelDialog() {
        AlertDialog.Builder(this)
            .setTitle("Cancel Post")
            .setMessage(R.string.cancel_confirm)
            .setPositiveButton("Yes, Cancel") { _, _ ->
                pollingJob?.cancel()
                Toast.makeText(this, R.string.post_cancelled, Toast.LENGTH_SHORT).show()
                finish()
            }
            .setNegativeButton("No", null)
            .show()
    }

    override fun onDestroy() {
        super.onDestroy()
        pollingJob?.cancel()
        coroutineScope.cancel()
    }
}