package de.hs_rm.recipe_me.ui.profile.settings

import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import de.hs_rm.recipe_me.R
import de.hs_rm.recipe_me.declaration.launchFragmentInHiltContainer
import org.junit.Rule
import org.junit.Test

@HiltAndroidTest
class SiteNoticeFragmentTest {

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    /**
     * Test if current version (from build.gradle) is shown in fragment. Must be hard coded here
     */
    @Test
    fun canShowVersion() {
        launchFragmentInHiltContainer<SiteNoticeFragment>()
        val currentVersion = "Version: 2.2.0"
        onView(withId(R.id.version_text)).check(matches(withText(currentVersion)))
    }

}