package lab3.servlet

import org.junit.jupiter.api.*
import org.mockito.Mockito.*
import java.io.BufferedWriter
import java.io.PrintWriter
import java.nio.file.Files
import java.nio.file.Path
import java.sql.DriverManager
import java.util.stream.Collectors
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

internal class ServerTest {

    @BeforeEach
    fun start() {
        DriverManager.getConnection("jdbc:sqlite:${DB_PATH.fileName}").use { c ->
            val sql =
                """
                    CREATE TABLE IF NOT EXISTS PRODUCT(
                        ID        INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        NAME      TEXT    NOT NULL,
                        PRICE     INT     NOT NULL
                    )
                """
            val stmt = c.createStatement()
            stmt.executeUpdate(sql)
            stmt.close()
        }
    }


    @Test
    fun `get from empty table returns answer with 0 rows`() {
        val get = GetProductsServlet()

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
        val add = AddProductServlet()
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

        val get = GetProductsServlet()

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
            extractNameAndPrices(body, 1),
            listOf(name to price)
        )
    }

    @Test
    fun `separate adds not overwrite values in table`() {
        val add = AddProductServlet()
        val get = GetProductsServlet()

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
            extractNameAndPrices(body, 2),
            listOf("k0" to "0", "k1" to "1")
        )
    }

    // right now can be added without name because there is no checking for null 
    @Test
    fun `can't add without all required params`() {
        val add = AddProductServlet()

        val req1 = request(mapOf())
        val res1 = response()

        assertThrows<Exception> {
            add.doGet(req1, res1).apply {
                res1.writer.flush()
                res1.writer.close()
            }
        }

        val get = GetProductsServlet()

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
            extractNameAndPrices(body, 0),
            listOf()
        )
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