package de.hs_rm.recipe_me.model.recipe

import org.junit.Assert.*
import org.junit.Test

class CookingStepTest {

    /**
     * Test if time is correctly converted to seconds
     */
    @Test
    fun getTimeInSeconds() {
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

    /**
     * Test that equals() compares only text, time and unit
     */
    @Test
    fun testEquals() {
        val equalsId = 1L
        val equalsText = "textEquals"
        val equalsTime = 20
        val equalsUnit = TimeUnit.SECOND

        val differentId = 2L
        val differentText = "textNotEquals"
        val differentTime = 31
        val differentUnit = TimeUnit.MINUTE

        val original = CookingStep(equalsId, equalsText, equalsTime, equalsUnit)
        val equals = CookingStep(differentId, equalsText, equalsTime, equalsUnit)

        val different1 = CookingStep(equalsId, differentText, equalsTime, equalsUnit)
        val different2 = CookingStep(equalsId, equalsText, differentTime, equalsUnit)
        val different3 = CookingStep(equalsId, equalsText, equalsTime, differentUnit)

        assertEquals(original, original)
        assertEquals(original, equals)

        assertNotEquals(original, different1)
        assertNotEquals(original, different2)
        assertNotEquals(original, different3)
    }

}