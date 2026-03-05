package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class JobDetailsActivity : AppCompatActivity() {

    // Top Bar
    private lateinit var tvBack: TextView

    // Job Details
    private lateinit var tvFoodType: TextView
    private lateinit var tvQuantity: TextView
    private lateinit var tvDistance: TextView
    private lateinit var tvTimePosted: TextView
    private lateinit var tvDonor: TextView

    // Location
    private lateinit var tvAddress: TextView
    private lateinit var tvCity: TextView
    private lateinit var tvInstructions: TextView

    // Buttons
    private lateinit var btnAccept: Button
    private lateinit var btnDecline: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_job_details)

        initViews()
        loadJobData()
        setupClickListeners()
    }

    private fun initViews() {
        // Top Bar
        tvBack = findViewById(R.id.tvBack)

        // Job Details
        tvFoodType = findViewById(R.id.tvFoodType)
        tvQuantity = findViewById(R.id.tvQuantity)
        tvDistance = findViewById(R.id.tvDistance)
        tvTimePosted = findViewById(R.id.tvTimePosted)
        tvDonor = findViewById(R.id.tvDonor)

        // Location
        tvAddress = findViewById(R.id.tvAddress)
        tvCity = findViewById(R.id.tvCity)
        tvInstructions = findViewById(R.id.tvInstructions)

        // Buttons
        btnAccept = findViewById(R.id.btnAccept)
        btnDecline = findViewById(R.id.btnDecline)
    }

    private fun loadJobData() {
        // Get job data from intent (passed from dashboard)
        val foodType = intent.getStringExtra("food_type") ?: "Mixed Food"
        val quantity = intent.getStringExtra("quantity") ?: "2.5 kg"
        val distance = intent.getStringExtra("distance") ?: "0.8 km"
        val timeAgo = intent.getStringExtra("time_ago") ?: "5 min ago"
        val donorName = intent.getStringExtra("donor_name") ?: "Sarah"
        val donorRating = intent.getStringExtra("donor_rating") ?: "4.9"
        val address = intent.getStringExtra("address") ?: "123 Main Street, Apt 4B"
        val city = intent.getStringExtra("city") ?: "Kampala, Uganda"
        val instructions = intent.getStringExtra("instructions") ?: "Ring doorbell, back door"

        // Set job details - USING DYNAMIC VALUES FROM INTENT
        tvFoodType.text = foodType
        tvQuantity.text = "📦 ${getString(R.string.quantity)} $quantity"
        tvDistance.text = "📍 ${getString(R.string.distance)} $distance"
        tvTimePosted.text = "⏱️ ${getString(R.string.posted)} $timeAgo"
        tvDonor.text = "👤 ${getString(R.string.donor)} $donorName (⭐$donorRating)"

        // Set location
        tvAddress.text = address
        tvCity.text = city
        tvInstructions.text = instructions
    }

    private fun setupClickListeners() {
        // Back button
        tvBack.setOnClickListener {
            finish()
        }

        // Accept button
        btnAccept.setOnClickListener {
            Toast.makeText(this, R.string.job_accepted, Toast.LENGTH_SHORT).show()

            // Navigate to Active Job screen with all job data
            val intent = Intent(this, ActiveJobActivity::class.java)
            intent.putExtra("food_type", tvFoodType.text.toString())
            intent.putExtra("quantity", intent.getStringExtra("quantity") ?: "2.5 kg")
            intent.putExtra("donor_name", intent.getStringExtra("donor_name") ?: "Sarah")
            intent.putExtra("donor_phone", "+256 712 345 678") // You'd get this from real data
            intent.putExtra("address", intent.getStringExtra("address") ?: "123 Main Street, Apt 4B")
            intent.putExtra("city", intent.getStringExtra("city") ?: "Kampala, Uganda")
            intent.putExtra("instructions", intent.getStringExtra("instructions") ?: "Ring doorbell, back door")
            startActivity(intent)
            finish()
        }
            // Navigate to Active Job screen (Screen 3F - we'll create next)



        // Decline button
        btnDecline.setOnClickListener {
            Toast.makeText(this, R.string.job_declined, Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}