package lab2.stats

import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.mockito.Mockito.*
import java.util.Arrays
import java.util.Random
import java.util.stream.Collectors
import kotlin.test.assertContentEquals

internal class StatsServiceImplTest {
    private lateinit var resourceWithStats: ResourceWithStats
    private lateinit var statsService: StatsService

    @BeforeEach
    fun setUp() {
        resourceWithStats = mock(ResourceWithStats::class.java)
        statsService = StatsServiceImpl(resourceWithStats)
    }

    @Test
    fun `number of hours between 0 and 24`() {
        val curTime = 0L
        val anyTag = ""

        setUpStatsDistribution(curTime)

        assertThrows(AssertionError::class.java) {
            statsService.getStatsRange(anyTag, -1, curTime)
        }

        for (i in 0..24) {
            assertDoesNotThrow {
                statsService.getStatsRange(anyTag, i, curTime)
            }
        }

        assertThrows(AssertionError::class.java) {
            statsService.getStatsRange(anyTag, 25, curTime)
        }
    }

    @Test
    fun `for tags without stats return zeros`() {
        val curTime = 0L
        val nonExistingTag = "#itmo"

        setUpStatsDistribution(curTime)

        for (i in 0..24) {
            assertContentEquals(
                Array(i) {0},
                statsService.getStatsRange(nonExistingTag, i, curTime)
            )
        }
    }

    @Test
    fun `for tags with stats return same values for same hours independent from range length`() {
        val curTime = System.currentTimeMillis() / 1000L
        val itmoTag = "#itmo"
        val itmoStats = randomStats()

        setUpStatsDistribution(
            curTime,
            itmoTag to itmoStats
        )

        for (i in 0..24) {
            val stats = statsService.getStatsRange(itmoTag, i, curTime)
            val trueStats = scaleStats(itmoStats, i)
            assertContentEquals(
                trueStats,
                stats
            )
        }
    }


    @Test
    fun `for tags with stats return same values for same hours independent from time shift`() {
        val curTime = System.currentTimeMillis() / 1000L
        val itmoTag = "#itmo"
        val itmoStats = randomStats()

        setUpStatsDistribution(
            curTime,
            itmoTag to itmoStats
        )

        for (shift in -1..1) {
            val stats = statsService.getStatsRange(itmoTag, 24, curTime + shift * 3600)
            val trueStats = scaleStats(itmoStats, 24, shift)
            assertContentEquals(
                trueStats,
                stats
            )
        }
    }

    @Test
    fun `for interval which returns null set null in array`() {
        val curTime = System.currentTimeMillis() / 1000L
        val itmoTag = "#itmo"
        val itmoStats = randomStats()
        val brokenHour = 10

        setUpStatsDistribution(
            curTime,
            itmoTag to itmoStats
        )

        `when`(
            resourceWithStats.getStats(
                matches(itmoTag),
                eq(curTime - 3600 * brokenHour),
                eq(curTime - 3600 * (brokenHour - 1))
            )
        ).thenReturn(null)

        val stats = statsService.getStatsRange(itmoTag, 24, curTime)
        val trueStats = scaleStats(itmoStats, 24)

        trueStats[24 - brokenHour] = null

        assertContentEquals(
            trueStats,
            stats
        )
    }

    private fun setUpStatsDistribution(curTime: Long, vararg tagsAppearance: Pair<String, Array<Int>>) {
        val tagsToTheirTimeDistribution =
            tagsAppearance
                .map { (tag, tagAppearances) ->
                    val curLen = tagAppearances.size
                    val events = mutableListOf<Pair<String, Long>>()
                    val startTime = curTime - 3600 * curLen
                    for (hour in 0 until curLen) {
                        repeat(tagAppearances[hour]) {
                            val shiftInHours = hour + Math.random()
                            events.add(tag to (startTime + shiftInHours * 3600).toLong())
                        }
                    }
                    events
                }
                .flatten()
                .groupBy { it.first }
                .mapValues { (_, tagAppearances) ->
                    tagAppearances
                        .map { it.second to 1 }
                }

        `when`(resourceWithStats.getStats(anyString(), anyLong(), anyLong())).thenReturn(0)

        for ((tag, distribution) in tagsToTheirTimeDistribution) {

            `when`(resourceWithStats.getStats(matches(tag), anyLong(), anyLong())).then {
                val start: Long = (it.arguments[1]) as Long
                val end: Long = (it.arguments[2]) as Long

                if (start > end) {
                    0
                } else {
                    var count = 0

                    for ((time, curCount) in distribution) {
                        if (time in start until end) {
                            count += curCount
                        }
                    }

                    count
                }
            }
        }
    }

    // shift = distance between stats and newStats tails according to newStats
    // Example:
    //  stats:    . . . 0 1 2
    //  newStats: 0 0 0 0
    //  shift = 1
    private fun scaleStats(stats: Array<Int>, newSize: Int, shift: Int = 0) : Array<Int?> {
        assert(newSize >= 0)
        val newStats = Array<Int?>(newSize) {0}
        for ((i, stat) in stats.withIndex()) {
            val newIndex = newSize - stats.size + i - shift
            if (newIndex in 0 until newSize) {
                newStats[newIndex] = stat
            }
        }
        return newStats
    }

    private fun randomStats(): Array<Int> {
        return Array<Int>(24) {
            (Math.random() * 100 + 3).toInt()
        }
    }
}