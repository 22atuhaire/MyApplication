package com.example.myapplication

import android.content.Intent
import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.myapplication.api.RetrofitClient
import com.example.myapplication.models.AvailableJob
import com.example.myapplication.models.toAvailableJob
import kotlinx.coroutines.*
import java.util.Locale

class CollectorDashboardActivity : AppCompatActivity() {

    // Top Bar
    private lateinit var tvOnlineStatus: TextView

    // Welcome
    private lateinit var tvCollectorName: TextView

    // Summary Stats
    private lateinit var tvJobsCompleted: TextView
    private lateinit var tvEarningsToday: TextView
    private lateinit var tvRating: TextView

    // Jobs Container
    private lateinit var llJobsContainer: LinearLayout

    // Bottom Navigation
    private lateinit var navHome: LinearLayout
    private lateinit var navJobs: LinearLayout
    private lateinit var navEarnings: LinearLayout
    private lateinit var navProfile: LinearLayout

    // UI state
    private var isOnline = true
    private lateinit var authToken: String
    private val coroutineScope = CoroutineScope(Dispatchers.Main + Job())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            setContentView(R.layout.activity_collector_dashboard)
            Log.d("Dashboard", "Layout set successfully")

            initViews()
            Log.d("Dashboard", "Views initialized")

            // Get token from SharedPreferences
            val prefs = getSharedPreferences("collector_prefs", MODE_PRIVATE)
            authToken = prefs.getString("collector_token", "") ?: ""
            Log.d("Dashboard", "Token retrieved: ${authToken.take(10)}...")

            if (authToken.isEmpty()) {
                // No token - go back to login
                Toast.makeText(this, "Session expired. Please login again.", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, CollectorLoginActivity::class.java))
                finish()
                return
            }

            loadCollectorInfo()
            Log.d("Dashboard", "Collector info loaded")
            loadDashboardStats()
            Log.d("Dashboard", "Dashboard stats loaded")
            loadAvailableJobs()
            Log.d("Dashboard", "Available jobs loaded")
            setupClickListeners()
            Log.d("Dashboard", "Click listeners set up")
        } catch (e: Exception) {
            Log.e("Dashboard", "Error in onCreate: ${e.message}", e)
            Toast.makeText(this, "Dashboard Error: ${e.message}", Toast.LENGTH_LONG).show()
            e.printStackTrace()
            finish()
        }
    }

    private fun initViews() {
        try {
            // Top Bar
            tvOnlineStatus = findViewById(R.id.tvOnlineStatus) ?: throw Exception("tvOnlineStatus not found")
            Log.d("Dashboard", "tvOnlineStatus found")

            // Welcome
            tvCollectorName = findViewById(R.id.tvCollectorName) ?: throw Exception("tvCollectorName not found")
            Log.d("Dashboard", "tvCollectorName found")

            // Summary Stats
            tvJobsCompleted = findViewById(R.id.tvJobsCompleted) ?: throw Exception("tvJobsCompleted not found")
            tvEarningsToday = findViewById(R.id.tvEarningsToday) ?: throw Exception("tvEarningsToday not found")
            tvRating = findViewById(R.id.tvRating) ?: throw Exception("tvRating not found")
            Log.d("Dashboard", "Summary stats views found")

            // Jobs Container
            llJobsContainer = findViewById(R.id.llJobsContainer) ?: throw Exception("llJobsContainer not found")
            Log.d("Dashboard", "llJobsContainer found")

            // Bottom Navigation
            navHome = findViewById(R.id.navHome) ?: throw Exception("navHome not found")
            navJobs = findViewById(R.id.navJobs) ?: throw Exception("navJobs not found")
            navEarnings = findViewById(R.id.navEarnings) ?: throw Exception("navEarnings not found")
            navProfile = findViewById(R.id.navProfile) ?: throw Exception("navProfile not found")
            Log.d("Dashboard", "Bottom navigation views found")
        } catch (e: Exception) {
            Log.e("Dashboard", "Error finding view: ${e.message}")
            throw e
        }
    }

    private fun loadCollectorInfo() {
        val prefs = getSharedPreferences("collector_prefs", MODE_PRIVATE)
        val name = intent.getStringExtra("collector_name")
            ?: prefs.getString("collector_name", "Collector")
            ?: "Collector"
        tvCollectorName.text = name
    }

    private fun loadDashboardStats() {
        coroutineScope.launch {
            try {
                val apiService = RetrofitClient.getAuthApiService(authToken)
                val response = withContext(Dispatchers.IO) {
                    apiService.getDashboardStats().execute()
                }

                if (response.isSuccessful) {
                    val result = response.body()
                    if (result != null && result.success) {
                        withContext(Dispatchers.Main) {
                            // Update UI with real data
                            tvJobsCompleted.text = result.data.todayJobs.toString()
                            tvEarningsToday.text = getString(
                                R.string.dashboard_earnings_today,
                                result.data.todayEarnings.toString()
                            )
                            tvRating.text = String.format(Locale.US, "%.1f", result.data.rating)
                        }
                    } else {
                        Log.e("Dashboard", "Stats response invalid: body=$result")
                        withContext(Dispatchers.Main) {
                            showStatsUnavailable()
                        }
                    }
                } else {
                    Log.e("Dashboard", "Error loading stats: ${response.code()}")
                    withContext(Dispatchers.Main) {
                        showStatsUnavailable()
                    }
                }
            } catch (e: Exception) {
                Log.e("Dashboard", "Exception loading stats", e)
                withContext(Dispatchers.Main) {
                    showStatsUnavailable()
                }
            }
        }
    }

    private fun loadAvailableJobs() {
        if (!isOnline) return

        coroutineScope.launch {
            try {
                val apiService = RetrofitClient.getAuthApiService(authToken)
                val donorPostsResponse = withContext(Dispatchers.IO) {
                    apiService.getAvailableDonorPosts().execute()
                }

                if (donorPostsResponse.isSuccessful) {
                    val posts = donorPostsResponse.body()?.data.orEmpty()
                    Log.d("Dashboard", "Donor posts success: count=${posts.size}")
                    val jobs = posts.map { it.toAvailableJob() }
                    if (jobs.isNotEmpty()) {
                        withContext(Dispatchers.Main) {
                            llJobsContainer.removeAllViews()
                            jobs.forEach { job ->
                                addRealJobCard(job)
                            }
                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            showNoJobsMessage()
                        }
                    }
                } else {
                    val donorPostsError = donorPostsResponse.errorBody()?.string() ?: "No error body"
                    Log.e(
                        "Dashboard",
                        "Donor posts endpoint failed: HTTP ${donorPostsResponse.code()} body=$donorPostsError"
                    )

                    // Fallback to legacy collector jobs endpoint to avoid empty dashboard during rollout.
                    val legacyResponse = withContext(Dispatchers.IO) {
                        apiService.getAvailableJobs().execute()
                    }

                    if (legacyResponse.isSuccessful) {
                        val jobs = legacyResponse.body()
                        withContext(Dispatchers.Main) {
                            llJobsContainer.removeAllViews()
                            if (jobs.isNullOrEmpty()) {
                                showNoJobsMessage()
                            } else {
                                jobs.forEach { addRealJobCard(it) }
                            }
                        }
                    } else {
                        val legacyError = legacyResponse.errorBody()?.string() ?: "No error body"
                        Log.e(
                            "Dashboard",
                            "Legacy jobs endpoint failed: HTTP ${legacyResponse.code()} body=$legacyError"
                        )
                        withContext(Dispatchers.Main) {
                            showJobsLoadError()
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("Dashboard", "Exception loading jobs", e)
                withContext(Dispatchers.Main) {
                    showJobsLoadError()
                }
            }
        }
    }

    private fun addRealJobCard(job: AvailableJob) {
        val jobCard = layoutInflater.inflate(R.layout.item_job_card, llJobsContainer, false)

        val tvFoodType = jobCard.findViewById<TextView>(R.id.tvFoodType)
        val tvDistance = jobCard.findViewById<TextView>(R.id.tvDistance)
        val tvTimeAgo = jobCard.findViewById<TextView>(R.id.tvTimeAgo)
        val btnView = jobCard.findViewById<TextView>(R.id.btnView)
        val tvFoodIcon = jobCard.findViewById<TextView>(R.id.tvFoodIcon)

        tvFoodType.text = job.foodType
        val distanceLabel = job.distance?.let { "${it} km" } ?: "Distance N/A"
        tvDistance.text = getString(
            R.string.available_job_distance_quantity,
            distanceLabel,
            job.quantity.toString()
        )
        tvTimeAgo.text = getString(R.string.posted_prefix, job.timeAgo)

        tvFoodIcon.text = when {
            job.foodType.contains("Mixed") -> "🍚"
            job.foodType.contains("Vegetables") -> "🥬"
            job.foodType.contains("Bakery") -> "🍞"
            job.foodType.contains("Meat") -> "🥩"
            else -> "🍲"
        }

        btnView.setOnClickListener {
            val intent = Intent(this, JobDetailsActivity::class.java)
            intent.putExtra("job_id", job.id)
            intent.putExtra("food_type", job.foodType)
            intent.putExtra("quantity", "${job.quantity} kg")
            intent.putExtra("distance", distanceLabel)
            intent.putExtra("time_ago", job.timeAgo)
            intent.putExtra("donor_name", job.donorName)
            intent.putExtra("donor_rating", job.donorRating.toString())
            intent.putExtra("address", job.address)
            intent.putExtra("instructions", job.instructions ?: "")
            intent.putExtra("city", "Kampala, Uganda")
            startActivity(intent)
        }

        llJobsContainer.addView(jobCard)
    }

    private fun showNoJobsMessage() {
        llJobsContainer.removeAllViews()
        val tvNoJobs = TextView(this)
        tvNoJobs.text = getString(R.string.jobs_no_nearby)
        tvNoJobs.textSize = 14f
        tvNoJobs.setTextColor(ContextCompat.getColor(this, R.color.gray_dark))
        tvNoJobs.gravity = android.view.Gravity.CENTER
        tvNoJobs.setPadding(32, 32, 32, 32)
        llJobsContainer.addView(tvNoJobs)
    }

    private fun showJobsLoadError() {
        llJobsContainer.removeAllViews()
        val tvError = TextView(this)
        tvError.text = getString(R.string.jobs_loading_error)
        tvError.textSize = 14f
        tvError.setTextColor(ContextCompat.getColor(this, R.color.gray_dark))
        tvError.gravity = android.view.Gravity.CENTER
        tvError.setPadding(32, 32, 32, 32)
        llJobsContainer.addView(tvError)
    }

    private fun showStatsUnavailable() {
        tvJobsCompleted.text = getString(R.string.dashboard_value_unavailable)
        tvEarningsToday.text = getString(R.string.dashboard_earnings_today, getString(R.string.dashboard_value_unavailable))
        tvRating.text = getString(R.string.dashboard_value_unavailable)
    }

    @SuppressLint("SetTextI18n")
    private fun toggleOnlineStatus() {
        isOnline = !isOnline

        if (isOnline) {
            tvOnlineStatus.text = getString(R.string.online)
            tvOnlineStatus.setBackgroundColor(ContextCompat.getColor(this, R.color.green_primary))
            tvOnlineStatus.setTextColor(ContextCompat.getColor(this, R.color.white))

            // Reload jobs from API
            llJobsContainer.removeAllViews()
            loadAvailableJobs()
        } else {
            tvOnlineStatus.text = getString(R.string.offline)
            tvOnlineStatus.setBackgroundColor(ContextCompat.getColor(this, R.color.gray_dark))
            tvOnlineStatus.setTextColor(ContextCompat.getColor(this, R.color.white))

            llJobsContainer.removeAllViews()
            val tvOffline = TextView(this)
            tvOffline.text = getString(R.string.offline_message)
            tvOffline.textSize = 14f
            tvOffline.setTextColor(ContextCompat.getColor(this, R.color.gray_dark))
            tvOffline.gravity = android.view.Gravity.CENTER
            tvOffline.setPadding(32, 32, 32, 32)
            llJobsContainer.addView(tvOffline)
        }
    }


    private fun setupClickListeners() {
        // Online/Offline Toggle
        tvOnlineStatus.setOnClickListener {
            toggleOnlineStatus()
        }


        // Bottom Navigation
        navHome.setOnClickListener {
            // Already on home
            Toast.makeText(
                this,
                getString(R.string.home_tab),
                Toast.LENGTH_SHORT
            ).show()
        }

        navJobs.setOnClickListener {
            val intent = Intent(this, JobsActivity::class.java)
            startActivity(intent)
        }

        navEarnings.setOnClickListener {
            val intent = Intent(this, EarningsActivity::class.java)
            startActivity(intent)
        }

        navProfile.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            intent.putExtra("collector_name", tvCollectorName.text.toString())
            val prefs = getSharedPreferences("collector_prefs", MODE_PRIVATE)
            intent.putExtra("collector_phone", prefs.getString("collector_phone", ""))
            startActivity(intent)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        coroutineScope.cancel()
    }
}