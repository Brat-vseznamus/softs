package lab3.servlet

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import java.io.BufferedWriter
import java.io.PrintWriter
import java.nio.file.Files
import java.nio.file.Path
import java.sql.DriverManager
import java.util.stream.Collectors
import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

internal class ServerTest {

    @BeforeEach
    fun start() {
        DriverManager.getConnection("jdbc:sqlite:${DB_PATH.fileName}").use { c ->
            val sql =
                """
                    CREATE TABLE IF NOT EXISTS PRODUCT(
                        ID INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        NAME           TEXT    NOT NULL,
                        PRICE          INT     NOT NULL
                    )
                """
            val stmt = c.createStatement()
            stmt.executeUpdate(sql)
            stmt.close()
        }
    }


//    @Test
//    fun test() {
//        val t: GetProductsServlet = GetProductsServlet()
//
//        val req = request(mapOf())
//        val res = response()
//
//        t.doGet(req, res).apply {
//            res.writer.flush()
//        }
//
//
//        println(readResponse())
//    }
//
//    @AfterEach
//    fun stop() {
//        Files.deleteIfExists(DB_PATH)
//        Files.deleteIfExists(OUT_PATH)
//    }

    companion object {
        val GET_MATCHER = {numberOfRows: Int ->
            Regex("\\s*<html>\\s*<body>${"(\\s*(\\w+)\t(\\d+)</br>)".repeat(numberOfRows)}\\s*</body>\\s*</html>\\s*")
        }

        val DB_PATH: Path = Path.of("test.db")
        val OUT_PATH: Path = Path.of("out.txt")

        fun extractNameAndPrices(response: String, expectedNumberOfRows: Int) : List<Pair<String, String>>? {
            val list: MutableList<Pair<String, String>> = mutableListOf()
            try {
                val parseResult = GET_MATCHER(expectedNumberOfRows)
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