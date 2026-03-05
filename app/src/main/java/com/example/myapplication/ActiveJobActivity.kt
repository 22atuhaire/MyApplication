package com.example.myapplication

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class ActiveJobActivity : AppCompatActivity() {

    // Top Bar
    private lateinit var tvBack: TextView

    // Job Info
    private lateinit var tvFoodType: TextView
    private lateinit var tvQuantity: TextView
    private lateinit var tvDonorName: TextView

    // Location
    private lateinit var tvAddress: TextView
    private lateinit var tvCity: TextView
    private lateinit var tvInstructions: TextView
    private lateinit var btnDirections: Button

    // Contact
    private lateinit var tvDonorPhone: TextView

    // Buttons
    private lateinit var btnCallDonor: Button
    private lateinit var btnMarkCollected: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_active_job)

        initViews()
        loadJobData()
        setupClickListeners()
    }

    private fun initViews() {
        // Top Bar
        tvBack = findViewById(R.id.tvBack)

        // Job Info
        tvFoodType = findViewById(R.id.tvFoodType)
        tvQuantity = findViewById(R.id.tvQuantity)
        tvDonorName = findViewById(R.id.tvDonorName)

        // Location
        tvAddress = findViewById(R.id.tvAddress)
        tvCity = findViewById(R.id.tvCity)
        tvInstructions = findViewById(R.id.tvInstructions)
        btnDirections = findViewById(R.id.btnDirections)

        // Contact
        tvDonorPhone = findViewById(R.id.tvDonorPhone)

        // Buttons
        btnCallDonor = findViewById(R.id.btnCallDonor)
        btnMarkCollected = findViewById(R.id.btnMarkCollected)
    }

    private fun loadJobData() {
        // Get job data from intent (passed from JobDetailsActivity)
        val foodType = intent.getStringExtra("food_type") ?: "Mixed Food"
        val quantity = intent.getStringExtra("quantity") ?: "2.5 kg"
        val donorName = intent.getStringExtra("donor_name") ?: "Sarah"
        val donorPhone = intent.getStringExtra("donor_phone") ?: "+256 712 345 678"
        val address = intent.getStringExtra("address") ?: "123 Main Street, Apt 4B"
        val city = intent.getStringExtra("city") ?: "Kampala, Uganda"
        val instructions = intent.getStringExtra("instructions") ?: "Ring doorbell, back door"

        // Set job info
        tvFoodType.text = foodType
        tvQuantity.text = "📦 ${getString(R.string.quantity_label)} $quantity"
        tvDonorName.text = "👤 ${getString(R.string.donor_name_label)} $donorName"

        // Set location
        tvAddress.text = address
        tvCity.text = city
        tvInstructions.text = "📝 $instructions"

        // Set contact
        tvDonorPhone.text = "📞 $donorPhone"
    }

    private fun setupClickListeners() {
        // Back button
        tvBack.setOnClickListener {
            finish()
        }

        // Directions button
        btnDirections.setOnClickListener {
            val address = "${tvAddress.text}, ${tvCity.text}"
            val uri = Uri.parse("geo:0,0?q=$address")
            val intent = Intent(Intent.ACTION_VIEW, uri)
            startActivity(intent)
            Toast.makeText(this, R.string.directions_open, Toast.LENGTH_SHORT).show()
        }

        // Call Donor button
        btnCallDonor.setOnClickListener {
            val phone = tvDonorPhone.text.toString().replace("📞 ", "")
            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))
            startActivity(intent)
        }

        // Mark as Collected button
        btnMarkCollected.setOnClickListener {
            Toast.makeText(this, R.string.collection_confirmed, Toast.LENGTH_LONG).show()

            // Navigate back to dashboard after collection
            // In a real app, you might go to a thank you/rating screen
            finish()
        }
    }
}