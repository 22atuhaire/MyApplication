package com.example.myapplication

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class EarningsActivity : AppCompatActivity() {

    // Top Bar
    private lateinit var tvBack: TextView

    // Summary
    private lateinit var tvTotalEarnings: TextView
    private lateinit var tvTotalKg: TextView

    // Input
    private lateinit var etKgInput: EditText
    private lateinit var btnCalculate: Button
    private lateinit var tvEstimatedEarnings: TextView

    // Recent Collections
    private lateinit var llRecentContainer: LinearLayout

    // Data
    private var totalEarnings = 2450  // In KES
    private var totalKg = 15.3
    private val ratePerKg = 160  // KES per kg

    // Sample recent collections
    private val recentCollections = listOf(
        Triple("25 Feb 2026", 2.5, 400),
        Triple("24 Feb 2026", 4.0, 640),
        Triple("23 Feb 2026", 1.5, 240),
        Triple("22 Feb 2026", 3.0, 480),
        Triple("21 Feb 2026", 2.0, 320)
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_earnings)

        initViews()
        setupClickListeners()
        loadSummaryData()
        loadRecentCollections()
    }

    private fun initViews() {
        // Top Bar
        tvBack = findViewById(R.id.tvBack)

        // Summary
        tvTotalEarnings = findViewById(R.id.tvTotalEarnings)
        tvTotalKg = findViewById(R.id.tvTotalKg)

        // Input
        etKgInput = findViewById(R.id.etKgInput)
        btnCalculate = findViewById(R.id.btnCalculate)
        tvEstimatedEarnings = findViewById(R.id.tvEstimatedEarnings)

        // Recent Collections
        llRecentContainer = findViewById(R.id.llRecentContainer)
    }

    private fun setupClickListeners() {
        // Back button
        tvBack.setOnClickListener {
            finish()
        }

        // Calculate button
        btnCalculate.setOnClickListener {
            calculateEarnings()
        }

        // Text change listener to clear previous estimate when typing
        etKgInput.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                tvEstimatedEarnings.text = ""
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun loadSummaryData() {
        tvTotalEarnings.text = "UGX $totalEarnings"
        tvTotalKg.text = "$totalKg kg"
    }

    private fun calculateEarnings() {
        val input = etKgInput.text.toString().trim()

        if (input.isEmpty()) {
            Toast.makeText(this, "Please enter kg collected", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val kg = input.toDouble()
            if (kg <= 0) {
                Toast.makeText(this, "Please enter a valid number", Toast.LENGTH_SHORT).show()
                return
            }

            val estimatedEarnings = kg * ratePerKg

            // Format with commas for thousands
            val formattedEarnings = "UGX ${String.format("%,.0f", estimatedEarnings)}"
            tvEstimatedEarnings.text = String.format(getString(R.string.estimated_earnings), formattedEarnings)

            // Optional: Show quick toast
            Toast.makeText(this, "For $kg kg you'll earn UGX ${estimatedEarnings.toInt()}", Toast.LENGTH_SHORT).show()

        } catch (e: NumberFormatException) {
            Toast.makeText(this, "Please enter a valid number", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadRecentCollections() {
        llRecentContainer.removeAllViews()

        for (collection in recentCollections) {
            addRecentItem(collection.first, collection.second, collection.third)
        }
    }

    private fun addRecentItem(date: String, kg: Double, earnings: Int) {
        // Inflate the recent item layout
        val itemView = layoutInflater.inflate(R.layout.item_recent_collection, llRecentContainer, false)

        // Set data
        val tvDate = itemView.findViewById<TextView>(R.id.tvDate)
        val tvKg = itemView.findViewById<TextView>(R.id.tvKg)
        val tvEarnings = itemView.findViewById<TextView>(R.id.tvEarnings)

        tvDate.text = date
        tvKg.text = "$kg kg"
        tvEarnings.text = "UGX $earnings"

        llRecentContainer.addView(itemView)
    }
}