package lab3.servlet

import lab3.service.ServiceConfig
import lab3.service.products.ProductService
import org.junit.jupiter.api.*
import org.mockito.Mockito.*
import java.io.BufferedWriter
import java.io.PrintWriter
import java.nio.file.Files
import java.nio.file.Path
import java.util.Collections.max
import java.util.Collections.min
import java.util.stream.Collectors
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

internal class ServerTest {
    private lateinit var productService: ProductService

    @BeforeEach
    fun start() {
        productService = ProductService(ServiceConfig("jdbc:sqlite:${DB_PATH.fileName}"))
    }


    @Test
    fun `get from empty table returns answer with 0 rows`() {
        val get = GetProductsServlet(productService)

        val req = request(mapOf())
        val res = response()

        assertDoesNotThrow {
            get.doGet(req, res).apply {
                res.writer.flush()
            }
        }

        val body = readResponse()

        assert(getMatcher(0).matches(body))
    }

    @Test
    fun `get from table after add returns same values that inserted`() {
        val add = AddProductServlet(productService)
        val name = "k1"
        val price = "1"

        val req1 = request(
            mapOf(
                "name" to name,
                "price" to price
            )
        )

        val res1 = response()

        assertDoesNotThrow {
            add.doGet(req1, res1).apply {
                res1.writer.flush()
                res1.writer.close()
            }
        }

        assertEquals(res1.status, HttpServletResponse.SC_OK)

        val get = GetProductsServlet(productService)

        val req2 = request(mapOf())
        val res2 = response()

        assertDoesNotThrow {
            get.doGet(req2, res2).apply {
                res2.writer.flush()
                res2.writer.close()
            }
        }

        val body = readResponse()

        assert(getMatcher(1).matches(body))
        assertContentEquals(
            listOf(name to price),
            extractNameAndPrices(body, 1)
        )
    }

    @Test
    fun `separate adds not overwrite values in table`() {
        val add = AddProductServlet(productService)
        val get = GetProductsServlet(productService)

        for (i in 0..1) {
            val name = "k$i"
            val price = "$i"

            val req = request(
                mapOf(
                    "name" to name,
                    "price" to price
                )
            )

            val res = response()

            assertDoesNotThrow {
                add.doGet(req, res).apply {
                    res.writer.flush()
                    res.writer.close()
                }
            }

            assertEquals(res.status, HttpServletResponse.SC_OK)
        }


        val getRequest = request(mapOf())
        val getResponse = response()

        assertDoesNotThrow {
            get.doGet(getRequest, getResponse).apply {
                getResponse.writer.flush()
                getResponse.writer.close()
            }
        }

        val body = readResponse()

        assert(getMatcher(2).matches(body))
        assertContentEquals(
            listOf("k0" to "0", "k1" to "1"),
            extractNameAndPrices(body, 2)
        )
    }

    // right now can be added without name because there is no checking for null 
    @Test
    fun `can't add without all required params`() {
        val add = AddProductServlet(productService)

        val req1 = request(mapOf())
        val res1 = response()

        assertThrows<Exception> {
            add.doGet(req1, res1).apply {
                res1.writer.flush()
                res1.writer.close()
            }
        }

        val get = GetProductsServlet(productService)

        val req2 = request(mapOf())
        val res2 = response()

        assertDoesNotThrow {
            get.doGet(req2, res2).apply {
                res2.writer.flush()
                res2.writer.close()
            }
        }

        val body = readResponse()

        assert(getMatcher(0).matches(body))
        assertContentEquals(
            listOf(),
            extractNameAndPrices(body, 0)
        )
    }

    @Test
    fun `price must be long`() {
        val add = AddProductServlet(productService)

        val req1 = request(mapOf("name" to "k1", "price" to "nonNumber"))
        val res1 = response()

        assertThrows<NumberFormatException> {
            add.doGet(req1, res1).apply {
                res1.writer.flush()
                res1.writer.close()
            }
        }

        val get = GetProductsServlet(productService)

        val req2 = request(mapOf())
        val res2 = response()

        assertDoesNotThrow {
            get.doGet(req2, res2).apply {
                res2.writer.flush()
                res2.writer.close()
            }
        }

        val body = readResponse()

        assert(getMatcher(0).matches(body))
        assertContentEquals(
            listOf(),
            extractNameAndPrices(body, 0)
        )
    }

    @Test
    fun `return 'unknown command' for query without param or param value is unknown`() {
        val query = QueryServlet(productService)

        val req1 = request(mapOf())
        val res1 = response()

        assertDoesNotThrow {
            query.doGet(req1, res1).apply {
                res1.writer.flush()
                res1.writer.close()
            }
        }

        assertEquals("Unknown command: null", readResponse())

        val unknownCommandName = "unknown"
        val req2 = request(mapOf("command" to unknownCommandName))
        val res2 = response()

        assertDoesNotThrow {
            query.doGet(req2, res2).apply {
                res2.writer.flush()
                res2.writer.close()
            }
        }

        assertEquals("Unknown command: $unknownCommandName", readResponse())
    }

    @Test
    fun `min query command works correctly`() {
        val command = "min"
        val prices = listOf(1, 2, 3)

        prepareData(prices)

        val expectedResult = min(prices)
        val query = QueryServlet(productService)

        val req = request(mapOf("command" to command))
        val res = response()

        assertDoesNotThrow {
            query.doGet(req, res).apply {
                res.writer.flush()
                res.writer.close()
            }
        }

        val expectedHTML = Regex("\\s*<html>" +
                "\\s*<body>" +
                "\\s*<h1>Product with min price: </h1>" +
                "\\s*k$expectedResult\t$expectedResult</br>" +
                "\\s*</body>" +
                "\\s*</html>\\s*")

        assertEquals(HttpServletResponse.SC_OK, res.status)
        assert(expectedHTML.matches(readResponse()))
    }

    @Test
    fun `min query command works correctly on empty table`() {
        val command = "min"
        val prices = emptyList<Int>()

        prepareData(prices)

        val query = QueryServlet(productService)

        val req = request(mapOf("command" to command))
        val res = response()

        assertDoesNotThrow {
            query.doGet(req, res).apply {
                res.writer.flush()
                res.writer.close()
            }
        }

        val expectedHTML = Regex("\\s*<html>" +
                "\\s*<body>" +
                "\\s*<h1>Product with min price: </h1>" +
                "\\s*</body>" +
                "\\s*</html>\\s*")

        assertEquals(HttpServletResponse.SC_OK, res.status)
        assert(expectedHTML.matches(readResponse()))
    }

    @Test
    fun `max query command works correctly`() {
        val command = "max"
        val prices = listOf(1, 2, 3)

        prepareData(prices)

        val expectedResult = max(prices)
        val query = QueryServlet(productService)

        val req = request(mapOf("command" to command))
        val res = response()

        assertDoesNotThrow {
            query.doGet(req, res).apply {
                res.writer.flush()
                res.writer.close()
            }
        }

        val expectedHTML = Regex("\\s*<html>" +
                "\\s*<body>" +
                "\\s*<h1>Product with max price: </h1>" +
                "\\s*k$expectedResult\t$expectedResult</br>" +
                "\\s*</body>" +
                "\\s*</html>\\s*")

        assertEquals(HttpServletResponse.SC_OK, res.status)
        assert(expectedHTML.matches(readResponse()))
    }

    @Test
    fun `max query command works correctly on empty table`() {
        val command = "max"
        val prices = emptyList<Int>()

        prepareData(prices)

        val query = QueryServlet(productService)

        val req = request(mapOf("command" to command))
        val res = response()

        assertDoesNotThrow {
            query.doGet(req, res).apply {
                res.writer.flush()
                res.writer.close()
            }
        }

        val expectedHTML = Regex("\\s*<html>" +
                "\\s*<body>" +
                "\\s*<h1>Product with max price: </h1>" +
                "\\s*</body>" +
                "\\s*</html>\\s*")

        assertEquals(HttpServletResponse.SC_OK, res.status)
        assert(expectedHTML.matches(readResponse()))
    }

    @Test
    fun `count query command works correctly`() {
        val command = "count"
        val prices = listOf(1, 2, 3)

        prepareData(prices)

        val expectedResult = prices.size
        val query = QueryServlet(productService)

        val req = request(mapOf("command" to command))
        val res = response()

        assertDoesNotThrow {
            query.doGet(req, res).apply {
                res.writer.flush()
                res.writer.close()
            }
        }

        val expectedHTML = Regex("\\s*<html>" +
                "\\s*<body>" +
                "\\s*Number of products:" +
                "\\s*$expectedResult" +
                "\\s*</body>" +
                "\\s*</html>\\s*")

        assertEquals(HttpServletResponse.SC_OK, res.status)
        assert(expectedHTML.matches(readResponse()))
    }

    @Test
    fun `sum query command works correctly`() {
        val command = "sum"
        val prices = listOf(1, 2, 3)

        prepareData(prices)

        val expectedResult = prices.sum()
        val query = QueryServlet(productService)

        val req = request(mapOf("command" to command))
        val res = response()

        assertDoesNotThrow {
            query.doGet(req, res).apply {
                res.writer.flush()
                res.writer.close()
            }
        }

        val expectedHTML = Regex("\\s*<html>" +
                "\\s*<body>" +
                "\\s*Summary price:" +
                "\\s*$expectedResult" +
                "\\s*</body>" +
                "\\s*</html>\\s*")

        assertEquals(HttpServletResponse.SC_OK, res.status)
        assert(expectedHTML.matches(readResponse()))
    }

    private fun prepareData(prices: List<Int>) {
        val add = AddProductServlet(productService)

        for (i in prices) {
            val name = "k$i"
            val price = "$i"

            val req = request(
                mapOf(
                    "name" to name,
                    "price" to price
                )
            )

            val res = response()

            add.doGet(req, res).apply {
                res.writer.flush()
                res.writer.close()
            }
        }
    }


    @AfterEach
    fun stop() {
        Files.deleteIfExists(DB_PATH)
        Files.deleteIfExists(OUT_PATH)
    }

    companion object {
        val DB_PATH: Path = Path.of("test.db")
        val OUT_PATH: Path = Path.of("out.txt")
        
        fun getMatcher(numberOfRows: Int): Regex {
            return Regex("\\s*<html>" +
                    "\\s*<body>" +
                    "(\\s*(\\w+)\t(\\d+)</br>)".repeat(numberOfRows) +
                    "\\s*</body>" +
                    "\\s*</html>\\s*")
        }
        
        fun extractNameAndPrices(response: String, expectedNumberOfRows: Int) : List<Pair<String, String>>? {
            val list: MutableList<Pair<String, String>> = mutableListOf()
            try {
                val parseResult = getMatcher(expectedNumberOfRows)
                    .find(response)!!
                    .destructured
                    .toList()

                repeat(expectedNumberOfRows) {
                    list.add(parseResult[3 * it + 1] to parseResult[3 * it + 2])
                }
                return list
            } catch (_: Exception) {}
            return null
        }

        fun outWriter(): BufferedWriter {
            return Files.newBufferedWriter(OUT_PATH)
        }

        fun readResponse(): String {
            return Files.newBufferedReader(OUT_PATH).lines().collect(Collectors.joining("\n"))
        }

        fun response(): HttpServletResponse {
            val response = mock(HttpServletResponse::class.java)

            val writer = PrintWriter(outWriter())

            `when`(response.getWriter()).thenReturn(writer)
            `when`(response.setContentType(anyString())).then {}

            val map: MutableMap<String, Any> = mutableMapOf()

            `when`(response.setStatus(anyInt())).then {
                map.put("status", it.getArgument<Int>(0))
            }
            `when`(response.getStatus()).then {
                map["status"]
            }

            return response
        }

        fun request(params: Map<String, String>): HttpServletRequest {
            val request = mock(HttpServletRequest::class.java)

            `when`(request.getParameter(anyString())).then {
                params[it.getArgument<String>(0)]
            }

            return request
        }
    }
}