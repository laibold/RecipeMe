package de.hs_rm.recipe_me.model.user

import org.junit.Assert.*
import org.junit.Test

class UserTest {

    /**
     * Test default values for user
     */
    @Test
    fun testDefaultValues() {
        val user = User()

        assertEquals(0, user.id)
        assertEquals("", user.name)
    }
}
