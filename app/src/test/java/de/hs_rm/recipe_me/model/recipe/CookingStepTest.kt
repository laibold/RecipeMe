package de.hs_rm.recipe_me.model.recipe

import org.junit.Assert.*
import org.junit.Test

class CookingStepTest {

    /**
     * Test if time is correctly converted to seconds
     */
    @Test
    fun getTimeSuccessful() {
        val seconds = 70
        val minutes = 75
        val hours = 3

        val secondStep = CookingStep("", seconds, TimeUnit.SECOND)
        assertEquals(seconds, secondStep.getTimeInSeconds())

        val minuteStep = CookingStep("", minutes, TimeUnit.MINUTE)
        assertEquals(minutes * 60, minuteStep.getTimeInSeconds())

        val hourStep = CookingStep("", hours, TimeUnit.HOUR)
        assertEquals(hours * 3600, hourStep.getTimeInSeconds())
    }

}