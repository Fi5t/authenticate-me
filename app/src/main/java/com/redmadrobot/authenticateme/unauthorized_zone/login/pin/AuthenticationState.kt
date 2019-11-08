package com.redmadrobot.authenticateme.unauthorized_zone.login.pin


enum class AuthenticationState {
    UNAUTHENTICATED,        // Initial state, the user needs to authenticatek
    NO_PIN,                 // The user hasn't created PIN yet
    AUTHENTICATED,          // The user has authenticated successfully
    INVALID_AUTHENTICATION  // Authentication failed
}

