package lab2.network

import com.github.tomakehurst.wiremock.client.WireMock.*
import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo
import com.github.tomakehurst.wiremock.junit5.WireMockTest
import lab2.network.VkServerTest.Companion.PORT
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

@WireMockTest(httpPort = PORT)
internal class VkServerTest {
    private lateinit var vkServer: VkServer

    @BeforeEach
    fun start() {
        vkServer = VkServer(ACCESS_TOKEN, ServerConfig("http://localhost:$PORT"))
    }

    @Test
    fun `network correctly reacts for request`(wmRuntimeInfo: WireMockRuntimeInfo) {
        val expectedResult: Int = 10

        stubFor(any(urlMatching("/${VkServer.VK_NEWSFEED_SEARCH_METHOD}?.*"))
            .willReturn(ok(
                """
                    {
                        "response": {
                            "total_count": $expectedResult
                        }
                    }
                """.trimIndent()
            )))

        val actualResult = vkServer.getStats("", 0, 0)
        assertEquals(actualResult, expectedResult)
    }

    @Test
    fun `network reject request`(wmRuntimeInfo: WireMockRuntimeInfo) {
        stubFor(any(urlMatching("/${VkServer.VK_NEWSFEED_SEARCH_METHOD}?.*"))
            .willReturn(ok(
                """
                    {
                        "error": {
                            "status": "A lot requests"
                        }
                    }
                """.trimIndent()
            )))

        val actualResult = vkServer.getStats("", 0, 0)
        assertNull(actualResult)
    }

    companion object {
        const val PORT = 12345
        const val ACCESS_TOKEN = "token"
    }
}