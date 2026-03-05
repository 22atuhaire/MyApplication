package com.example.myapplication

import android.content.Intent
import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
//import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat

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

    // Active Collections
    private lateinit var tvActiveCount: TextView
    private lateinit var llActiveCollections: LinearLayout

    // Bottom Navigation
    private lateinit var navHome: LinearLayout
    private lateinit var navJobs: LinearLayout
    private lateinit var navEarnings: LinearLayout
    private lateinit var navProfile: LinearLayout

    // Sample data
    private var isOnline = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_collector_dashboard)

        initViews()
        setupClickListeners()
        loadSampleData()
        loadSampleJobs()
    }

    private fun initViews() {
        // Top Bar
        tvOnlineStatus = findViewById(R.id.tvOnlineStatus)

        // Welcome
        tvCollectorName = findViewById(R.id.tvCollectorName)

        // Summary Stats
        tvJobsCompleted = findViewById(R.id.tvJobsCompleted)
        tvEarningsToday = findViewById(R.id.tvEarningsToday)
        tvRating = findViewById(R.id.tvRating)

        // Jobs Container
        llJobsContainer = findViewById(R.id.llJobsContainer)

        // Active Collections
        tvActiveCount = findViewById(R.id.tvActiveCount)
        llActiveCollections = findViewById(R.id.llActiveCollections)

        // Bottom Navigation
        navHome = findViewById(R.id.navHome)
        navJobs = findViewById(R.id.navJobs)
        navEarnings = findViewById(R.id.navEarnings)
        navProfile = findViewById(R.id.navProfile)
    }

    private fun setupClickListeners() {
        // Online/Offline Toggle
        tvOnlineStatus.setOnClickListener {
            toggleOnlineStatus()
        }

        // Active Collections Link
        llActiveCollections.setOnClickListener {
            // Navigate to active jobs screen (future)
            android.widget.Toast.makeText(
                this,
                getString(R.string.active_collections_coming),
                android.widget.Toast.LENGTH_SHORT
            ).show()
        }

        // Bottom Navigation
        navHome.setOnClickListener {
            // Already on home
            android.widget.Toast.makeText(
                this,
                getString(R.string.home_tab),
                android.widget.Toast.LENGTH_SHORT
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
            intent.putExtra("collector_phone", "+256 712 345 678") // You'd get this from real data
            startActivity(intent)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun toggleOnlineStatus() {
        isOnline = !isOnline

        if (isOnline) {
            tvOnlineStatus.text = getString(R.string.online)
            tvOnlineStatus.setBackgroundColor(ContextCompat.getColor(this, R.color.green_primary))
            tvOnlineStatus.setTextColor(ContextCompat.getColor(this, R.color.white))

            // Show jobs
            llJobsContainer.removeAllViews()
            loadSampleJobs()
        } else {
            tvOnlineStatus.text = getString(R.string.offline)
            tvOnlineStatus.setBackgroundColor(ContextCompat.getColor(this, R.color.gray_dark))
            tvOnlineStatus.setTextColor(ContextCompat.getColor(this, R.color.white))

            // Clear jobs
            llJobsContainer.removeAllViews()

            // Show offline message
            val tvOffline = TextView(this)
            tvOffline.text = "You are offline. Go online to see available jobs."
            tvOffline.textSize = 14f
            tvOffline.setTextColor(ContextCompat.getColor(this, R.color.gray_dark))
            tvOffline.gravity = android.view.Gravity.CENTER
            tvOffline.setPadding(32, 32, 32, 32)
            llJobsContainer.addView(tvOffline)
        }
    }

    private fun loadSampleData() {
        // Get collector name from intent (passed from login)
        val name = intent.getStringExtra("collector_name") ?: "John"
        tvCollectorName.text = name

        // Sample stats
        tvJobsCompleted.text = getString(R.string.jobs_completed)
        tvEarningsToday.text = getString(R.string.ugx_30000)
        tvRating.text = getString(R.string._rating)

        // Sample active count
        tvActiveCount.text = getString(R.string.in_progress, 2)
    }

    private fun loadSampleJobs() {
        if (!isOnline) return

        // Sample job data
        val jobs = listOf(
            Triple("Mixed Food", "0.8 km • 2.5 kg", "5 min ago"),
            Triple("Vegetables", "1.2 km • 4.0 kg", "12 min ago"),
            Triple("Bakery", "2.5 km • 1.5 kg", "25 min ago")
        )

        for (job in jobs) {
            addJobCard(job.first, job.second, timeAgo = job.third)
        }
    }

    private fun addJobCard(foodType: String, distance: String, timeAgo: String) {
        // Inflate the job card layout
        val jobCard = layoutInflater.inflate(R.layout.item_job_card, llJobsContainer, false)

        // Set job data
        val tvFoodType = jobCard.findViewById<TextView>(R.id.tvFoodType)
        val tvDistance = jobCard.findViewById<TextView>(R.id.tvDistance)
        val tvTimeAgo = jobCard.findViewById<TextView>(R.id.tvTimeAgo)
        val btnView = jobCard.findViewById<TextView>(R.id.btnView)

        tvFoodType.text = foodType
        tvDistance.text = distance
        tvTimeAgo.text = "Posted $timeAgo"

        // Set appropriate icon based on food type
        val tvFoodIcon = jobCard.findViewById<TextView>(R.id.tvFoodIcon)
        tvFoodIcon.text = when {
            foodType.contains("Mixed") -> "🍚"
            foodType.contains("Vegetables") -> "🥬"
            foodType.contains("Bakery") -> "🍞"
            else -> "🍲"
        }

        // Parse distance string to extract quantity and distance
        val parts = distance.split("•")
        val distanceValue = if (parts.isNotEmpty()) parts[0].trim() else distance
        val quantityValue = if (parts.size > 1) parts[1].trim() else "2.5 kg"

        // Set different donor data based on food type (so each job looks unique)
        val donorName = when {
            foodType.contains("Mixed") -> "Sarah"
            foodType.contains("Vegetables") -> "Michael"
            foodType.contains("Bakery") -> "Jane"
            else -> "David"
        }

        val donorRating = when {
            foodType.contains("Mixed") -> "4.9"
            foodType.contains("Vegetables") -> "4.7"
            foodType.contains("Bakery") -> "5.0"
            else -> "4.8"
        }

        val address = when {
            foodType.contains("Mixed") -> "123 Main Street, Apt 4B"
            foodType.contains("Vegetables") -> "456 Park Avenue"
            foodType.contains("Bakery") -> "789 Oak Road"
            else -> "321 Pine Street"
        }

        val instructions = when {
            foodType.contains("Mixed") -> "Ring doorbell, back door"
            foodType.contains("Vegetables") -> "Leave at front gate"
            foodType.contains("Bakery") -> "Call when arrived"
            else -> "Ring bell 3 times"
        }

        // View button click - Navigate to Job Details with THIS JOB's data
        btnView.setOnClickListener {
            val intent = Intent(this, JobDetailsActivity::class.java)
            intent.putExtra("food_type", foodType)
            intent.putExtra("quantity", quantityValue)
            intent.putExtra("distance", distanceValue)
            intent.putExtra("time_ago", timeAgo)
            intent.putExtra("donor_name", donorName)
            intent.putExtra("donor_rating", donorRating)
            intent.putExtra("address", address)
            intent.putExtra("city", "Kampala, Uganda") // Same city for all
            intent.putExtra("instructions", instructions)
            startActivity(intent)
        }

        llJobsContainer.addView(jobCard)
    }
}