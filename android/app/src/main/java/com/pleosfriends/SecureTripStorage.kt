package com.pleosfriends
import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import com.google.gson.Gson
import java.security.KeyStore
import java.util.concurrent.Executors
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
/** One encrypted journey, written in order. No invitation phone number or raw audio is persisted. */
class SecureTripStorage(context: Context) {
 private val preferences = context.getSharedPreferences("pleos_private_trip", Context.MODE_PRIVATE)
 private val writer = Executors.newSingleThreadExecutor()
 private val gson = Gson()
 private val key: SecretKey by lazy {
  val store = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
  (store.getKey("pleos.trip", null) as? SecretKey) ?: KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore").apply {
   init(KeyGenParameterSpec.Builder("pleos.trip", KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
    .setBlockModes(KeyProperties.BLOCK_MODE_GCM).setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE).build())
  }.generateKey()
 }
 fun load(): TravelState? = runCatching {
  val bytes = Base64.decode(preferences.getString("journey", null) ?: return null, Base64.NO_WRAP)
  val cipher = Cipher.getInstance("AES/GCM/NoPadding")
  cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(128, bytes.copyOfRange(0, 12)))
  val old = com.google.gson.JsonParser.parseString(String(cipher.doFinal(bytes.copyOfRange(12, bytes.size)), Charsets.UTF_8)).asJsonObject
  val merged = gson.toJsonTree(TravelState()).asJsonObject
  old.entrySet().forEach { merged.add(it.key, it.value) }
  val state = gson.fromJson(merged, TravelState::class.java)
  require(state.nights in 0..6 && state.days.size == state.nights + 1 && state.members.size in 2..4 && state.joined in 0 until state.members.size && state.invited in state.joined until state.members.size)
  state
 }.getOrNull()
 fun save(state: TravelState) {
  val json = gson.toJson(state)
  if (!writer.isShutdown) writer.execute {
   runCatching {
    val cipher = Cipher.getInstance("AES/GCM/NoPadding"); cipher.init(Cipher.ENCRYPT_MODE, key)
    val encoded = Base64.encodeToString(cipher.iv + cipher.doFinal(json.toByteArray(Charsets.UTF_8)), Base64.NO_WRAP)
    preferences.edit().putString("journey", encoded).commit()
   }
  }
 }
 fun close() { writer.shutdown(); writer.awaitTermination(500, java.util.concurrent.TimeUnit.MILLISECONDS) }
}
