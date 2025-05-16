package com.example.hackaton

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

import androidx.compose.ui.unit.sp
import retrofit2.*
import retrofit2.converter.gson.GsonConverterFactory

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
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
    }

    private fun askGemini(prompt: String, onResponse: (String) -> Unit) {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://generativelanguage.googleapis.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val api = retrofit.create(GeminiApi::class.java)
        val request = GeminiRequest(
            contents = listOf(Content(parts = listOf(Part(text = prompt))))
        )

        val apiKey = "AIzaSyBhUnruHaNIMcZgVvoVl3HcUjP5Iou5KLE"


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
}

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
            label = { Text("Zadaj pytanie") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { onSend(prompt) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Wyślij")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = responseText,
            fontSize = 16.sp,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewGeminiChatScreen() {
    MaterialTheme {
        GeminiChatScreen(onSend = {}, responseText = "Odpowiedź pojawi się tutaj")
    }
}

