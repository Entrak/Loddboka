package com.bearkingsoftware.loddboka.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TicketPoolTest {

    @Test
    fun rangeSize_inclusiveCount() {
        assertEquals(1L, TicketPool.rangeSize(5..5))
        assertEquals(10L, TicketPool.rangeSize(1..10))
        assertEquals(0L, TicketPool.rangeSize(5..4)) // empty-ish
    }

    @Test
    fun removeNumber_middle_splitsRange() {
        val ranges = mutableListOf(1..10)
        TicketPool.removeNumber(ranges, 5)
        assertEquals(listOf(1 until 5, 6..10), ranges)
    }

    @Test
    fun removeNumber_first_shrinksStart() {
        val ranges = mutableListOf(1..10)
        TicketPool.removeNumber(ranges, 1)
        assertEquals(listOf(2..10), ranges)
    }

    @Test
    fun removeNumber_last_shrinksEnd() {
        val ranges = mutableListOf(1..10)
        TicketPool.removeNumber(ranges, 10)
        assertEquals(listOf(1 until 10), ranges)
    }

    @Test
    fun removeNumber_onlyElement_removesRange() {
        val ranges = mutableListOf(7..7)
        TicketPool.removeNumber(ranges, 7)
        assertTrue(ranges.isEmpty())
    }

    @Test
    fun removeNumber_missing_noop() {
        val ranges = mutableListOf(1..5, 10..15)
        TicketPool.removeNumber(ranges, 7)
        assertEquals(listOf(1..5, 10..15), ranges)
    }
}
