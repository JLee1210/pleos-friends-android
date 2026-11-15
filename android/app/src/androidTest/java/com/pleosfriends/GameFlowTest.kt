package com.pleosfriends

import android.os.Bundle
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class GameFlowTest {
    @get:Rule val compose=createEmptyComposeRule()
    @Test fun parkedLadderFinishesWithoutChangingRoute() {
        val scenario=ActivityScenario.launch(MainActivity::class.java)
        fun snapshot():TravelState { var s:TravelState?=null;scenario.onActivity { s=it.snapshotForRecording() };return s!! }
        try {
            scenario.onActivity { it.simulateForRecording("showcase") }
            compose.waitUntil(10000) { runCatching { compose.onAllNodesWithTag("group-map").fetchSemanticsNodes().isNotEmpty() }.getOrDefault(false) }
            compose.onNodeWithText("게임 제안").assertIsEnabled().performClick()
            compose.onNodeWithTag("game-reason").performTextInput("오늘 점심 쏘기")
            listOf("me","a","b").forEach { compose.onNodeWithTag("player-$it").performClick() }
            val before=snapshot().changes
            compose.onNodeWithTag("game-start").performScrollTo().assertIsEnabled().performClick()
            InstrumentationRegistry.getInstrumentation().sendStatus(2,Bundle().apply { putString("stream","After start: ${snapshot().game}\n") })
            compose.mainClock.advanceTimeBy(3500)
            compose.waitUntil(12000) { snapshot().game.finished }
            assertTrue(snapshot().game.winner in snapshot().game.participants)
            assertEquals(before,snapshot().changes)
        } finally { scenario.close() }
    }
}
