package com.lapoushko.core_network

import okhttp3.Headers
import okhttp3.Interceptor
import okhttp3.MediaType
import okhttp3.Response
import okio.Buffer
import java.io.IOException

private const val MAX_BODY_BYTES = 64L * 1024

/**
 * Захватывает каждый HTTP-обмен (запрос/ответ/ошибку) и передаёт его в [NetworkTrafficRecorder],
 * не влияя на основной поток запроса — тело читается через отдельный [Buffer]/peekBody.
 */
class NetworkTrafficInterceptor(
    private val recorderProvider: () -> NetworkTrafficRecorder
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val startedAt = System.currentTimeMillis()
        val requestBody = request.body

        val requestBodyText = if (requestBody == null) {
            null
        } else if (isPlaintext(requestBody.contentType())) {
            runCatching {
                Buffer().apply { requestBody.writeTo(this) }.readString(Charsets.UTF_8)
            }.getOrNull()
        } else {
            "<binary ${requestBody.contentLength()} bytes>"
        }

        try {
            val response = chain.proceed(request)
            val durationMillis = System.currentTimeMillis() - startedAt

            val responseBodyText = response.body?.let { body ->
                if (isPlaintext(body.contentType())) {
                    runCatching { response.peekBody(MAX_BODY_BYTES).string() }.getOrNull()
                } else {
                    "<binary ${body.contentLength()} bytes>"
                }
            }

            recorderProvider().record(
                NetworkTrafficEntry(
                    timestampEpochMillis = startedAt,
                    method = request.method,
                    url = request.url.toString(),
                    requestHeaders = request.headers.toLogString(),
                    requestBody = requestBodyText,
                    responseCode = response.code,
                    responseHeaders = response.headers.toLogString(),
                    responseBody = responseBodyText,
                    durationMillis = durationMillis,
                    errorMessage = null
                )
            )
            return response
        } catch (error: IOException) {
            val durationMillis = System.currentTimeMillis() - startedAt
            recorderProvider().record(
                NetworkTrafficEntry(
                    timestampEpochMillis = startedAt,
                    method = request.method,
                    url = request.url.toString(),
                    requestHeaders = request.headers.toLogString(),
                    requestBody = requestBodyText,
                    responseCode = null,
                    responseHeaders = null,
                    responseBody = null,
                    durationMillis = durationMillis,
                    errorMessage = error.message ?: error::class.java.simpleName
                )
            )
            throw error
        }
    }
}

private fun Headers.toLogString(): String =
    (0 until size).joinToString(separator = "\n") { "${name(it)}: ${value(it)}" }

private fun isPlaintext(mediaType: MediaType?): Boolean {
    if (mediaType == null) return true
    if (mediaType.type == "text") return true
    return when (mediaType.subtype) {
        "json", "xml", "html", "x-www-form-urlencoded", "plain" -> true
        else -> false
    }
}
