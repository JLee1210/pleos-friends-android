package com.pleosfriends

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.util.UUID

data class Place(val name: String = "", val address: String = "", val lat: Double = 37.5445, val lon: Double = 127.0566)
data class DayPlan(val stops: List<Place> = emptyList())
enum class Screen { HOME, SETUP, FRIENDS, CONFIRM, SUMMARY, HUB, PICKS, HISTORY }
enum class Panel { NONE, ITINERARY, NOTIFICATIONS, WAYPOINT, PICKS, GAME }
data class TravelState(
    val screen: Screen = Screen.HOME,
    val destination: String = "",
    val nights: Int = 1,
    val days: List<DayPlan> = List(2) { DayPlan() },
    val groupName: String = "",
    val invited: Int = 0,
    val joined: Int = 0,
    val elapsedSeconds: Int = 0,
    val started: Boolean = false,
    val panel: Panel = Panel.NONE,
    val tripId: String = UUID.randomUUID().toString(),
    val members: List<Traveler> = defaultTravelers(),
    val viewerId: String = "me",
    val story: StoryFixture? = null,
    val departureMinute: Int = 570,
    val activeDay: Int = 0,
    val changes: List<RouteChange> = emptyList(),
    val notes: List<TravelNote> = emptyList(),
    val bookmarks: List<String> = emptyList(),
    val completedStops: List<String> = emptyList(),
    val arrivals: List<String> = emptyList(),
    val game: LadderState = LadderState(),
    val journeyEnded: Boolean = false,
    val picks: PicksFilter = PicksFilter(),
)

object DemoPlaces {
    val home = Place("이태원", "서울 용산구 이태원로", 37.5348, 126.9947)
    val destination = Place("성수 연무장", "서울 성동구 연무장길", 37.5445, 127.0566)
    val rest = Place("휴게소", "시연 장소 · 옥수 한강공원", 37.5395, 127.0195)
    val river = Place("한강", "서울 성동구 옥수 한강공원", 37.5346, 127.0215)
    val cafe = Place("해피해피 식물카페", "서울 성동구 성수동 · 시연 장소", 37.546, 127.054)
    fun resolve(text: String, address: String = ""): Place {
        val name = text.trim()
        val known = when {
            name.contains("휴게") -> rest
            name.contains("한강") -> river
            name.contains("성수") || name.contains("연무장") -> destination
            name.contains("카페") -> cafe
            name.contains("이태원") -> home
            name.contains("부산") -> Place(name, "부산광역시 해운대구 해운대해변로 296", 35.1595, 129.1628)
            else -> Place(name, "직접 입력한 장소 · 지도 위치는 시연용")
        }
        return known.copy(name = name, address = address.ifBlank { known.address })
    }
}

object TripRules {
    fun resize(state: TravelState, nights: Int): TravelState {
        val safe = nights.coerceIn(0, 6)
        return state.copy(nights = safe, days = List(safe + 1) { state.days.getOrNull(it) ?: DayPlan() })
    }
    fun insert(stops: List<Place>, place: Place, index: Int): List<Place> {
        require(place.name.isNotBlank())
        return stops.toMutableList().apply { add(index.coerceIn(0, size), place) }
    }
    fun move(stops: List<Place>, from: Int, insertion: Int): List<Place> {
        if (from !in stops.indices) return stops
        return stops.toMutableList().apply {
            val place = removeAt(from)
            add((if (insertion > from) insertion - 1 else insertion).coerceIn(0, size), place)
        }
    }
    fun mayStart(state: TravelState) = state.destination.isNotBlank()
    fun mayConfirm(state: TravelState) = state.groupName.isNotBlank() && state.joined == state.members.size - 1
    fun start(state: TravelState): TravelState {
        require(mayStart(state) && mayConfirm(state))
        return state.copy(screen = Screen.HUB, started = true, elapsedSeconds = 0, panel = Panel.NONE)
    }
    fun nextPlace(state: TravelState) = JourneyEngine.visibleStops(state).firstOrNull { it.name !in state.completedStops }
        ?: JourneyEngine.resolve(state, state.destination)
}

class TripStore(private val disk: SecureTripStorage) {
    var state by mutableStateOf(disk.load() ?: TravelState())
        private set
    fun update(persist: Boolean = true, block: (TravelState) -> TravelState) {
        state = block(state)
        if (persist) disk.save(state)
    }
    fun go(screen: Screen) = update { it.copy(screen = screen, panel = Panel.NONE) }
    fun save() = disk.save(state)
    fun reset() = update { TravelState() }
    fun close() { save(); disk.close() }
}
