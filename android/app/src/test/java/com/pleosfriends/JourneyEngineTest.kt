package com.pleosfriends

import org.junit.Assert.*
import org.junit.Test

class JourneyEngineTest {
    @Test fun `destination heading never treats every hotel as Busan`() {
        assertEquals("서울시 강남구",JourneyEngine.region(TravelState(destination="서울 강남구 호텔")))
        assertEquals("부산광역시 해운대구",JourneyEngine.region(TravelState(destination="파라다이스호텔 부산")))
        assertEquals("대전역 주변",JourneyEngine.region(TravelState(destination="대전역")))
    }
    private fun state() = TravelState(destination = "부산 호텔", groupName = "동기 여행", members = defaultTravelers().take(3), invited = 2, joined = 2,
        days = listOf(DayPlan(listOf(DemoPlaces.cafe)), DayPlan(), DayPlan()), nights = 2)
    @Test fun `three vehicles can start without a fourth invitation`() { assertTrue(TripRules.start(state()).started) }
    @Test fun `personal stop affects only the owner and preserves the original plan`() {
        val initial = state(); val changed = JourneyEngine.commit(initial,RouteChange("one",DemoPlaces.rest,0,"me",RouteScope.PERSONAL),"I'll stop briefly")
        assertEquals(listOf("휴게소",DemoPlaces.cafe.name),JourneyEngine.visibleStops(changed,"me").map { it.name })
        assertEquals(listOf(DemoPlaces.cafe),JourneyEngine.visibleStops(changed,"a"))
        assertEquals(JourneyEngine.etaMinutes(initial,"me")+12,JourneyEngine.etaMinutes(changed,"me"))
        assertEquals(JourneyEngine.etaMinutes(initial,"a"),JourneyEngine.etaMinutes(changed,"a"))
        assertEquals(initial.days,changed.days)
    }
    @Test fun `group update reaches every member exactly once`() {
        val s=state();val c=RouteChange("group",DemoPlaces.rest,0,"me",RouteScope.GROUP)
        val updated=JourneyEngine.commit(s,c,"함께 쉬자")
        updated.members.forEach { assertEquals("휴게소",JourneyEngine.visibleStops(updated,it.id).first().name) }
        assertEquals(updated,JourneyEngine.commit(updated,c,"duplicate"))
        assertEquals(listOf("a","b"),updated.notes.single().recipients)
    }
    @Test fun `reply retains original notification and all itinerary rows`() {
        val original=JourneyEngine.commit(state(),RouteChange("r",DemoPlaces.rest,0,"me",RouteScope.GROUP)).copy(viewerId="a")
        val reply=JourneyEngine.reply(original,"좋아, 거기서 만나")
        assertEquals(2,reply.notes.size);assertEquals(original.changes,reply.changes)
        assertEquals("a",reply.notes.last().from);assertTrue("me" in reply.notes.last().recipients)
    }
    @Test fun `game never changes vehicle routes`() {
        val initial=state();val withGame=initial.copy(game=LadderState("점심",listOf("me","a","b"),"a",true))
        assertEquals(JourneyEngine.visibleStops(initial),JourneyEngine.visibleStops(withGame))
        assertEquals(JourneyEngine.etaMinutes(initial),JourneyEngine.etaMinutes(withGame))
    }
    @Test fun `filters sort actual results and preserve bookmarked identity`() {
        val s=state().copy(destination="성수 연무장",picks=PicksFilter(category="카페",sort="가까운순"))
        val rows=PickCatalog.filtered(s)
        assertTrue(rows.all { it.category=="카페" });assertEquals(rows.map { it.distance }.sorted(),rows.map { it.distance })
    }
}
