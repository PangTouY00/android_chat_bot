package com.example.chatreplyassistant

import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.IBinder
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ChatHeadService : Service() {
    private lateinit var windowManager: WindowManager
    private lateinit var chatHeadView: View
    private lateinit var expandedView: View
    private var isExpanded = false
    private val aiService = AIService()

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        
        // Create chat head view
        chatHeadView = LayoutInflater.from(this).inflate(R.layout.chat_head_layout, null)
        
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 0
            y = 100
        }
        
        windowManager.addView(chatHeadView, params)
        
        // Set up click listener for chat head
        chatHeadView.findViewById<ImageView>(R.id.chat_head).setOnClickListener {
            toggleExpandedView()
        }
        
        // Initialize expanded view but don't show it yet
        expandedView = LayoutInflater.from(this).inflate(R.layout.expanded_layout, null)
        setupExpandedView()
    }

    private fun setupExpandedView() {
        val sendButton = expandedView.findViewById<Button>(R.id.send_button)
        val messageInput = expandedView.findViewById<EditText>(R.id.message_input)
        val recyclerView = expandedView.findViewById<RecyclerView>(R.id.messages_recycler)
        val toneSpinner = expandedView.findViewById<Spinner>(R.id.tone_spinner)
        
        // Setup RecyclerView
        val adapter = MessageAdapter(mutableListOf())
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)
        
        sendButton.setOnClickListener {
            val message = messageInput.text.toString()
            if (message.isNotEmpty()) {
                val selectedTone = toneSpinner.selectedItem.toString()
                
                // Add user message
                adapter.addMessage(Message(content = message, isUser = true))
                messageInput.text.clear()
                
                // Generate AI response
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val aiResponse = aiService.generateResponse(message, selectedTone)
                        val aiMessage = Message(
                            content = aiResponse,
                            isUser = false,
                            tone = selectedTone
                        )
                        
                        launch(Dispatchers.Main) {
                            adapter.addMessage(aiMessage)
                            recyclerView.smoothScrollToPosition(adapter.itemCount - 1)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        launch(Dispatchers.Main) {
                            adapter.addMessage(Message(
                                content = "生成回复时出错: ${e.message}",
                                isUser = false
                            ))
                        }
                    }
                }
            }
        }
    }

    private fun toggleExpandedView() {
        if (isExpanded) {
            windowManager.removeView(expandedView)
        } else {
            val params = WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
            ).apply {
                gravity = Gravity.CENTER
            }
            
            windowManager.addView(expandedView, params)
        }
        isExpanded = !isExpanded
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isExpanded) windowManager.removeView(expandedView)
        windowManager.removeView(chatHeadView)
    }
}
