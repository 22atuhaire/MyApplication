package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat

class WasteTypeActivity : AppCompatActivity() {

    // Views
    private lateinit var tvProgress: TextView
    private lateinit var scrollWasteTypes: ScrollView
    private lateinit var llWasteTypesContainer: LinearLayout
    private lateinit var tvSelectionCounter: TextView
    private lateinit var btnContinue: Button

    // Data - initialized in onCreate
    private lateinit var wasteTypes: List<WasteType>
    private val selectedItems = mutableListOf<WasteType>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_waste_type)

        // Initialize waste types here (context is ready)
        wasteTypes = listOf(
            WasteType("cooked", "🍚", getString(R.string.cooked_food), getString(R.string.cooked_food_desc), false),
            WasteType("vegetables", "🥬", getString(R.string.vegetables), getString(R.string.vegetables_desc), false),
            WasteType("bakery", "🍞", getString(R.string.bakery), getString(R.string.bakery_desc), false),
            WasteType("meat", "🥩", getString(R.string.meat_dairy), getString(R.string.meat_dairy_desc), true),
            WasteType("mixed", "❓", getString(R.string.mixed_other), getString(R.string.mixed_other_desc), false)
        )

        initViews()
        loadWasteTypes()
        updateSelectionCounter()
    }

    private fun initViews() {
        tvProgress = findViewById(R.id.tvProgress)
        scrollWasteTypes = findViewById(R.id.scrollWasteTypes)
        llWasteTypesContainer = findViewById(R.id.llWasteTypesContainer)
        tvSelectionCounter = findViewById(R.id.tvSelectionCounter)
        btnContinue = findViewById(R.id.btnContinue)

        // Continue button click
        btnContinue.setOnClickListener {
            proceedToNextScreen()
        }
    }

    private fun loadWasteTypes() {
        llWasteTypesContainer.removeAllViews()

        for (wasteType in wasteTypes) {
            addWasteTypeCard(wasteType)
        }
    }

    private fun addWasteTypeCard(wasteType: WasteType) {
        // Inflate the waste type card layout
        val cardView = layoutInflater.inflate(R.layout.item_waste_type, llWasteTypesContainer, false) as CardView

        // Get views
        val tvIcon = cardView.findViewById<TextView>(R.id.tvIcon)
        val tvTitle = cardView.findViewById<TextView>(R.id.tvTitle)
        val tvDescription = cardView.findViewById<TextView>(R.id.tvDescription)
        val tvCheckmark = cardView.findViewById<TextView>(R.id.tvCheckmark)

        // Set data
        tvIcon.text = wasteType.icon
        tvTitle.text = wasteType.title
        tvDescription.text = wasteType.description

        // Special styling for meat/dairy warning
        if (wasteType.isWarning) {
            tvDescription.setTextColor(ContextCompat.getColor(this, R.color.orange_accent))
        }

        // Set click listener
        cardView.setOnClickListener {
            toggleSelection(wasteType, cardView, tvCheckmark)
        }

        llWasteTypesContainer.addView(cardView)
    }

    private fun toggleSelection(wasteType: WasteType, cardView: CardView, checkmark: TextView) {
        if (selectedItems.contains(wasteType)) {
            // Deselect
            selectedItems.remove(wasteType)
            cardView.setCardBackgroundColor(ContextCompat.getColor(this, R.color.white))
            checkmark.visibility = TextView.GONE
        } else {
            // Select
            selectedItems.add(wasteType)
            cardView.setCardBackgroundColor(ContextCompat.getColor(this, R.color.light_green_selected))
            checkmark.visibility = TextView.VISIBLE
        }

        updateSelectionCounter()
    }

    private fun updateSelectionCounter() {
        val count = selectedItems.size

        // Update counter text
        val counterText = if (count == 1) {
            getString(R.string.selected_items, count)
        } else {
            getString(R.string.selected_items_plural, count)
        }
        tvSelectionCounter.text = counterText

        // Enable/disable continue button
        if (count > 0) {
            btnContinue.isEnabled = true
            btnContinue.backgroundTintList = ContextCompat.getColorStateList(this, R.color.green_primary)
        } else {
            btnContinue.isEnabled = false
            btnContinue.backgroundTintList = ContextCompat.getColorStateList(this, R.color.disabled_gray)
        }
    }

    private fun proceedToNextScreen() {
        // Get selected waste type IDs
        val selectedIds = selectedItems.map { it.id }.toTypedArray()

        // Navigate to Quantity screen
        val intent = Intent(this, QuantityActivity::class.java)
        intent.putExtra("selected_types", selectedIds)
        startActivity(intent)
    }

    // Data class for waste types
    data class WasteType(
        val id: String,
        val icon: String,
        val title: String,
        val description: String,
        val isWarning: Boolean
    )
}