package com.example.util

import android.content.Context
import android.speech.tts.TextToSpeech
import android.util.Log
import android.widget.Toast
import java.util.Locale

class TextToSpeechHelper(private val context: Context) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = TextToSpeech(context, this)
    private var isInitialized = false

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts?.setLanguage(Locale("vi", "VN"))
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                tts?.language = Locale.getDefault()
            }
            isInitialized = true
        } else {
            Log.e("TextToSpeechHelper", "TTS Initialization Failed")
        }
    }

    fun speak(text: String, speechRate: Float = 1.0f) {
        if (!isInitialized) {
            Toast.makeText(context, "Hệ thống âm thanh đang khởi tạo, vui lòng thử lại...", Toast.LENGTH_SHORT).show()
            return
        }
        tts?.setSpeechRate(speechRate)
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "TTS_ID_${System.currentTimeMillis()}")
    }

    fun stop() {
        if (tts?.isSpeaking == true) {
            tts?.stop()
        }
    }

    fun isSpeaking(): Boolean {
        return tts?.isSpeaking == true
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
    }
}
