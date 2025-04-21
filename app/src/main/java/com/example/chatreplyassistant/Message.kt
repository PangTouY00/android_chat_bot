package com.example.chatreplyassistant

data class Message(
    val id: Long = System.currentTimeMillis(),
    val content: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
    val tone: String? = null
)
