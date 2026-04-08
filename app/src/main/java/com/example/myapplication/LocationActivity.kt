package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.myapplication.api.RetrofitClient
import com.example.myapplication.models.WastePostRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LocationActivity : AppCompatActivity() {

    // Views
    private lateinit var tvProgress: TextView
    private lateinit var tvUseCurrentLocation: TextView
    private lateinit var etAddress: EditText
    private lateinit var etInstructions: EditText
    private lateinit var cbSaveAddress: CheckBox
    private lateinit var btnContinue: Button

    // Data from previous screens
    private var selectedTypes: Array<String> = arrayOf()
    private var quantity: Double = 0.0
    private var notes: String = ""
    private var pickupTime: String = "ASAP"

    private val coroutineScope = CoroutineScope(Dispatchers.Main + Job())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_location)

        getIntentData()
        initViews()
        setupClickListeners()
        updateContinueButtonState()
    }

    private fun getIntentData() {
        selectedTypes = intent.getStringArrayExtra("selected_types") ?: arrayOf()
        quantity = intent.getDoubleExtra("quantity", 0.0)
        notes = intent.getStringExtra("notes") ?: ""
        pickupTime = intent.getStringExtra("pickup_time") ?: "ASAP"
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
            submitWastePost()
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

    private fun submitWastePost() {
        val address = etAddress.text.toString().trim()
        val instructions = etInstructions.text.toString().trim().ifBlank { null }

        if (address.isBlank()) {
            Toast.makeText(this, "Please enter an address", Toast.LENGTH_SHORT).show()
            return
        }

        val request = WastePostRequest(
            waste_types = selectedTypes.toList(),
            quantity = quantity,
            notes = notes.ifBlank { null },
            pickup_time = pickupTime,
            address = address,
            instructions = instructions,
            photos = null
        )

        setLoading(true)

        coroutineScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    RetrofitClient.apiService.createWastePost(request).execute()
                }

                setLoading(false)

                if (response.isSuccessful) {
                    val result = response.body()
                    if (result?.success == true) {
                        val confirmationIntent = Intent(this@LocationActivity, ConfirmationActivity::class.java).apply {
                            putExtra("post_id", result.data?.id ?: 0)
                            putExtra("selected_types", selectedTypes)
                            putExtra("quantity", quantity)
                            putExtra("notes", notes)
                            putExtra("pickup_time", pickupTime)
                            putExtra("address", address)
                            putExtra("instructions", instructions)
                        }
                        startActivity(confirmationIntent)
                    } else {
                        Toast.makeText(
                            this@LocationActivity,
                            result?.message ?: "Failed to create post",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                } else {
                    val errorBody = response.errorBody()?.string().orEmpty()
                    Log.e("WastePostError", "HTTP ${response.code()}: $errorBody")
                    Toast.makeText(
                        this@LocationActivity,
                        "Failed to post waste (${response.code()})",
                        Toast.LENGTH_LONG
                    ).show()
                }
            } catch (e: Exception) {
                setLoading(false)
                Log.e("WastePostError", "Request failed", e)
                Toast.makeText(
                    this@LocationActivity,
                    "Network error: ${e.message ?: "unknown"}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun setLoading(loading: Boolean) {
        btnContinue.isEnabled = !loading
        if (loading) {
            btnContinue.text = "Submitting..."
        } else {
            btnContinue.text = getString(R.string.continue_button)
            updateContinueButtonState()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        coroutineScope.cancel()
    }

}