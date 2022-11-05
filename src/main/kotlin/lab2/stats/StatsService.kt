package lab2.stats

interface StatsService {
    fun getStatsRange(tag: String, hours: Int, endTime: Long): Array<Int?>
}