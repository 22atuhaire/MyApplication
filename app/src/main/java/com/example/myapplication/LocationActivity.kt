package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class LocationActivity : AppCompatActivity() {

    // Views
    private lateinit var tvProgress: TextView
    private lateinit var tvUseCurrentLocation: TextView
    private lateinit var etAddress: EditText
    private lateinit var etInstructions: EditText
    private lateinit var cbSaveAddress: CheckBox
    private lateinit var btnContinue: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_location)

        initViews()
        setupClickListeners()
        updateContinueButtonState()
    }

    private fun initViews() {
        tvProgress = findViewById(R.id.tvProgress)
        tvUseCurrentLocation = findViewById(R.id.tvUseCurrentLocation)
        etAddress = findViewById(R.id.etAddress)
        etInstructions = findViewById(R.id.etInstructions)
        cbSaveAddress = findViewById(R.id.cbSaveAddress)
        btnContinue = findViewById(R.id.btnContinue)
    }

    private fun setupClickListeners() {
        // Use current location
        tvUseCurrentLocation.setOnClickListener {
            // For demo, just fill with sample address
            etAddress.setText("123 Main Street, Kampala")
            Toast.makeText(this, "Current location used (demo)", Toast.LENGTH_SHORT).show()
            updateContinueButtonState()
        }

        // Address text change listener
        etAddress.addTextChangedListener(object : android.text.TextWatcher {
            override fun afterTextChanged(s: android.text.Editable?) {
                updateContinueButtonState()
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // Continue button
        btnContinue.setOnClickListener {
            proceedToNextScreen()
        }
    }

    private fun updateContinueButtonState() {
        val address = etAddress.text.toString().trim()
        val isAddressValid = address.isNotBlank()

        btnContinue.isEnabled = isAddressValid

        // Visual feedback
        if (isAddressValid) {
            btnContinue.backgroundTintList = ContextCompat.getColorStateList(this, R.color.green_primary)
        } else {
            btnContinue.backgroundTintList = ContextCompat.getColorStateList(this, R.color.disabled_gray)
        }
    }

    private fun proceedToNextScreen() {
        val address = etAddress.text.toString().trim()
        val instructions = etInstructions.text.toString().trim()
        val saveAddress = cbSaveAddress.isChecked

        // Get data from previous screens (passed via Intent)
        val selectedTypes = intent.getStringArrayExtra("selected_types") ?: arrayOf()
        val quantity = intent.getDoubleExtra("quantity", 0.0)
        val notes = intent.getStringExtra("notes") ?: ""
        val pickupTime = intent.getStringExtra("pickup_time") ?: "ASAP"

        // Navigate to Confirmation screen
        val intent = Intent(this, ConfirmationActivity::class.java)
        intent.putExtra("selected_types", selectedTypes)
        intent.putExtra("quantity", quantity)
        intent.putExtra("notes", notes)
        intent.putExtra("pickup_time", pickupTime)
        intent.putExtra("address", address)
        intent.putExtra("instructions", instructions)
        intent.putExtra("save_address", saveAddress)
        startActivity(intent)
    }

}