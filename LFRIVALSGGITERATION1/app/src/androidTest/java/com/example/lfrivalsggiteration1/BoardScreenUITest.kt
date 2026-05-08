package com.example.lfrivalsggiteration1

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.lfrivalsggiteration1.ui.MainActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BoardScreenUITest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    private fun login() {
        composeTestRule.onNodeWithText("Username").performTextReplacement("Acehlers")
        composeTestRule.onNodeWithText("Password").performTextInput("Ace112305")
        composeTestRule.onNodeWithText("LOGIN").performClick()
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodesWithText("LFG Board").fetchSemanticsNodes().isNotEmpty()
        }
    }

    @Test
    fun boardScreen_titleIsDisplayed() {
        login()
        composeTestRule.onNodeWithText("LFG Board").assertIsDisplayed()
    }

    @Test
    fun boardScreen_searchBarIsDisplayed() {
        login()
        composeTestRule.onNodeWithText("Search by hero, role, rank…").assertIsDisplayed()
    }

    @Test
    fun boardScreen_fabIsDisplayed() {
        login()
        composeTestRule.onNodeWithContentDescription("Create Post").assertIsDisplayed()
    }
}