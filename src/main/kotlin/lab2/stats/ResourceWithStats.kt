package lab2.stats

interface ResourceWithStats {
    fun getStats(tag: String, startDate: Long, endDate: Long): Int?
}