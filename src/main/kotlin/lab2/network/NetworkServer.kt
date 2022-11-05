package lab2.network

import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration
import java.time.temporal.ChronoUnit
import java.util.stream.Collectors

open class NetworkServer(private val config: ServerConfig) {
    private val client: HttpClient = HttpClient.newHttpClient()

    fun <R> requestSync(
        method: String,
        params: Map<String, String>? = null,
        handler: (String) -> R?) : R? {
        try {
            val response = client.send(
                HttpRequest
                    .newBuilder(createURI(method, params))
                    .timeout(AWAIT_DURATION)
                    .GET()
                    .build(),
            ) { it ->
                HttpResponse.BodySubscribers.mapping(
                    HttpResponse.BodyHandlers.ofString().apply(it)
                ) {
                    handler(it)
                }
            }

            return response.body()
        } catch (e: Exception) {
            return null
        }
    }

    private fun createURI(method: String, params: Map<String, String>?): URI {
        return URI.create(
            "${config.host}/$method"
            + if (params != null) {
                "?" + params.entries
                    .map { "${it.key}=${it.value}"}
                    .stream()
                    .collect(Collectors.joining("&"))
            } else {
                ""
            }
        )
    }

    companion object {
        val AWAIT_DURATION: Duration = Duration.of(5, ChronoUnit.SECONDS)
    }
}