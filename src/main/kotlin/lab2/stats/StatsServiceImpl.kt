package lab2.stats

class StatsServiceImpl(private val resourceWithStats: ResourceWithStats): StatsService {

    override fun getStatsRange(tag: String, hours: Int, endTime: Long): Array<Int?> {
        assert(hours in 0..24)
        val hourInSecs = 60 * 60
        return Array(hours) {
            val startDate = endTime - hourInSecs * (hours - it)
            val endDate = startDate + hourInSecs
            resourceWithStats.getStats(tag, startDate, endDate)
        }
    }

}