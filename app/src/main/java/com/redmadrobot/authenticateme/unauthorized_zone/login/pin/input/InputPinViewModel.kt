package com.redmadrobot.authenticateme.unauthorized_zone.login.pin.input

import android.app.Application
import android.os.Handler
import android.security.keystore.KeyProperties
import android.util.Base64
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.redmadrobot.authenticateme.App
import com.redmadrobot.authenticateme.StorageKey
import com.redmadrobot.authenticateme.internal.utils.Pbkdf2Factory
import com.redmadrobot.authenticateme.internal.utils.Sha256
import com.redmadrobot.authenticateme.internal.viewmodel.SingleLiveEvent
import com.redmadrobot.authenticateme.unauthorized_zone.login.pin.AuthenticationState
import com.redmadrobot.authenticateme.unauthorized_zone.login.pin.BiometricParams
import java.security.GeneralSecurityException
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec


class InputPinViewModel(application: Application) : AndroidViewModel(application) {
    companion object {
        private const val ANDROID_KEY_STORE = "AndroidKeyStore"
        private const val KEY_NAME = "biometric_key"
    }

    val authenticationState = MutableLiveData<AuthenticationState>()

    val biometricErrorMessage = MutableLiveData<SingleLiveEvent<String>>()

    val biometricParams = MutableLiveData<SingleLiveEvent<BiometricParams>>()

    private val preferences by lazy { getApplication<App>().encryptedStorage }
    private val aead by lazy { getApplication<App>().pinSecuredAead }

    private val biometricManager by lazy { getApplication<App>().biometricManager }

    private val keyStore = KeyStore.getInstance(ANDROID_KEY_STORE)

    val authenticationCallback = object : BiometricPrompt.AuthenticationCallback() {
        override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
            super.onAuthenticationError(errorCode, errString)
        }

        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
            super.onAuthenticationSucceeded(result)

            val encryptedSecretKey =
                Base64.decode(preferences.getString(StorageKey.KEY, ""), Base64.DEFAULT)
            val secretKey = result.cryptoObject?.cipher?.doFinal(encryptedSecretKey)

            val token = try {
                val encryptedToken =
                    Base64.decode(preferences.getString(StorageKey.TOKEN, null), Base64.DEFAULT)
                aead.decrypt(encryptedToken, secretKey)
            } catch (e: GeneralSecurityException) {
                null
            }

            val state = if (token?.isNotEmpty() == true) {
                AuthenticationState.AUTHENTICATED
            } else {
                AuthenticationState.INVALID_AUTHENTICATION
            }

            authenticationState.postValue(state)
        }

        override fun onAuthenticationFailed() {
            super.onAuthenticationFailed()
        }
    }

    init {
        // It's just to emulate "hard work" behind the splash screen
        Handler().postDelayed({
            authenticationState.value =
                if (preferences.getBoolean(StorageKey.PIN_IS_ENABLED, false)) {
                    AuthenticationState.UNAUTHENTICATED
                } else {
                    AuthenticationState.NO_PIN
                }
        }, 3000)
    }

    fun refuseAuthentication() {
        authenticationState.value = AuthenticationState.UNAUTHENTICATED
    }

    fun authenticate(pin: String) {
        authenticationState.value = if (pinIsValid(pin)) {
            AuthenticationState.AUTHENTICATED
        } else {
            AuthenticationState.INVALID_AUTHENTICATION
        }
    }

    fun biometricAuthenticate() {
        if (preferences.contains(StorageKey.KEY)) {
            when (biometricManager.canAuthenticate()) {
                BiometricManager.BIOMETRIC_SUCCESS -> {
                    val promptInfo = createPromptInfo()
                    val cryptoObject = BiometricPrompt.CryptoObject(createCipher())

                    biometricParams.value = SingleLiveEvent(BiometricParams(promptInfo, cryptoObject))
                }
            }
        } else {
            biometricErrorMessage.value =
                SingleLiveEvent("Biometric authentication isn't configured")
        }
    }

    private fun pinIsValid(pin: String): Boolean {
        val salt = Base64.decode(preferences.getString(StorageKey.SALT, null), Base64.DEFAULT)
        val secretKey = Pbkdf2Factory.createKey(pin.toCharArray(), salt)

        val token = try {
            val encryptedToken =
                Base64.decode(preferences.getString(StorageKey.TOKEN, null), Base64.DEFAULT)
            aead.decrypt(encryptedToken, secretKey.encoded)
        } catch (e: GeneralSecurityException) {
            null
        }

        return token?.isNotEmpty() ?: false
    }

    private fun createPromptInfo(): BiometricPrompt.PromptInfo {
        return BiometricPrompt.PromptInfo.Builder()
            .setTitle("Biometric login for my app")
            .setSubtitle("Log in using your biometric credential")
            .setNegativeButtonText("Cancel")
            .build()
    }

    private fun createCipher(): Cipher {
        val key = with(keyStore) {
            load(null)
            getKey(KEY_NAME, null)
        }

        return Cipher.getInstance("${KeyProperties.KEY_ALGORITHM_AES}/${KeyProperties.BLOCK_MODE_CBC}/${KeyProperties.ENCRYPTION_PADDING_NONE}").apply {
            val iv = Base64.decode(preferences.getString(StorageKey.KEY_IV, null), Base64.DEFAULT)

            init(Cipher.DECRYPT_MODE, key, IvParameterSpec(iv))
        }
    }
}
