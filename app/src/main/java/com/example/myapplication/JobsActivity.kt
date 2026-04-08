package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.myapplication.api.RetrofitClient
import com.example.myapplication.models.AvailableJob
import com.example.myapplication.models.toAvailableJob
import kotlinx.coroutines.*

class JobsActivity : AppCompatActivity() {

    // Top Bar
    private lateinit var tvBack: TextView

    // Tabs
    private lateinit var tabAvailable: TextView
    private lateinit var tabCompleted: TextView

    // Jobs Container
    private lateinit var scrollJobs: ScrollView
    private lateinit var llJobsContainer: LinearLayout

    // State
    private var currentTab = "available" // "available" or "completed"
    private lateinit var authToken: String
    private val coroutineScope = CoroutineScope(Dispatchers.Main + Job())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            setContentView(R.layout.activity_jobs)
            Log.d("JobsScreen", "Layout set successfully")

            initViews()

            // Get token from SharedPreferences
            val prefs = getSharedPreferences("collector_prefs", MODE_PRIVATE)
            authToken = prefs.getString("collector_token", "") ?: ""

            if (authToken.isEmpty()) {
                Toast.makeText(this, "Session expired. Please login again.", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, CollectorLoginActivity::class.java))
                finish()
                return
            }

            setupClickListeners()
            loadAvailableJobs() // Default tab
        } catch (e: Exception) {
            Log.e("JobsScreen", "Error in onCreate: ${e.message}", e)
            Toast.makeText(this, "Jobs Error: ${e.message}", Toast.LENGTH_LONG).show()
            finish()
        }
    }

    private fun initViews() {
        try {
            // Top Bar
            tvBack = findViewById(R.id.tvBack) ?: throw Exception("tvBack not found")

            // Tabs
            tabAvailable = findViewById(R.id.tabAvailable) ?: throw Exception("tabAvailable not found")
            tabCompleted = findViewById(R.id.tabCompleted) ?: throw Exception("tabCompleted not found")

            // Jobs Container
            scrollJobs = findViewById(R.id.scrollJobs) ?: throw Exception("scrollJobs not found")
            llJobsContainer = findViewById(R.id.llJobsContainer) ?: throw Exception("llJobsContainer not found")
        } catch (e: Exception) {
            Log.e("JobsScreen", "Error finding view: ${e.message}")
            throw e
        }
    }

    private fun setupClickListeners() {
        // Back button
        tvBack.setOnClickListener {
            finish()
        }

        // Available tab
        tabAvailable.setOnClickListener {
            if (currentTab != "available") {
                currentTab = "available"
                highlightTab(tabAvailable, tabCompleted)
                loadAvailableJobs()
            }
        }

        // Completed tab
        tabCompleted.setOnClickListener {
            if (currentTab != "completed") {
                currentTab = "completed"
                highlightTab(tabCompleted, tabAvailable)
                loadCompletedJobs()
            }
        }
    }

    private fun highlightTab(selectedTab: TextView, otherTab: TextView) {
        // Selected tab - green background, white text
        selectedTab.setBackgroundColor(ContextCompat.getColor(this, R.color.green_primary))
        selectedTab.setTextColor(ContextCompat.getColor(this, R.color.white))

        // Other tab - light gray background, gray text
        otherTab.setBackgroundColor(ContextCompat.getColor(this, R.color.gray_light))
        otherTab.setTextColor(ContextCompat.getColor(this, R.color.gray_dark))
    }

    private fun loadAvailableJobs() {
        coroutineScope.launch {
            try {
                val apiService = RetrofitClient.getAuthApiService(authToken)
                val donorPostsResponse = withContext(Dispatchers.IO) {
                    apiService.getAvailableDonorPosts().execute()
                }

                if (donorPostsResponse.isSuccessful) {
                    val jobs = donorPostsResponse.body()?.data.orEmpty().map { it.toAvailableJob() }
                    if (jobs.isNotEmpty()) {
                        withContext(Dispatchers.Main) {
                            llJobsContainer.removeAllViews()
                            jobs.forEach { job ->
                                addRealJobCard(job)
                            }
                            Log.d("JobsScreen", "Loaded ${jobs.size} available jobs from API")
                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            showNoJobsMessage()
                        }
                    }
                } else {
                    Log.e("JobsScreen", "Donor posts endpoint failed: ${donorPostsResponse.code()}")

                    // Fallback during backend transition.
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
                        Log.e("JobsScreen", "Legacy endpoint failed: ${legacyResponse.code()}")
                        withContext(Dispatchers.Main) {
                            showJobsLoadError()
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("JobsScreen", "Exception loading jobs: ${e.message}")
                withContext(Dispatchers.Main) {
                    showJobsLoadError()
                }
            }
        }
    }

    private fun addRealJobCard(job: AvailableJob) {
        // Inflate the available job card layout
        val jobCard = layoutInflater.inflate(R.layout.item_job_available, llJobsContainer, false)

        // Set job data
        val tvFoodType = jobCard.findViewById<TextView>(R.id.tvFoodType)
        val tvDistance = jobCard.findViewById<TextView>(R.id.tvDistance)
        val tvQuantity = jobCard.findViewById<TextView>(R.id.tvQuantity)
        val tvTimePosted = jobCard.findViewById<TextView>(R.id.tvTimePosted)
        val btnViewJob = jobCard.findViewById<Button>(R.id.btnViewJob)

        tvFoodType.text = job.foodType
        val distanceLabel = job.distance?.let { "${it} km" } ?: "Distance N/A"
        tvDistance.text = getString(
            R.string.available_job_distance_quantity,
            distanceLabel,
            job.quantity.toString()
        )
        tvQuantity.text = getString(R.string.available_job_quantity, job.quantity.toString())
        tvTimePosted.text = getString(R.string.posted_prefix, job.timeAgo)

        // View button click
        btnViewJob.setOnClickListener {
            val intent = Intent(this, JobDetailsActivity::class.java)
            intent.putExtra("job_id", job.id)
            intent.putExtra("food_type", job.foodType)
            intent.putExtra("quantity", "${job.quantity}")
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
        tvNoJobs.setPadding(32, 64, 32, 64)
        llJobsContainer.addView(tvNoJobs)
    }

    private fun showJobsLoadError() {
        llJobsContainer.removeAllViews()
        val tvError = TextView(this)
        tvError.text = getString(R.string.jobs_loading_error)
        tvError.textSize = 14f
        tvError.setTextColor(ContextCompat.getColor(this, R.color.gray_dark))
        tvError.gravity = android.view.Gravity.CENTER
        tvError.setPadding(32, 64, 32, 64)
        llJobsContainer.addView(tvError)
    }

    private fun loadCompletedJobs() {
        llJobsContainer.removeAllViews()
        showNoCompletedJobsMessage()
    }

    private fun showNoCompletedJobsMessage() {
        val tvNoCompleted = TextView(this)
        tvNoCompleted.text = getString(R.string.no_completed_jobs)
        tvNoCompleted.textSize = 14f
        tvNoCompleted.setTextColor(ContextCompat.getColor(this, R.color.gray_dark))
        tvNoCompleted.gravity = android.view.Gravity.CENTER
        tvNoCompleted.setPadding(32, 64, 32, 64)
        llJobsContainer.addView(tvNoCompleted)
    }

    override fun onDestroy() {
        super.onDestroy()
        coroutineScope.cancel()
    }
}