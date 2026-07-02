package com.airhud.app.ai

import com.airhud.app.data.AiConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * Minimal client for the OpenAI-compatible "chat completions" API shape, which is
 * shared by OpenAI, OpenRouter and Ollama's OpenAI-compatible endpoint. Good enough
 * for the MVP "type a question on the phone, see the answer on the HUD" flow.
 */
class AiClient {

    private val http = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    suspend fun ask(prompt: String, config: AiConfig): Result<String> = withContext(Dispatchers.IO) {
        try {
            val body = JSONObject().apply {
                put("model", config.model)
                put(
                    "messages",
                    JSONArray().put(
                        JSONObject().apply {
                            put("role", "user")
                            put("content", prompt)
                        }
                    )
                )
            }

            val url = config.baseUrl.trimEnd('/') + "/chat/completions"
            val requestBuilder = Request.Builder()
                .url(url)
                .post(body.toString().toRequestBody("application/json".toMediaType()))

            if (config.apiKey.isNotBlank()) {
                requestBuilder.addHeader("Authorization", "Bearer ${config.apiKey}")
            }

            http.newCall(requestBuilder.build()).execute().use { response ->
                val raw = response.body?.string().orEmpty()
                if (!response.isSuccessful) {
                    val concise = runCatching {
                        JSONObject(raw).getJSONObject("error").getString("message")
                    }.getOrDefault(raw.take(120))
                    return@withContext Result.failure(IOException("HTTP ${response.code}: $concise"))
                }
                val content = JSONObject(raw)
                    .getJSONArray("choices")
                    .getJSONObject(0)
                    .getJSONObject("message")
                    .getString("content")
                Result.success(content.trim())
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
