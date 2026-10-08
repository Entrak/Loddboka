package com.bearkingsoftware.loddboka.data

/**
 * Helpers for ticket-number ranges used during draws.
 * Keeps remaining tickets as IntRange lists instead of materializing every number.
 */
object TicketPool {
    fun rangeSize(range: IntRange): Long =
        (range.last.toLong() - range.first.toLong() + 1L).coerceAtLeast(0L)

    /** Remove a single number from a list of ranges, splitting as needed. */
    fun removeNumber(ranges: MutableList<IntRange>, number: Int) {
        val idx = ranges.indexOfFirst { number in it }
        if (idx < 0) return
        val range = ranges[idx]
        when {
            range.first == range.last -> ranges.removeAt(idx)
            number == range.first -> ranges[idx] = (number + 1)..range.last
            number == range.last -> ranges[idx] = range.first until number
            else -> {
                ranges[idx] = range.first until number
                ranges.add(idx + 1, (number + 1)..range.last)
            }
        }
    }
}
