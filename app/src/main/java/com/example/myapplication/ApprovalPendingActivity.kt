package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class ApprovalPendingActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_approval_pending)

        // Get data from previous screen
        val name = intent.getStringExtra("name") ?: "John Doe"
        val phone = intent.getStringExtra("phone") ?: "+256 712 345 678"

        // Set the data
        findViewById<TextView>(R.id.tvName).text = name
        findViewById<TextView>(R.id.tvPhone).text = phone

        // Back to Home button
        findViewById<Button>(R.id.btnBackToHome).setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }
    }
}