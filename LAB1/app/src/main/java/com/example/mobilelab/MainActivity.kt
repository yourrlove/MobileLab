package com.example.mobilelab

import android.content.res.ColorStateList
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import androidx.core.graphics.toColorInt

class MainActivity : AppCompatActivity() {

    private lateinit var buttonSubmit: Button
    private lateinit var imageSentiment: ImageView
    private lateinit var rootLayout: LinearLayout
    private lateinit var editTextInput: EditText
    private lateinit var buttonSwitch: Button  // Button to switch layout

    private val positiveRegex = Regex("\\b(good|great|happy|awesome|fantastic|amazing|excellent)\\b", RegexOption.IGNORE_CASE)
    private val negativeRegex = Regex("\\b(bad|sad|unhappy|terrible|worst|hate|depressing)\\b", RegexOption.IGNORE_CASE)
    private val neutralRegex = Regex("\\b(neutral)\\b", RegexOption.IGNORE_CASE)

    private var isFirstLayout = true  // Track current layout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.homework1)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize switch button
        buttonSwitch = findViewById(R.id.buttonSwitch)
        buttonSwitch.setOnClickListener {
            switchLayout()
        }
    }

    private fun switchLayout() {
        if (isFirstLayout) {
            setContentView(R.layout.homework2) // Switch to homework2.xml
            isFirstLayout = false
            setupHomework2Views()
        } else {
            setContentView(R.layout.homework1) // Switch to homework1.xml
            isFirstLayout = true
        }
        buttonSwitch = findViewById(R.id.buttonSwitch)
        buttonSwitch.setOnClickListener {
            switchLayout()
        }
    }

    private fun setupHomework2Views() {
        // Initialize views in homework2
        rootLayout = findViewById(R.id.LinerLayout)
        editTextInput = findViewById(R.id.editTextInput)
        buttonSubmit = findViewById(R.id.buttonSubmit)
        imageSentiment = findViewById(R.id.imageSentiment)
        buttonSubmit.setOnClickListener {
            val userInput = editTextInput.text.toString()
            if (userInput.isNotEmpty()) {
                analyzeSentiment(userInput)
            }
        }
    }

//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.homework2)
//
//
//        editTextInput = findViewById(R.id.editTextInput)
//        buttonSubmit = findViewById(R.id.buttonSubmit)
//        imageSentiment = findViewById(R.id.imageSentiment)
//        rootLayout = findViewById(R.id.LinerLayout)
//
//        buttonSubmit.setOnClickListener {
//            val userInput = editTextInput.text.toString()
//            if (userInput.isNotEmpty()) {
//                analyzeSentiment(userInput)
//            }
//        }
//    }

    private fun analyzeSentiment(text: String) {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://generativelanguage.googleapis.com/") // ✅ Base URL must end in /
            .addConverterFactory(GsonConverterFactory.create(GsonBuilder().create()))
            .client(OkHttpClient())
            .build()

        val service = retrofit.create(ApiService::class.java)

        val requestBody = SentimentRequest(
            contents = listOf(
                Content(parts = listOf(Part(text = "Analyze the sentiment of this text and classify it as 'happy', 'neutral', or 'sad'. Only return the classification (one word). Text: $text")))
            )
        )

        service.analyzeSentiment(
            contentType = "application/json",
            apiKey = "AIzaSyAn7ThOCBW8tnTBarWNT0vuyH94Lj30I20",  // Your API Key
            requestBody = requestBody
        ).enqueue(object : Callback<SentimentResponse> {
            override fun onResponse(call: Call<SentimentResponse>, response: Response<SentimentResponse>) {
                if (response.isSuccessful) {
//                    val gson = GsonBuilder().setPrettyPrinting().create()
//                    val json = gson.toJson(response.body())
//                    Log.d("API_SUCCESS", "Response:\n$json")
                    response.body()?.let { handleSentimentResult(it) }
                } else {
                    Log.e("API_ERROR", "Error: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<SentimentResponse>, t: Throwable) {
                Log.e("API_FAILURE", "Failure: ${t.message}")
            }
        })
    }


    private fun handleSentimentResult(response: SentimentResponse) {
        if (response.candidates.isNotEmpty()) {
            val responseText = response.candidates[0].content.parts[0].text.trim()

            runOnUiThread {
                when {
                    positiveRegex.containsMatchIn(responseText) -> {
                        rootLayout.setBackgroundColor("#00B66C".toColorInt())
                        imageSentiment.setImageResource(R.drawable.ic_happy)
                        buttonSubmit.backgroundTintList = ColorStateList.valueOf("#00B66C".toColorInt())
                        Log.d("API_RESPONSE", "Positive sentiment detected: $responseText")
                    }
                    negativeRegex.containsMatchIn(responseText) -> {
                        rootLayout.setBackgroundColor("#721A26".toColorInt())
                        imageSentiment.setImageResource(R.drawable.ic_sad)
                        buttonSubmit.backgroundTintList = ColorStateList.valueOf("#721A26".toColorInt())
                        Log.d("API_RESPONSE", "Negative sentiment detected: $responseText")
                    }
                    neutralRegex.containsMatchIn(responseText) || responseText.endsWith("?") -> {
                        rootLayout.setBackgroundColor("#195e83".toColorInt())
                        imageSentiment.setImageResource(R.drawable.ic_neutral)
                        buttonSubmit.backgroundTintList = ColorStateList.valueOf("#195e83".toColorInt())
                        Log.d("API_RESPONSE", "Neutral sentiment detected: $responseText")
                    }
                    else -> {
                        rootLayout.setBackgroundColor("#195e83".toColorInt())
                        imageSentiment.setImageResource(R.drawable.ic_neutral)
                        buttonSubmit.backgroundTintList = ColorStateList.valueOf("#195e83".toColorInt())
                        Log.d("API_RESPONSE", "Unclassified sentiment: $responseText")
                    }
                }
            }
        } else {
            Log.e("API_ERROR", "Empty response from Gemini API")
        }
    }

}