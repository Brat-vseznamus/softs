package lab2.network

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import lab2.stats.ResourceWithStats
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

class VkServer(private val accessToken: String, config: ServerConfig = ServerConfig(VK_API_HOST)):
    NetworkServer(config),
    ResourceWithStats {

    override fun getStats(tag: String, startDate: Long, endDate: Long): Int? {
        Thread.sleep(100)
        return requestSync(
            method = VK_NEWSFEED_SEARCH_METHOD,
            params = mapOf(
                "access_token" to accessToken,
                "count" to "0",
                "q" to URLEncoder.encode(tag, StandardCharsets.UTF_8),
                "start_time" to "$startDate",
                "&end_time" to "$endDate",
                "v" to VK_VERSION
            )
        ) {
            try {
                jacksonObjectMapper().readValue(it, VkData::class.java)
                    .response
                    .total_count
            } catch (e: Exception) {
                null
            }
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class VkData(var response: Response)

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class Response(var total_count: Int)

    companion object {
        const val VK_API_HOST: String = "https://api.vk.com"
        const val VK_NEWSFEED_SEARCH_METHOD: String = "method/newsfeed.search"
        const val VK_VERSION = "5.131"
    }
}