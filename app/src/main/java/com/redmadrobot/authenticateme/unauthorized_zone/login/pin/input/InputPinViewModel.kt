package com.redmadrobot.authenticateme.unauthorized_zone.login.pin.input

import android.app.Application
import android.content.Context
import android.os.Handler
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.redmadrobot.authenticateme.StorageKey
import com.redmadrobot.authenticateme.unauthorized_zone.login.pin.AuthenticationState


class InputPinViewModel(application: Application) : AndroidViewModel(application) {
    val authenticationState = MutableLiveData<AuthenticationState>()

    private val preferences by lazy {
        getApplication<Application>().getSharedPreferences("main", Context.MODE_PRIVATE)
    }

    init {
        // It's just to emulate "hard work" behind the splash screen
        Handler().postDelayed({
            authenticationState.value =
                if (preferences.getString(StorageKey.PIN, null).isNullOrBlank()) {
                    AuthenticationState.NO_PIN
                } else {
                    AuthenticationState.UNAUTHENTICATED
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
        return preferences.getString(StorageKey.PIN, null) == pin
    }
}
