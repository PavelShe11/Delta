package io.github.pavelshel1.delta.about

import org.junit.Assert.assertEquals
import org.junit.Test

class AdminTapTest {

    @Test
    fun `series continues within gap and restarts after longer pause`() {
        assertEquals(4, nextAdminTapCount(3, sinceLastTapMs = 1_000))
        assertEquals(1, nextAdminTapCount(6, sinceLastTapMs = 1_001))
    }
}
