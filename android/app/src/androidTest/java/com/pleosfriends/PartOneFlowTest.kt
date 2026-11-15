package com.pleosfriends

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.test.core.app.ActivityScenario
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.uiautomator.UiDevice
import java.io.File
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Runs against a parked Pleos Connect emulator, including its real AAOS UX restrictions. */
@RunWith(AndroidJUnit4::class)
class PartOneFlowTest {
    @get:Rule val compose = createEmptyComposeRule()
    private val instrumentation get() = InstrumentationRegistry.getInstrumentation()
    private fun click(tag: String) { compose.onNodeWithTag(tag).performClick() }
    private fun voice(expected: String) {
        click("gleo")
        compose.waitUntil(5000) { compose.onAllNodes(hasText(expected) and hasSetTextAction()).fetchSemanticsNodes().isNotEmpty() }
    }
    private fun screenshot(name: String) {
        compose.waitForIdle()
        val dir = File(instrumentation.targetContext.getExternalFilesDir(null), "screenshots").apply { mkdirs() }
        UiDevice.getInstance(instrumentation).takeScreenshot(File(dir, "$name.png"))
    }
    @Test fun videoJourneyAndRestart() {
        val context = instrumentation.targetContext
        context.getSharedPreferences("pleos_private_trip", 0).edit().clear().commit()
        var scenario = ActivityScenario.launch(MainActivity::class.java)
        try {
            screenshot("01-home")
            click("home-trip")
            compose.waitUntil(15000) { compose.onAllNodesWithTag("destination").fetchSemanticsNodes().isNotEmpty() }
            compose.onNodeWithTag("setup-next").assertIsNotEnabled()
            click("destination"); voice("성수 연무장"); click("input-save")
            click("duration"); compose.onNodeWithText("2박", useUnmergedTree = true).performClick(); click("duration-save")
            compose.onNodeWithText("3일차").assertIsDisplayed()
            click("edit-day-0")
            compose.onNodeWithTag("stop-input").performTextInput("휴게소"); click("add-stop")
            voice("한강")
            val candidate = compose.onNodeWithTag("candidate-stop").fetchSemanticsNode().boundsInRoot
            val drop = compose.onNodeWithTag("drop-zone-1").fetchSemanticsNode().boundsInRoot
            compose.onNodeWithTag("candidate-stop").performTouchInput {
                swipe(center, androidx.compose.ui.geometry.Offset(center.x, drop.center.y - candidate.top), durationMillis = 950)
            }
            compose.onNodeWithTag("candidate-stop").assertDoesNotExist()
            click("save-stops")
            screenshot("02-itinerary-three-columns")
            click("setup-next")
            compose.onNodeWithTag("friends-next").assertIsNotEnabled()
            click("group-name"); voice("여자들우정영원히"); click("input-save")
            compose.onAllNodesWithContentDescription("친구 초대").onFirst().performClick()
            compose.onNodeWithTag("invite-phone").performTextInput("123456789"); click("send-invite")
            compose.waitUntil(7000) { compose.onAllNodesWithText("4 / 4").fetchSemanticsNodes().isNotEmpty() }
            screenshot("03-friends")
            click("friends-next"); click("confirm-next"); screenshot("04-summary"); click("start-trip")
            compose.onNodeWithTag("group-map").assertExists(); screenshot("05-group-map")
            click("calendar"); compose.onNodeWithText("3일차").assertExists(); screenshot("06-calendar")
            click("close-panel"); click("notifications"); compose.onNodeWithText("함께하는 여행이 시작됐어요").assertExists(); click("close-panel")
            scenario.recreate()
            compose.onNodeWithTag("group-map").assertExists()
            compose.onNodeWithText("여자들우정영원히").assertExists()
            // Activity recreation also exercises Keystore deserialization into a new store.
            scenario.close()
            scenario = ActivityScenario.launch(MainActivity::class.java)
            compose.waitUntil(10000) { runCatching { compose.onAllNodesWithTag("group-map").fetchSemanticsNodes().isNotEmpty() }.getOrDefault(false) }
            compose.onNodeWithTag("group-map").assertExists()
        } finally { scenario.close() }
    }
}
