package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_jobs)

        initViews()
        setupClickListeners()
        loadAvailableJobs() // Default tab
    }

    private fun initViews() {
        // Top Bar
        tvBack = findViewById(R.id.tvBack)

        // Tabs
        tabAvailable = findViewById(R.id.tabAvailable)
        tabCompleted = findViewById(R.id.tabCompleted)

        // Jobs Container
        scrollJobs = findViewById(R.id.scrollJobs)
        llJobsContainer = findViewById(R.id.llJobsContainer)
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

    // Data class for available jobs
    data class AvailableJob(
        val foodType: String,
        val distance: String,
        val quantity: String,
        val timeAgo: String
    )
    private fun loadAvailableJobs() {
        llJobsContainer.removeAllViews()

        // Sample available jobs data using data class
        val availableJobs = listOf(
            AvailableJob("Mixed Food", "0.8 km", "2.5 kg", "5 min ago"),
            AvailableJob("Vegetables", "1.2 km", "4.0 kg", "12 min ago"),
            AvailableJob("Bakery", "2.5 km", "1.5 kg", "25 min ago"),
            AvailableJob("Meat/Dairy", "3.1 km", "3.0 kg", "40 min ago")
        )

        for (job in availableJobs) {
            addAvailableJobCard(job.foodType, job.distance, job.quantity, job.timeAgo)
        }

        // If no jobs, show empty state
        if (availableJobs.isEmpty()) {
            showEmptyState(R.string.no_available_jobs)
        }
    }


    private fun loadCompletedJobs() {
        llJobsContainer.removeAllViews()

        // Sample completed jobs data
        val completedJobs = listOf(
            arrayOf("Mixed Food", "2.5 kg", "25 Feb 2026", "450"),
            arrayOf("Vegetables", "4.0 kg", "24 Feb 2026", "640"),
            arrayOf("Bakery", "1.5 kg", "23 Feb 2026", "240")
        )

        for (job in completedJobs) {
            addCompletedJobCard(job[0], job[1], job[2], job[3])
        }

        // If no jobs, show empty state
        if (completedJobs.isEmpty()) {
            showEmptyState(R.string.no_completed_jobs)
        }
    }

    private fun addAvailableJobCard(foodType: String, distance: String, quantity: String, timeAgo: String) {
        // Inflate the available job card layout
        val jobCard = layoutInflater.inflate(R.layout.item_job_available, llJobsContainer, false)

        // Set job data
        val tvFoodType = jobCard.findViewById<TextView>(R.id.tvFoodType)
        val tvDistance = jobCard.findViewById<TextView>(R.id.tvDistance)
        val tvQuantity = jobCard.findViewById<TextView>(R.id.tvQuantity)
        val tvTimePosted = jobCard.findViewById<TextView>(R.id.tvTimePosted)
        val btnViewJob = jobCard.findViewById<Button>(R.id.btnViewJob)

        tvFoodType.text = foodType
        tvDistance.text = distance
        tvQuantity.text = "📦 $quantity"
        tvTimePosted.text = "⏱️ Posted $timeAgo"

        // View button click
        btnViewJob.setOnClickListener {
            Toast.makeText(this, "Viewing $foodType job", Toast.LENGTH_SHORT).show()

            // Navigate to Job Details
            val intent = Intent(this, JobDetailsActivity::class.java)
            intent.putExtra("food_type", foodType)
            intent.putExtra("quantity", quantity)
            intent.putExtra("distance", distance)
            intent.putExtra("time_ago", timeAgo)
            intent.putExtra("donor_name", "Sarah")
            intent.putExtra("donor_rating", "4.9")
            intent.putExtra("address", "123 Main Street, Apt 4B")
            intent.putExtra("city", "Kampala, Uganda")
            intent.putExtra("instructions", "Ring doorbell, back door")
            startActivity(intent)
        }

        llJobsContainer.addView(jobCard)
    }

    private fun addCompletedJobCard(foodType: String, quantity: String, date: String, earnings: String) {
        // Inflate the completed job card layout
        val jobCard = layoutInflater.inflate(R.layout.item_job_completed, llJobsContainer, false)

        // Set job data
        val tvFoodType = jobCard.findViewById<TextView>(R.id.tvFoodType)
        val tvQuantity = jobCard.findViewById<TextView>(R.id.tvQuantity)
        val tvCompletedDate = jobCard.findViewById<TextView>(R.id.tvCompletedDate)
        val tvEarnings = jobCard.findViewById<TextView>(R.id.tvEarnings)
        val btnDetails = jobCard.findViewById<Button>(R.id.btnDetails)

        tvFoodType.text = foodType
        tvQuantity.text = "📦 $quantity"
        tvCompletedDate.text = "📅 ${getString(R.string.job_completed_on)} $date"
        tvEarnings.text = String.format(getString(R.string.earned_amount), earnings)

        // Details button click
        btnDetails.setOnClickListener {
            Toast.makeText(this, "Viewing details for $foodType", Toast.LENGTH_SHORT).show()
            // Navigate to job summary/receipt (future)
        }

        llJobsContainer.addView(jobCard)
    }

    private fun showEmptyState(messageResId: Int) {
        val tvEmpty = TextView(this)
        tvEmpty.text = getString(messageResId)
        tvEmpty.textSize = 16f
        tvEmpty.setTextColor(ContextCompat.getColor(this, R.color.gray_dark))
        tvEmpty.gravity = android.view.Gravity.CENTER
        tvEmpty.setPadding(32, 64, 32, 64)
        llJobsContainer.addView(tvEmpty)
    }
}