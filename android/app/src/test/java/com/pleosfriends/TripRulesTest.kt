package com.pleosfriends

import org.junit.Assert.*
import org.junit.Test

class TripRulesTest {
    @Test fun `changing nights preserves existing days and appends the third day`() {
        val state = TravelState(days = listOf(DayPlan(listOf(DemoPlaces.rest)), DayPlan()))
        val resized = TripRules.resize(state, 2)
        assertEquals(3, resized.days.size)
        assertEquals("휴게소", resized.days[0].stops[0].name)
        assertEquals(emptyList<Place>(), resized.days[2].stops)
    }
    @Test fun `dragging inserts at the requested position and reorders without duplicates`() {
        val initial = listOf(DemoPlaces.rest, DemoPlaces.cafe)
        val inserted = TripRules.insert(initial, DemoPlaces.river, 1)
        assertEquals(listOf("휴게소", "한강", "해피해피 식물카페"), inserted.map { it.name })
        assertEquals(listOf("한강", "해피해피 식물카페", "휴게소"), TripRules.move(inserted, 0, 3).map { it.name })
    }
    @Test fun `trip cannot start before destination and three friends are confirmed`() {
        assertFalse(TripRules.mayStart(TravelState()))
        val state = TravelState(destination = "성수 연무장", groupName = "여자들우정영원히", joined = 2)
        assertFalse(TripRules.mayConfirm(state))
        val started = TripRules.start(state.copy(joined = 3))
        assertEquals(Screen.HUB, started.screen)
        assertEquals("성수 연무장", started.destination)
        assertTrue(started.started)
    }
}
