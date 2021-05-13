package de.hs_rm.recipe_me.model.user

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class UserTest {

    /**
     * Test default values for user
     */
    @Test
    fun testDefaultValues() {
        val user = User()

        assertThat(user.id).isEqualTo(0)
        assertThat(user.name).isEqualTo("")
    }
}
