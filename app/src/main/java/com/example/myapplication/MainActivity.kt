package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.viewpager2.widget.ViewPager2
import com.example.myapplication.api.RetrofitClient
import kotlinx.coroutines.*

class MainActivity : AppCompatActivity() {

    private lateinit var cardDonor: CardView
    private lateinit var cardCollector: CardView
    private lateinit var btnTestApi: Button
    private lateinit var vpCarousel: ViewPager2

    private val carouselImages = listOf(
        R.drawable.image2,
        R.drawable.image3,
        R.drawable.image4
    )

    private val autoSlideHandler = Handler(Looper.getMainLooper())
    private val autoSlideRunnable = object : Runnable {
        override fun run() {
            if (::vpCarousel.isInitialized && carouselImages.isNotEmpty()) {
                val next = (vpCarousel.currentItem + 1) % carouselImages.size
                vpCarousel.setCurrentItem(next, true)
                autoSlideHandler.postDelayed(this, 3000)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initViews()
        setupClickListeners()
        setupCarousel()
    }

    private fun initViews() {
        cardDonor = findViewById(R.id.cardDonor)
        cardCollector = findViewById(R.id.cardCollector)
        btnTestApi = findViewById(R.id.btnTestApi)
        vpCarousel = findViewById(R.id.vpCarousel)
    }

    private fun setupClickListeners() {
        cardDonor.setOnClickListener {
            startActivity(Intent(this, WasteTypeActivity::class.java))
        }

        cardCollector.setOnClickListener {
            startActivity(Intent(this, CollectorRegistrationActivity::class.java))
        }

        btnTestApi.setOnClickListener {
            testApiConnection()
        }
    }

    private fun setupCarousel() {
        vpCarousel.adapter = ImageCarouselAdapter(carouselImages)

        vpCarousel.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                autoSlideHandler.removeCallbacks(autoSlideRunnable)
                autoSlideHandler.postDelayed(autoSlideRunnable, 3000)
            }
        })
    }

    private fun testApiConnection() {
        // Show loading message
        Toast.makeText(this, "Connecting to API...", Toast.LENGTH_SHORT).show()

        // Run network call on background thread
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.apiService.checkHealth().execute()

                // Switch back to main thread to show result
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        val health = response.body()
                        Toast.makeText(
                            this@MainActivity,
                            "✅ Connected! Status: ${health?.status}",
                            Toast.LENGTH_LONG
                        ).show()
                    } else {
                        Toast.makeText(
                            this@MainActivity,
                            "❌ Error: ${response.code()}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@MainActivity,
                        "❌ Failed: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        autoSlideHandler.postDelayed(autoSlideRunnable, 3000)
    }

    override fun onPause() {
        autoSlideHandler.removeCallbacks(autoSlideRunnable)
        super.onPause()
    }

    override fun onDestroy() {
        autoSlideHandler.removeCallbacks(autoSlideRunnable)
        super.onDestroy()
    }
}