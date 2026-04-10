package helium314.keyboard.latin.utils

import org.json.JSONArray
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import android.util.Log

class GeminiTranslationService : TranslationService {
    override fun translate(text: String, apiKey: String, language: String): String? {
        if (apiKey.isBlank()) {
            Log.e("GeminiTranslationService", "Traducción abortada: API Key está vacía.")
            return null
        }
        if (text.isBlank()) {
            Log.e("GeminiTranslationService", "Traducción abortada: El texto ingresado está vacío.")
            return null
        }
        return try {
            val url = URL("https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=$apiKey")
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.setRequestProperty("Content-Type", "application/json")
            conn.doOutput = true
            
            val body = JSONObject()

            // Construct contents
            val contentsArray = JSONArray()
            val contentObj = JSONObject()
            val partsArray = JSONArray()
            val partObj = JSONObject()
            
            val prompt = """
                You are a professional translator with expertise in natural, idiomatic language.
                Rules:
                - Auto-detect the source language
                - Preserve the original tone, intent, and style (formal, casual, humorous, etc.)
                - Produce a natural translation that sounds native, not literal
                - Never add explanations, notes, or alternatives
                - Respond ONLY with the translated text, nothing else
                
                Translate the following text to $language:
                
                $text
            """.trimIndent()
            
            partObj.put("text", prompt)
            partsArray.put(partObj)
            contentObj.put("parts", partsArray)
            contentsArray.put(contentObj)
            
            body.put("contents", contentsArray)

            // Construct generationConfig
            val configObj = JSONObject()
            configObj.put("temperature", 0.3)
            configObj.put("maxOutputTokens", 500)
            body.put("generationConfig", configObj)
            
            val payload = body.toString()
            Log.d("GeminiTranslationService", "Request Payload: $payload")
            OutputStreamWriter(conn.outputStream).use { it.write(payload) }
            
            val responseCode = conn.responseCode
            if (responseCode == 200) {
                val responseStr = conn.inputStream.bufferedReader().use { it.readText() }
                Log.d("GeminiTranslationService", "Response JSON: $responseStr")
                val responseJson = JSONObject(responseStr)
                val candidates = responseJson.optJSONArray("candidates")
                if (candidates != null && candidates.length() > 0) {
                    val content = candidates.getJSONObject(0).optJSONObject("content")
                    val parts = content?.optJSONArray("parts")
                    if (parts != null && parts.length() > 0) {
                        return parts.getJSONObject(0).optString("text").trim()
                    }
                }
            } else {
                val errorMsg = conn.errorStream?.bufferedReader()?.use { it.readText() }
                Log.e("GeminiTranslationService", "Error HTTP $responseCode: $errorMsg")
            }
            null
        } catch (e: Exception) {
            Log.e("GeminiTranslationService", "Translation error", e)
            null
        }
    }
}
