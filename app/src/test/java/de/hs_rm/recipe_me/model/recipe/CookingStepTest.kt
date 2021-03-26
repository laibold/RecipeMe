package de.hs_rm.recipe_me.model.recipe

import org.junit.Test
import com.google.common.truth.Truth.assertThat

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
        assertThat(secondStep.getTimeInSeconds()).isEqualTo(seconds)

        val minuteStep = CookingStep("", minutes, TimeUnit.MINUTE)
        assertThat(minuteStep.getTimeInSeconds()).isEqualTo(minutes * 60)

        val hourStep = CookingStep("", hours, TimeUnit.HOUR)
        assertThat(hourStep.getTimeInSeconds()).isEqualTo(hours * 3600)
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

        assertThat(original).isEqualTo(original)
        assertThat(equals).isEqualTo(original)

        assertThat(different1).isNotEqualTo(original)
        assertThat(different2).isNotEqualTo(original)
        assertThat(different3).isNotEqualTo(original)
    }
}
