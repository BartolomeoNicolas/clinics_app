package com.bartolomeo.clinics.app.infrastructure.storage

import android.content.Context
import android.content.SharedPreferences
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import com.bartolomeo.clinics.app.domain.ports.out.SessionStore
import com.bartolomeo.clinics.app.infrastructure.network.nowEpochMillis
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

// El token se cifra con una llave del Keystore de Android: el material nunca sale
// del almacenamiento de llaves de la plataforma, asi que sacar el SharedPreferences
// del dispositivo no da nada legible.
//
// No se usa EncryptedSharedPreferences porque desde security-crypto 1.1.0 esta
// marcada como Deprecated, y este repo compila sin avisos.
class SecureTokenStore(context: Context) : SessionStore {

    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    override suspend fun token(): String? {
        val stored = prefs.getString(KEY_TOKEN, null) ?: return null
        val expires = prefs.getLong(KEY_EXPIRES, NO_EXPIRY).takeIf { it != NO_EXPIRY }
        if (expires != null && expires <= nowEpochMillis()) return null

        return runCatching { decrypt(stored) }.getOrNull()
    }

    override suspend fun save(token: String, expiresAtEpochMillis: Long?) {
        prefs.edit()
            .putString(KEY_TOKEN, encrypt(token))
            .putLong(KEY_EXPIRES, expiresAtEpochMillis ?: NO_EXPIRY)
            .apply()
    }

    override suspend fun clear() {
        prefs.edit().remove(KEY_TOKEN).remove(KEY_EXPIRES).apply()
    }

    // [1 byte con el largo del IV] [IV] [cifrado]. El IV cambia en cada escritura
    // (el Keystore lo genera), por eso hay que guardarlo junto al dato y no
    // derivarlo de la llave.
    private fun encrypt(value: String): String {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey())

        val iv = cipher.iv
        val data = cipher.doFinal(value.toByteArray(Charsets.UTF_8))
        val payload = ByteArray(1 + iv.size + data.size)

        payload[0] = iv.size.toByte()
        iv.copyInto(payload, destinationOffset = 1)
        data.copyInto(payload, destinationOffset = 1 + iv.size)

        return Base64.encodeToString(payload, Base64.NO_WRAP)
    }

    private fun decrypt(payload: String): String {
        val bytes = Base64.decode(payload, Base64.NO_WRAP)
        val ivSize = bytes[0].toInt() and 0xFF
        val iv = bytes.copyOfRange(1, 1 + ivSize)
        val data = bytes.copyOfRange(1 + ivSize, bytes.size)

        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.DECRYPT_MODE, secretKey(), GCMParameterSpec(TAG_BITS, iv))

        return cipher.doFinal(data).toString(Charsets.UTF_8)
    }

    private fun secretKey(): SecretKey {
        val entry = keyStore().getEntry(KEY_ALIAS, null) as? KeyStore.SecretKeyEntry
        if (entry != null) return entry.secretKey

        val generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEY_STORE)
        generator.init(
            KeyGenParameterSpec.Builder(
                KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT,
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .build(),
        )
        return generator.generateKey()
    }

    private fun keyStore(): KeyStore =
        KeyStore.getInstance(ANDROID_KEY_STORE).apply { load(null) }

    private companion object {
        const val ANDROID_KEY_STORE = "AndroidKeyStore"
        const val KEY_ALIAS = "clinics_session_key"
        const val TRANSFORMATION = "AES/GCM/NoPadding"
        const val TAG_BITS = 128

        const val PREFS_NAME = "clinics_session"
        const val KEY_TOKEN = "token"
        const val KEY_EXPIRES = "expires_at"
        const val NO_EXPIRY = -1L
    }
}
