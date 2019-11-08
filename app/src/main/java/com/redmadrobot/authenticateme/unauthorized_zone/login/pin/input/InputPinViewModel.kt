package com.redmadrobot.authenticateme.unauthorized_zone.login.pin.input

import android.app.Application
import android.os.Handler
import android.util.Base64
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.redmadrobot.authenticateme.App
import com.redmadrobot.authenticateme.StorageKey
import com.redmadrobot.authenticateme.internal.utils.Pbkdf2Factory
import com.redmadrobot.authenticateme.internal.utils.Sha256
import com.redmadrobot.authenticateme.unauthorized_zone.login.pin.AuthenticationState
import java.security.GeneralSecurityException


class InputPinViewModel(application: Application) : AndroidViewModel(application) {
    val authenticationState = MutableLiveData<AuthenticationState>()

    private val preferences by lazy { getApplication<App>().encryptedStorage }
    private val aead by lazy { getApplication<App>().pinSecuredAead }

    init {
        // It's just to emulate "hard work" behind the splash screen
        Handler().postDelayed({
            authenticationState.value = if (preferences.getBoolean(StorageKey.PIN_IS_ENABLED, false)) {
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
}
