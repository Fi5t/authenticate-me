package com.redmadrobot.authenticateme

import android.app.Application
import androidx.security.crypto.EncryptedSharedPreferences
import com.google.crypto.tink.aead.AeadFactory
import com.google.crypto.tink.aead.AeadKeyTemplates
import com.google.crypto.tink.integration.android.AndroidKeysetManager

class App : Application() {
    val encryptedStorage by lazy {
        EncryptedSharedPreferences.create(
            "main_storage",
            "main_storage_key",
            this,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    val pinSecuredAead by lazy {
        val keysetName = "pin_secured_keyset"
        val prefFileName = "pin_secured_key_preference"
        val masterKeyUri = "android-keystore://pin_secured_key"

        val keysetHandle = AndroidKeysetManager.Builder()
            .withSharedPref(
                this,
                keysetName,
                prefFileName
            )
            .withKeyTemplate(AeadKeyTemplates.AES256_GCM)
            .withMasterKeyUri(masterKeyUri)
            .build()
            .keysetHandle

        AeadFactory.getPrimitive(keysetHandle)
    }
}

