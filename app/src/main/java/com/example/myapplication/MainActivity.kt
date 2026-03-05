package com.example.myapplication


//import androidx.activity.enableEdgeToEdge
//import androidx.core.view.ViewCompat
//import androidx.core.view.WindowInsetsCompat
import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Find views
        val cardDonor = findViewById<CardView>(R.id.cardDonor)
        val cardCollector = findViewById<CardView>(R.id.cardCollector)
        val tvLogin = findViewById<TextView>(R.id.tvLogin)

        // Set click listeners
        cardDonor.setOnClickListener {
            val intent = Intent(this, WasteTypeActivity::class.java)
            startActivity(intent)
        }

        //   navigate to Screen 3A
        cardCollector.setOnClickListener {
            cardCollector.setOnClickListener {
                val intent = Intent(this, CollectorRegistrationActivity::class.java)
                startActivity(intent)
            }
            // val intent = Intent(this, CollectorRegistrationActivity::class.java)
            // startActivity(intent)
        }

        tvLogin.setOnClickListener {
            // For now, just show a message
            Toast.makeText(this, "Login - coming soon!", Toast.LENGTH_SHORT).show()

            //  navigate to Login screen
            // val intent = Intent(this, LoginActivity::class.java)
            // startActivity(intent)
        }
    }
}