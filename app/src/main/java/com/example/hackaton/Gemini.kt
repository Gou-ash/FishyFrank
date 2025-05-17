package com.example.hackaton

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Query

// Main ask function
public fun askGemini(prompt: String, onResponse: (String) -> Unit) {
    // Build retrofit
    val retrofit = Retrofit.Builder()
        .baseUrl("https://generativelanguage.googleapis.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    // Generate request
    val api = retrofit.create(GeminiApi::class.java)
    val request = GeminiRequest(
        contents = listOf(Content(parts = listOf(Part(text =  GetDayplan(prompt)))))
    )

    // API KEY
    val apiKey = "AIzaSyBhUnruHaNIMcZgVvoVl3HcUjP5Iou5KLE"

    // Generate response
    api.generateContent(apiKey, request)
        .enqueue(object : Callback<GeminiResponse> {
            override fun onResponse(call: Call<GeminiResponse>, response: Response<GeminiResponse>) {
                if (!response.isSuccessful) {
                    val err = response.errorBody()?.string() ?: "Unknown error"
                    onResponse("Błąd API: $err")
                    return
                }
                val reply = response.body()?.candidates
                    ?.firstOrNull()?.content?.parts
                    ?.firstOrNull()?.text
                onResponse(reply ?: "Brak treści w odpowiedzi")
            }

            override fun onFailure(call: Call<GeminiResponse>, t: Throwable) {
                onResponse("Network error: ${t.message}")
            }
        })
}


//COMPOSABLE
@Composable
fun GeminiChatScreen(
    onSend: (String) -> Unit,
    responseText: String
) {
    var prompt by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        OutlinedTextField(
            value = prompt,
            onValueChange = { prompt = it },
            label = { Text("Opisz co chcesz robić dzisiaj") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { onSend(prompt) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Wygeneruj plan!")
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (responseText!="Odpowiedź pojawi się tutaj") {
            CreatePlan(stripFences(responseText))
        }
    }
}
@Composable
fun GeminiTest() {
    var responseText by remember { mutableStateOf("Odpowiedź pojawi się tutaj") }

    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            GeminiChatScreen(
                onSend = { prompt ->
                    askGemini(prompt) { reply ->
                        responseText = reply
                    }
                },
                responseText = responseText
            )
        }
    }

}

// UTILS
fun stripFences(raw: String): String {
    return raw
        .trim()
        .removePrefix("```json")
        .removeSuffix("```")
        .trim()
}

// DATA STRUCTURES
data class GeminiRequest(
    val contents: List<Content>
)
data class Content(
    val parts: List<Part>,
    val role: String = "user"
)
data class Part(
    val text: String
)
data class GeminiResponse(
    val candidates: List<Candidate>
)
data class Candidate(
    val content: Content
)

// API
interface GeminiApi {
    @Headers("Content-Type: application/json")
    @POST("v1beta/models/gemini-2.0-flash:generateContent")
    fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GeminiRequest
    ): Call<GeminiResponse>
}
