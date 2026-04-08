package com.example.myapplication

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import java.util.Calendar
import android.content.Intent

class QuantityActivity : AppCompatActivity() {

    // Views
    private lateinit var tvProgress: TextView
    private lateinit var etQuantity: EditText
    private lateinit var llPhotoContainer: LinearLayout
    private lateinit var btnAddPhoto: TextView
    private lateinit var etNotes: EditText
    private lateinit var radioGroup: RadioGroup
    private lateinit var radioAsap: RadioButton
    private lateinit var radioSchedule: RadioButton
    private lateinit var llSchedule: LinearLayout
    private lateinit var btnDate: Button
    private lateinit var btnTime: Button
    private lateinit var btnContinue: Button

    // Data
    private val selectedPhotos = mutableListOf<String>() // Will store URIs
    private var selectedDate = ""
    private var selectedTime = ""
    private var isScheduled = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quantity)

        initViews()
        setupClickListeners()
        updateContinueButtonState()
    }

    private fun initViews() {
        tvProgress = findViewById(R.id.tvProgress)
        etQuantity = findViewById(R.id.etQuantity)
        llPhotoContainer = findViewById(R.id.llPhotoContainer)
        btnAddPhoto = findViewById(R.id.btnAddPhoto)
        etNotes = findViewById(R.id.etNotes)
        radioGroup = findViewById(R.id.radioGroup)
        radioAsap = findViewById(R.id.radioAsap)
        radioSchedule = findViewById(R.id.radioSchedule)
        llSchedule = findViewById(R.id.llSchedule)
        btnDate = findViewById(R.id.btnDate)
        btnTime = findViewById(R.id.btnTime)
        btnContinue = findViewById(R.id.btnContinue)
    }

    private fun setupClickListeners() {
        // Add photo button
        btnAddPhoto.setOnClickListener {
            // For demo, just add a placeholder
            if (selectedPhotos.size < 3) {
                addPhotoPlaceholder()
                Toast.makeText(this, "Photo added (demo)", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Maximum 3 photos", Toast.LENGTH_SHORT).show()
            }
        }

        // Radio group listener
        radioGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.radioAsap -> {
                    isScheduled = false
                    llSchedule.visibility = android.view.View.GONE
                }
                R.id.radioSchedule -> {
                    isScheduled = true
                    llSchedule.visibility = android.view.View.VISIBLE
                }
            }
            updateContinueButtonState()
        }

        // Date picker
        btnDate.setOnClickListener {
            showDatePicker()
        }

        // Time picker
        btnTime.setOnClickListener {
            showTimePicker()
        }

        // Continue button
        btnContinue.setOnClickListener {
            proceedToNextScreen()
        }

        // Text change listener for quantity
        etQuantity.addTextChangedListener(object : android.text.TextWatcher {
            override fun afterTextChanged(s: android.text.Editable?) {
                updateContinueButtonState()
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun addPhotoPlaceholder() {
        val photoView = TextView(this)
        photoView.layoutParams = LinearLayout.LayoutParams(80.dpToPx(), 80.dpToPx())
        photoView.text = "📷"
        photoView.textSize = 32f
        photoView.gravity = android.view.Gravity.CENTER
        photoView.setBackgroundColor(ContextCompat.getColor(this, R.color.gray_light))
        photoView.setPadding(8, 8, 8, 8)
        photoView.layoutParams = (photoView.layoutParams as LinearLayout.LayoutParams).apply {
            marginEnd = 8.dpToPx()
        }

        // Remove button on click
        photoView.setOnClickListener {
            llPhotoContainer.removeView(photoView)
            selectedPhotos.remove("placeholder")
            Toast.makeText(this, "Photo removed", Toast.LENGTH_SHORT).show()
            updateContinueButtonState()
        }

        llPhotoContainer.addView(photoView, llPhotoContainer.childCount - 1) // Add before the add button
        selectedPhotos.add("placeholder")
        updateContinueButtonState()
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        DatePickerDialog(this, { _, year, month, dayOfMonth ->
            selectedDate = "$dayOfMonth/${month + 1}/$year"
            btnDate.text = selectedDate
            updateContinueButtonState()
        }, year, month, day).show()
    }

    private fun showTimePicker() {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        TimePickerDialog(this, { _, hourOfDay, minute ->
            val amPm = if (hourOfDay < 12) "AM" else "PM"
            val hour12 = if (hourOfDay > 12) hourOfDay - 12 else if (hourOfDay == 0) 12 else hourOfDay
            selectedTime = String.format("%d:%02d %s", hour12, minute, amPm)
            btnTime.text = selectedTime
            updateContinueButtonState()
        }, hour, minute, false).show()
    }

    private fun updateContinueButtonState() {
        val quantity = etQuantity.text.toString().trim()
        val isQuantityValid = quantity.isNotBlank() && quantity.toDoubleOrNull() != null && quantity.toDouble() > 0

        val isScheduleValid = if (isScheduled) {
            selectedDate.isNotBlank() && selectedTime.isNotBlank()
        } else {
            true // ASAP always valid
        }

        btnContinue.isEnabled = isQuantityValid && isScheduleValid
    }

    private fun proceedToNextScreen() {
        val quantity = etQuantity.text.toString().trim().toDoubleOrNull() ?: 0.0
        val notes = etNotes.text.toString().trim()
        val pickupTime = if (isScheduled) {
            "$selectedDate at $selectedTime"
        } else {
            "ASAP"
        }

        // Get selected types from intent (passed from WasteTypeActivity)
        val selectedTypes = intent.getStringArrayExtra("selected_types") ?: arrayOf()

        // Navigate to Location screen with all data
        val intent = Intent(this, LocationActivity::class.java).apply {
            putExtra("selected_types", selectedTypes)
            putExtra("quantity", quantity)
            putExtra("notes", notes)
            putExtra("pickup_time", pickupTime)
        }
        startActivity(intent)
    }
    // Extension function to convert dp to pixels
    private fun Int.dpToPx(): Int {
        return (this * resources.displayMetrics.density).toInt()
    }
}