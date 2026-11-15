package com.pleosfriends
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.google.gson.Gson
class MainActivity : ComponentActivity() {
 private lateinit var store: TripStore
 private lateinit var services: ConnectServices
 override fun onCreate(savedInstanceState: Bundle?) {
  super.onCreate(savedInstanceState)
  store = TripStore(SecureTripStorage(applicationContext))
  if (BuildConfig.DEMO_MODE && savedInstanceState == null && intent.getStringExtra("scenario") == "busan") {
   val fixture = Gson().fromJson(assets.open("busan-roadtrip.json").bufferedReader().use { it.readText() }, StoryFixture::class.java)
   store.update { JourneyEngine.seed(fixture) }
  }
  services = ConnectServices(this).also { it.initialize() }
  setContent { PleosTheme { PleosApp(store, services) } }
 }
 override fun onStart() { super.onStart(); if (::services.isInitialized) services.onForeground(true, store.state.started) }
 override fun onStop() { services.onForeground(false, false); store.save(); super.onStop() }
 override fun onDestroy() { services.close(); store.close(); super.onDestroy() }
 fun simulateForRecording(action: String, argument: String = "") {
  check(BuildConfig.DEMO_MODE)
  store.update { JourneyEngine.simulate(it, action, argument) }
 }
 fun presentDesignReview(state: TravelState) { check(BuildConfig.DEMO_MODE); store.update { state } }
 fun snapshotForRecording(): TravelState { check(BuildConfig.DEMO_MODE); return store.state }
}
