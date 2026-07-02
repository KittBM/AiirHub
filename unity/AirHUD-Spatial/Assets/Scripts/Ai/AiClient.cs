using System;
using System.Collections;
using System.Text;
using UnityEngine;
using UnityEngine.Networking;
using AirHud.Data;

namespace AirHud.Ai
{
    /// <summary>
    /// Unity-side port of the Kotlin AiClient, for testing the AI widget without
    /// the phone bridge. Same OpenAI-compatible "chat completions" shape used by
    /// OpenAI, OpenRouter and Ollama.
    /// </summary>
    public static class AiClient
    {
        public static IEnumerator Ask(string prompt, AiConfig config, Action<string> onSuccess, Action<string> onError)
        {
            var body = new ChatRequest
            {
                model = config.model,
                messages = new[] { new ChatMessage { role = "user", content = prompt } },
            };
            string json = JsonUtility.ToJson(body);

            string url = config.baseUrl.TrimEnd('/') + "/chat/completions";
            using var request = new UnityWebRequest(url, "POST");
            request.uploadHandler = new UploadHandlerRaw(Encoding.UTF8.GetBytes(json));
            request.downloadHandler = new DownloadHandlerBuffer();
            request.SetRequestHeader("Content-Type", "application/json");
            if (!string.IsNullOrWhiteSpace(config.apiKey))
            {
                request.SetRequestHeader("Authorization", $"Bearer {config.apiKey}");
            }

            yield return request.SendWebRequest();

            if (request.result != UnityWebRequest.Result.Success)
            {
                string concise = TryExtractErrorMessage(request.downloadHandler.text) ?? Truncate(request.downloadHandler.text, 120);
                onError($"HTTP {request.responseCode}: {concise}");
                yield break;
            }

            try
            {
                var response = JsonUtility.FromJson<ChatResponse>(request.downloadHandler.text);
                onSuccess(response.choices[0].message.content.Trim());
            }
            catch (Exception e)
            {
                onError($"Failed to parse response: {e.Message}");
            }
        }

        private static string TryExtractErrorMessage(string raw)
        {
            try
            {
                var err = JsonUtility.FromJson<ErrorEnvelope>(raw);
                return err?.error?.message;
            }
            catch
            {
                return null;
            }
        }

        private static string Truncate(string s, int max) =>
            string.IsNullOrEmpty(s) || s.Length <= max ? s : s.Substring(0, max);

        [Serializable] private class ChatMessage { public string role; public string content; }
        [Serializable] private class ChatRequest { public string model; public ChatMessage[] messages; }
        [Serializable] private class ChatChoice { public ChatMessage message; }
        [Serializable] private class ChatResponse { public ChatChoice[] choices; }
        [Serializable] private class ErrorDetail { public string message; }
        [Serializable] private class ErrorEnvelope { public ErrorDetail error; }
    }
}
