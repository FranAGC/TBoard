package helium314.keyboard.latin.utils

import org.json.JSONArray
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import android.util.Log

class GroqTranslationService : TranslationService {
    override fun translate(text: String, apiKey: String, language: String): String? {
        if (apiKey.isBlank()) {
            Log.e("GroqTranslationService", "Traducción abortada: API Key está vacía.")
            return null
        }
        if (text.isBlank()) {
            Log.e("GroqTranslationService", "Traducción abortada: El texto ingresado está vacío.")
            return null
        }
        return try {
            val url = URL("https://api.groq.com/openai/v1/chat/completions")
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.setRequestProperty("Authorization", "Bearer $apiKey")
            conn.setRequestProperty("Content-Type", "application/json")
            conn.doOutput = true
            
            val body = JSONObject()
            body.put("model", "llama-3.1-8b-instant")
            val messages = JSONArray()

            val sysMsg = JSONObject()
            sysMsg.put("role", "system")
            sysMsg.put("content", """
                You are a professional translator with expertise in natural, idiomatic language.
                Rules:
                - Auto-detect the source language
                - Preserve the original tone, intent, and style (formal, casual, humorous, etc.)
                - Produce a natural translation that sounds native, not literal
                - Never add explanations, notes, or alternatives
                - Respond ONLY with the translated text, nothing else
            """.trimIndent())

            val usrMsg = JSONObject()
            usrMsg.put("role", "user")
            usrMsg.put("content", "Translate the following text to $language:\n\n$text")

            messages.put(sysMsg)
            messages.put(usrMsg)

            body.put("messages", messages)
            body.put("temperature", 0.3)
            body.put("max_tokens", 500)
            
            OutputStreamWriter(conn.outputStream).use { it.write(body.toString()) }
            
            val responseCode = conn.responseCode
            if (responseCode == 200) {
                val responseStr = conn.inputStream.bufferedReader().use { it.readText() }
                val responseJson = JSONObject(responseStr)
                val choices = responseJson.optJSONArray("choices")
                if (choices != null && choices.length() > 0) {
                    val message = choices.getJSONObject(0).optJSONObject("message")
                    return message?.optString("content")?.trim()
                }
            } else {
                val errorMsg = conn.errorStream?.bufferedReader()?.use { it.readText() }
                Log.e("GroqTranslationService", "Error HTTP $responseCode: $errorMsg")
            }
            null
        } catch (e: Exception) {
            Log.e("GroqTranslationService", "Translation error", e)
            null
        }
    }
}
