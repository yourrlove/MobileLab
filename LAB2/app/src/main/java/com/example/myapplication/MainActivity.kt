package com.example.myapplication

import android.content.res.ColorStateList
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.toColorInt
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MainActivity : AppCompatActivity() {
    private lateinit var buttonSubmit: Button
    private lateinit var imageSentiment: ImageView
    private lateinit var rootLayout: LinearLayout
    private lateinit var editTextInput: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

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

    private fun analyzeSentiment(text: String) {
        val retrofit = Retrofit.Builder()
            .baseUrl("http://10.0.2.2:5000/") // ✅ Base URL must end in /
            .addConverterFactory(GsonConverterFactory.create(GsonBuilder().create()))
            .client(OkHttpClient())
            .build()

        val service = retrofit.create(ApiService::class.java)

        val requestBody = SentimentRequest(sentence = text)

        service.analyzeSentiment(
            requestBody = requestBody
        ).enqueue(object : Callback<SentimentResponse> {
            override fun onResponse(call: Call<SentimentResponse>, response: Response<SentimentResponse>) {
                if (response.isSuccessful) {
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
        if (response.result != null) {
            val responseText = response.result.label
            val responseScore = response.result.scores
            Log.d("API_RESPONSE", "Scores: $responseScore")
            runOnUiThread {
                when {
                    responseText == "POS" -> {
                        rootLayout.setBackgroundColor("#00B66C".toColorInt())
                        imageSentiment.setImageResource(R.drawable.ic_happy)
                        buttonSubmit.backgroundTintList = ColorStateList.valueOf("#00B66C".toColorInt())
                    }
                    responseText == "NEG" -> {
                        rootLayout.setBackgroundColor("#721A26".toColorInt())
                        imageSentiment.setImageResource(R.drawable.ic_sad)
                        buttonSubmit.backgroundTintList = ColorStateList.valueOf("#721A26".toColorInt())
                    }
                    responseText == "NEU" -> {
                        rootLayout.setBackgroundColor("#195e83".toColorInt())
                        imageSentiment.setImageResource(R.drawable.ic_neutral)
                        buttonSubmit.backgroundTintList = ColorStateList.valueOf("#195e83".toColorInt())
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
            Log.e("API_ERROR", "Empty response from PhoBERT API")
        }
    }

}