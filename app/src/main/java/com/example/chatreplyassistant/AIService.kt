package com.example.chatreplyassistant

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import org.json.JSONObject

class AIService {
    private val client = OkHttpClient()
    private val mediaType = "application/json".toMediaType()
    
    suspend fun generateResponse(prompt: String, tone: String): String {
        val apiKey = "YOUR_API_KEY" // Replace with your API key
        val apiUrl = "https://api.openai.com/v1/chat/completions"
        
        val messages = listOf(
            mapOf("role" to "system", "content" to "你是一个聊天助手，根据用户输入和指定语气生成回复。只回复内容，不要解释说明。"),
            mapOf("role" to "user", "content" to "语气要求: $tone\n用户输入: $prompt")
        )
        
        val requestBody = JSONObject().apply {
            put("model", "gpt-3.5-turbo")
            put("messages", messages)
            put("temperature", 0.7)
        }.toString()
        
        val request = Request.Builder()
            .url(apiUrl)
            .post(RequestBody.create(mediaType, requestBody))
            .addHeader("Authorization", "Bearer $apiKey")
            .build()
        
        val response = client.newCall(request).execute()
        val responseBody = response.body?.string() ?: throw Exception("Empty response")
        
        val jsonResponse = JSONObject(responseBody)
        return jsonResponse.getJSONArray("choices")
            .getJSONObject(0)
            .getJSONObject("message")
            .getString("content")
    }
}
