package com.redmadrobot.authenticateme.unauthorized_zone.login.pin.create

import android.app.Application
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import androidx.biometric.BiometricConstants
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.edit
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.redmadrobot.authenticateme.App
import com.redmadrobot.authenticateme.StorageKey
import com.redmadrobot.authenticateme.internal.utils.Pbkdf2Factory
import com.redmadrobot.authenticateme.internal.utils.Salt
import com.redmadrobot.authenticateme.internal.viewmodel.SingleLiveEvent
import com.redmadrobot.authenticateme.unauthorized_zone.login.pin.BiometricParams
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey


class CreatePinViewModel(application: Application) : AndroidViewModel(application) {
    companion object {
        private const val ANDROID_KEY_STORE = "AndroidKeyStore"
        private const val KEY_NAME = "biometric_key"
    }

    val pinIsCreated = MutableLiveData<Boolean>()
    val biometricEnableDialog = MutableLiveData<SingleLiveEvent<Unit>>()
    val biometricParams = MutableLiveData<BiometricParams>()

    val authenticationCallback = object : BiometricPrompt.AuthenticationCallback() {
        override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
            super.onAuthenticationError(errorCode, errString)
        }

        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
            super.onAuthenticationSucceeded(result)

            val encryptedSecretKey = result.cryptoObject?.cipher?.doFinal(secretKey.encoded)

            preferences.edit {
                putString(StorageKey.KEY, Base64.encodeToString(encryptedSecretKey, Base64.DEFAULT))
            }

            pinIsCreated.postValue(true)
        }

        override fun onAuthenticationFailed() {
            super.onAuthenticationFailed()
        }
    }

    private val fakeAccessToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzZWNyZXQiOiJXZSdyZSBoaXJpbmcgOykifQ.WZrEWG-l3VsJzJrbnjn2BIYO68gHIGyat6jrw7Iu-Rw"

    private val preferences by lazy { getApplication<App>().encryptedStorage }
    private val aead by lazy { getApplication<App>().pinSecuredAead }

    private val biometricManager by lazy { getApplication<App>().biometricManager }

    private val keyStore = KeyStore.getInstance(ANDROID_KEY_STORE)

    private lateinit var secretKey: SecretKey

    fun savePin(pin: String) {
        val salt = Salt.generate()
        secretKey = Pbkdf2Factory.createKey(pin.toCharArray(), salt)

        val encryptedToken = aead.encrypt(fakeAccessToken.toByteArray(), secretKey.encoded)

        preferences.edit {
            putString(StorageKey.TOKEN, Base64.encodeToString(encryptedToken, Base64.DEFAULT))
            putString(StorageKey.SALT, Base64.encodeToString(salt, Base64.DEFAULT))
            putBoolean(StorageKey.PIN_IS_ENABLED, true)
        }

        when (biometricManager.canAuthenticate()) {
            BiometricManager.BIOMETRIC_SUCCESS -> biometricEnableDialog.value = SingleLiveEvent(Unit)
        }
    }

    fun enableBiometric() {
        generateKey()

        val cipher = createCipher().also {
            preferences.edit {
                putString(StorageKey.KEY_IV, Base64.encodeToString(it.iv, Base64.DEFAULT))
            }
        }

        val promptInfo = createPromptInfo()
        val cryptoObject = BiometricPrompt.CryptoObject(cipher)

        biometricParams.value = BiometricParams(promptInfo, cryptoObject)
    }

    private fun createPromptInfo(): BiometricPrompt.PromptInfo {
        return BiometricPrompt.PromptInfo.Builder()
            .setTitle("Create biometric authorization")
            .setSubtitle("Provide your biometric credential")
            .setNegativeButtonText("Cancel")
            .build()
    }

    private fun generateKey() {
        try {
            keyStore.load(null)

            val keyProperties = KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            val builder = KeyGenParameterSpec.Builder(KEY_NAME, keyProperties)
                .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                .setUserAuthenticationRequired(true)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)

            val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEY_STORE)

            keyGenerator.run {
                init(builder.build())
                generateKey()
            }
        } catch (e: Exception) {
            authenticationCallback.onAuthenticationError(
                BiometricConstants.ERROR_NO_DEVICE_CREDENTIAL,
                e.localizedMessage
            )
        }
    }

    private fun createCipher(): Cipher {
        val key = with(keyStore) {
            load(null)
            getKey(KEY_NAME, null)
        }

        return Cipher.getInstance("${KeyProperties.KEY_ALGORITHM_AES}/${KeyProperties.BLOCK_MODE_CBC}/${KeyProperties.ENCRYPTION_PADDING_NONE}").apply {
            init(Cipher.ENCRYPT_MODE, key)
        }
    }
}
