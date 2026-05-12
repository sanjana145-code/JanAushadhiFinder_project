package com.example.janaushadhifinder.ai

import com.example.janaushadhifinder.BuildConfig
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class GeminiRepository {
    private val api = Retrofit.Builder()
        .baseUrl("https://generativelanguage.googleapis.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(GeminiApi::class.java)

    suspend fun ask(prompt: String): Result<String> {
        if (BuildConfig.GEMINI_API_KEY.isBlank()) {
            return Result.failure(IllegalStateException("Gemini API key is not configured. Add GEMINI_API_KEY to local.properties."))
        }
        return runCatching {
            val response = api.generateContent(
                apiKey = BuildConfig.GEMINI_API_KEY,
                request = GeminiRequest(
                    contents = listOf(GeminiContent(parts = listOf(GeminiPart(prompt))))
                )
            )
            response.candidates.firstOrNull()
                ?.content
                ?.parts
                ?.joinToString("\n") { it.text }
                ?.takeIf { it.isNotBlank() }
                ?: "No response returned."
        }
    }
}
