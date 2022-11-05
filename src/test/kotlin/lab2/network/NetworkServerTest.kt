package lab2.network

import com.github.tomakehurst.wiremock.client.WireMock.*
import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo
import com.github.tomakehurst.wiremock.junit5.WireMockTest
import lab2.network.NetworkServerTest.Companion.PORT
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@WireMockTest(httpPort = PORT)
internal class NetworkServerTest {
    private lateinit var networkServer: NetworkServer

    @BeforeEach
    fun start() {
        networkServer = NetworkServer(ServerConfig("http://localhost:$PORT"))
    }

    @Test
    fun `network correctly reacts for request`(wmRuntimeInfo: WireMockRuntimeInfo) {
        val methodName = "method"
        val expectedResult = "answer"

        stubFor(get("/$methodName").willReturn(ok(expectedResult)))

        val actualResult: String? = networkServer.requestSync(methodName) {
            it
        }

        assertNotNull(actualResult)
        assertEquals(expectedResult, actualResult)
    }

    @Test
    fun `network reject request`(wmRuntimeInfo: WireMockRuntimeInfo) {
        val methodName = "method"

        stubFor(get("/$methodName").willReturn(serverError()))

        val actualResult: String? = networkServer.requestSync(methodName) {
            it
        }

        assertNotNull(actualResult)
        assertEquals(actualResult, "")
    }

    @Test
    fun `request rejected by timeout or another client problem`(wmRuntimeInfo: WireMockRuntimeInfo) {
        val methodName = "method"

        networkServer = NetworkServer(ServerConfig("http://localhost:${PORT + 1}"))

        val actualResult: String? = networkServer.requestSync(methodName) {
            it
        }

        assertNull(actualResult)
    }

    @Test
    fun `request pass all params correctly`(wmRuntimeInfo: WireMockRuntimeInfo) {
        val methodName = "method"
        val params = mapOf(
            "k1" to "v1",
            "k2" to "v2"
        )

        stubFor(
            get("/$methodName")
                .willReturn(ok())
                .withQueryParams(params.mapValues { equalTo(it.value) }))

        val actualResult: String? = networkServer.requestSync(methodName, params) {
            it
        }

        assertNotNull(actualResult)
    }


    companion object {
        const val PORT = 12345
    }
}