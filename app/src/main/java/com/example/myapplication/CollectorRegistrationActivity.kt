package com.example.myapplication

import android.content.Intent
import android.util.Log
import android.os.Bundle
import android.text.InputType
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.myapplication.api.RetrofitClient
import com.example.myapplication.models.CollectorRegistrationRequest
import kotlinx.coroutines.*

class CollectorRegistrationActivity : AppCompatActivity() {

    // Views
    private lateinit var etFullName: EditText
    private lateinit var etPhone: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var etConfirmPassword: EditText
    private lateinit var ivPasswordToggle: ImageView
    private lateinit var ivConfirmPasswordToggle: ImageView
    private lateinit var spinnerVehicle: Spinner
    private lateinit var btnUploadFront: TextView
    private lateinit var btnUploadBack: TextView
    private lateinit var cbTerms: CheckBox
    private lateinit var btnSubmit: Button
    private lateinit var tvLoginLink: TextView
    private lateinit var progressBar: ProgressBar

    // Track upload status
    private var isFrontUploaded = false
    private var isBackUploaded = false

    // For API calls
    private val coroutineScope = CoroutineScope(Dispatchers.Main + Job())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_collector_registration)

        initViews()
        setupSpinner()
        setupClickListeners()
        checkFormValidity()
    }

    private fun initViews() {
        etFullName = findViewById(R.id.etFullName)
        etPhone = findViewById(R.id.etPhone)
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        etConfirmPassword = findViewById(R.id.etConfirmPassword)
        ivPasswordToggle = findViewById(R.id.ivPasswordToggle)
        ivConfirmPasswordToggle = findViewById(R.id.ivConfirmPasswordToggle)
        spinnerVehicle = findViewById(R.id.spinnerVehicle)
        btnUploadFront = findViewById(R.id.btnUploadFront)
        btnUploadBack = findViewById(R.id.btnUploadBack)
        cbTerms = findViewById(R.id.cbTerms)
        btnSubmit = findViewById(R.id.btnSubmit)
        tvLoginLink = findViewById(R.id.tvLoginLink)
        progressBar = findViewById(R.id.progressBar)

        // Set initial text from resources
        btnUploadFront.text = getString(R.string.id_front_upload)
        btnUploadBack.text = getString(R.string.id_back_upload)
    }

    private fun setupSpinner() {
        ArrayAdapter.createFromResource(
            this,
            R.array.vehicle_types,
            R.layout.spinner_vehicle_item
        ).also { adapter ->
            adapter.setDropDownViewResource(R.layout.spinner_vehicle_dropdown_item)
            spinnerVehicle.adapter = adapter
        }

        spinnerVehicle.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                checkFormValidity()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                checkFormValidity()
            }
        }
    }

    private fun setupClickListeners() {
        btnUploadFront.setOnClickListener {
            // Simulate upload
            isFrontUploaded = true
            btnUploadFront.text = getString(R.string.id_front_uploaded)
            btnUploadFront.setTextColor(ContextCompat.getColor(this, R.color.green_primary))
            checkFormValidity()
            Toast.makeText(this, getString(R.string.front_upload_demo), Toast.LENGTH_SHORT).show()
        }

        btnUploadBack.setOnClickListener {
            // Simulate upload
            isBackUploaded = true
            btnUploadBack.text = getString(R.string.id_back_uploaded)
            btnUploadBack.setTextColor(ContextCompat.getColor(this, R.color.green_primary))
            checkFormValidity()
            Toast.makeText(this, getString(R.string.back_upload_demo), Toast.LENGTH_SHORT).show()
        }

        cbTerms.setOnCheckedChangeListener { _, _ ->
            checkFormValidity()
        }

        btnSubmit.setOnClickListener {
            submitForm()
        }

        tvLoginLink.setOnClickListener {
            val intent = Intent(this, CollectorLoginActivity::class.java)
            startActivity(intent)
        }

        // Password visibility toggle
        ivPasswordToggle.setOnClickListener {
            togglePasswordVisibility(etPassword, ivPasswordToggle)
        }

        // Confirm Password visibility toggle
        ivConfirmPasswordToggle.setOnClickListener {
            togglePasswordVisibility(etConfirmPassword, ivConfirmPasswordToggle)
        }

        val textWatcher = object : android.text.TextWatcher {
            override fun afterTextChanged(s: android.text.Editable?) { checkFormValidity() }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        }

        etFullName.addTextChangedListener(textWatcher)
        etPhone.addTextChangedListener(textWatcher)
        etPassword.addTextChangedListener(textWatcher)
        etConfirmPassword.addTextChangedListener(textWatcher)
    }

    private fun isFormValid(): Boolean {
        val name = etFullName.text.toString().trim()
        val phone = etPhone.text.toString().trim()
        val password = etPassword.text.toString().trim()
        val confirmPassword = etConfirmPassword.text.toString().trim()
        val isNameValid = name.isNotBlank()
        val isPhoneValid = phone.isNotBlank()
        val isPasswordValid = password.length >= 6
        val isPasswordMatch = password == confirmPassword
        val isVehicleValid = spinnerVehicle.selectedItemPosition != 0
        val isUploadValid = isFrontUploaded && isBackUploaded
        val isTermsChecked = cbTerms.isChecked

        return isNameValid && isPhoneValid && isPasswordValid &&
                isPasswordMatch && isVehicleValid && isUploadValid && isTermsChecked
    }

    private fun checkFormValidity() {
        btnSubmit.isEnabled = isFormValid()
    }

    private fun submitForm() {
        if (!isFormValid()) {
            Toast.makeText(this, "Please complete all required fields", Toast.LENGTH_SHORT).show()
            return
        }

        // Get values
        val name = etFullName.text.toString().trim()
        val phone = etPhone.text.toString().trim()
        val email = etEmail.text.toString().trim().ifBlank { null }
        val password = etPassword.text.toString().trim()
        val confirmPassword = etConfirmPassword.text.toString().trim()
        val vehicle = spinnerVehicle.selectedItem.toString()

        // Validate passwords match
        if (password != confirmPassword) {
            Toast.makeText(this, getString(R.string.password_mismatch), Toast.LENGTH_SHORT).show()
            return
        }

        if (password.length < 6) {
            Toast.makeText(this, getString(R.string.password_too_short), Toast.LENGTH_SHORT).show()
            return
        }

        // Show progress
        showLoading(true)

        // Create request
        val request = CollectorRegistrationRequest(
            name = name,
            phone = phone,
            email = email,
            vehicle_type = vehicle,
            password = password
        )

        // Log the request for debugging
        Log.d("RegistrationRequest", "Name: $name, Phone: $phone, Email: $email, Vehicle: $vehicle")

        // Make API call
        coroutineScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    RetrofitClient.apiService.collectorRegister(request).execute()
                }

                showLoading(false)

                if (response.isSuccessful) {
                    val result = response.body()
                    Log.d("RegistrationSuccess", "Response: ${result?.message}")
                    if (result != null && result.success) {
                        // Registration successful
                        Toast.makeText(
                            this@CollectorRegistrationActivity,
                            result.message,
                            Toast.LENGTH_LONG
                        ).show()

                        // Navigate to Approval Pending screen
                        val intent = Intent(
                            this@CollectorRegistrationActivity,
                            ApprovalPendingActivity::class.java
                        )
                        intent.putExtra("name", name)
                        intent.putExtra("phone", phone)
                        startActivity(intent)
                        finish()
                    } else {
                        // Server returned error
                        Log.e("RegistrationError", "Server error: ${result?.message}")
                        Toast.makeText(
                            this@CollectorRegistrationActivity,
                            result?.message ?: "Registration failed",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                } else {
                    // HTTP error
                    val errorBody = response.errorBody()?.string()
                    Log.e("RegistrationError", "HTTP ${response.code()}: $errorBody")

                    Toast.makeText(
                        this@CollectorRegistrationActivity,
                        "Error: ${response.code()} - ${response.message()}\n$errorBody",
                        Toast.LENGTH_LONG
                    ).show()
                }
            } catch (e: Exception) {
                showLoading(false)

                // Log full error details
                Log.e("RegistrationError", "Exception", e)

                // Show more specific error message
                val errorMessage = when (e) {
                    is android.os.NetworkOnMainThreadException -> "Network call on main thread."
                    is java.net.ConnectException -> "Cannot connect to server. Check if Laravel is running."
                    is java.net.SocketTimeoutException -> "Connection timeout. Server not responding."
                    is java.net.UnknownHostException -> "Network error. Check your internet connection."
                    is java.io.IOException -> "Network I/O error: ${e.localizedMessage ?: "Unknown"}"
                    else -> "Unexpected error: ${e.javaClass.simpleName}"
                }

                Toast.makeText(
                    this@CollectorRegistrationActivity,
                    errorMessage,
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun showLoading(show: Boolean) {
        if (show) {
            progressBar.visibility = android.view.View.VISIBLE
            btnSubmit.isEnabled = false
        } else {
            progressBar.visibility = android.view.View.GONE
            checkFormValidity()
        }
    }

    private fun togglePasswordVisibility(editText: EditText, toggleIcon: ImageView) {
        val isPasswordVisible = editText.inputType == InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
        if (isPasswordVisible) {
            // Hide password
            editText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            toggleIcon.setImageResource(android.R.drawable.ic_menu_view)
        } else {
            // Show password
            editText.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            toggleIcon.setImageResource(android.R.drawable.ic_menu_close_clear_cancel)
        }
        editText.setSelection(editText.text.length)
    }

    override fun onDestroy() {
        super.onDestroy()
        coroutineScope.cancel()
    }
}