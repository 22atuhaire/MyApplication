package com.example.myapplication

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import android.content.Intent
import android.util.Log

class CollectorRegistrationActivity : AppCompatActivity() {

    // Declare views
    private lateinit var etFullName: EditText
    private lateinit var etPhone: EditText
    private lateinit var etEmail: EditText
    private lateinit var spinnerVehicle: Spinner
    private lateinit var btnUploadFront: TextView
    private lateinit var btnUploadBack: TextView
    private lateinit var cbTerms: CheckBox
    private lateinit var btnSubmit: Button
    private lateinit var tvLoginLink: TextView

    // Track upload status
    private var isFrontUploaded = false
    private var isBackUploaded = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_collector_registration)

        // Initialize views
        initViews()

        // Setup vehicle spinner
        setupSpinner()

        // Setup all click listeners (this already includes upload buttons)
        setupClickListeners()
    }

    private fun initViews() {
        etFullName = findViewById(R.id.etFullName)
        etPhone = findViewById(R.id.etPhone)
        etEmail = findViewById(R.id.etEmail)
        spinnerVehicle = findViewById(R.id.spinnerVehicle)
        btnUploadFront = findViewById(R.id.btnUploadFront)
        btnUploadBack = findViewById(R.id.btnUploadBack)
        cbTerms = findViewById(R.id.cbTerms)
        btnSubmit = findViewById(R.id.btnSubmit)
        tvLoginLink = findViewById(R.id.tvLoginLink)

        // Set initial text from resources
        btnUploadFront.text = getString(R.string.id_front_upload)
        btnUploadBack.text = getString(R.string.id_back_upload)
    }

    private fun setupSpinner() {
        // Create adapter for spinner
        ArrayAdapter.createFromResource(
            this,
            R.array.vehicle_types,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            // Specify the layout to use when the list of choices appears
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            // Apply the adapter to the spinner
            spinnerVehicle.adapter = adapter
        }
    }

    private fun setupClickListeners() {
        // Upload Front button
        btnUploadFront.setOnClickListener {
            // For now, just simulate upload
            isFrontUploaded = true
            btnUploadFront.text = getString(R.string.id_front_uploaded)
            btnUploadFront.setTextColor(getColor(R.color.green_primary))
            checkFormValidity()
            Toast.makeText(this, getString(R.string.front_upload_demo), Toast.LENGTH_SHORT).show()
        }

        // Upload Back button
        btnUploadBack.setOnClickListener {
            // For now, just simulate upload
            isBackUploaded = true
            btnUploadBack.text = getString(R.string.id_back_uploaded)
            btnUploadBack.setTextColor(getColor(R.color.green_primary))
            checkFormValidity()
            Toast.makeText(this, getString(R.string.back_upload_demo), Toast.LENGTH_SHORT).show()
        }

        // Checkbox listener
        cbTerms.setOnCheckedChangeListener { _, _ ->
            checkFormValidity()
        }

        // Submit button
        btnSubmit.setOnClickListener {
            submitForm()
        }

        // Login link
        tvLoginLink.setOnClickListener {
            // Navigate to login screen (we'll create later)
            Toast.makeText(this, "Navigate to Login", Toast.LENGTH_SHORT).show()
        }

        // Text change listeners for required fields
        val textWatcher = object : android.text.TextWatcher {
            override fun afterTextChanged(s: android.text.Editable?) {
                checkFormValidity()
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        }

        etFullName.addTextChangedListener(textWatcher)
        etPhone.addTextChangedListener(textWatcher)
    }

    private fun checkFormValidity() {
        val name = etFullName.text.toString().trim()
        val phone = etPhone.text.toString().trim()
        val isNameValid = name.isNotBlank()
        val isPhoneValid = phone.isNotBlank()
        val isVehicleValid = spinnerVehicle.selectedItemPosition != 0 // Not "Select Vehicle"
        val isUploadValid = isFrontUploaded && isBackUploaded
        val isTermsChecked = cbTerms.isChecked

        // Log each condition to debug
        Log.d("FormValidation", "Name: '$name' - Valid: $isNameValid")
        Log.d("FormValidation", "Phone: '$phone' - Valid: $isPhoneValid")
        Log.d("FormValidation", "Vehicle Position: ${spinnerVehicle.selectedItemPosition} - Valid: $isVehicleValid")
        Log.d("FormValidation", "Front Uploaded: $isFrontUploaded, Back Uploaded: $isBackUploaded - Valid: $isUploadValid")
        Log.d("FormValidation", "Terms Checked: $isTermsChecked")

        btnSubmit.isEnabled = isNameValid && isPhoneValid && isVehicleValid &&
                isUploadValid && isTermsChecked

        Log.d("FormValidation", "Submit Button Enabled: ${btnSubmit.isEnabled}")
    }

    /**private fun submitForm() {
        Log.d("Registration", "submitForm() called")

        // Get all values
        val name = etFullName.text.toString().trim()
        val phone = etPhone.text.toString().trim()
        val email = etEmail.text.toString().trim().ifBlank { "Not provided" }
        val vehicle = spinnerVehicle.selectedItem.toString()

        // Log the values
        Log.d("Registration", "Name: $name")
        Log.d("Registration", "Phone: $phone")
        Log.d("Registration", "Email: $email")
        Log.d("Registration", "Vehicle: $vehicle")

        // Navigate to Approval Pending screen
        val intent = Intent(this, ApprovalPendingActivity::class.java)
        intent.putExtra("name", name)
        intent.putExtra("phone", phone)
        startActivity(intent)
    }**/

    private fun submitForm() {
        // Get all values
        val name = etFullName.text.toString().trim()
        val phone = etPhone.text.toString().trim()
        val email = etEmail.text.toString().trim().ifBlank { "Not provided" }
        val vehicle = spinnerVehicle.selectedItem.toString()

        // Log the values
        Log.d("Registration", "Submitting: $name, $phone, $email, $vehicle")

        // FOR DEVELOPMENT: Go directly to Login screen
        val intent = Intent(this, CollectorLoginActivity::class.java)
        startActivity(intent)

        // Comment out Approval Pending for now
        // val intent = Intent(this, ApprovalPendingActivity::class.java)
        // intent.putExtra("name", name)
        // intent.putExtra("phone", phone)
        // startActivity(intent)
    }
}