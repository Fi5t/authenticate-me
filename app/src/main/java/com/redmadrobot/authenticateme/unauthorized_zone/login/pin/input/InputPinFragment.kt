package com.redmadrobot.authenticateme.unauthorized_zone.login.pin.input

import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.biometric.BiometricPrompt
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.redmadrobot.authenticateme.R
import com.redmadrobot.authenticateme.unauthorized_zone.login.pin.AuthenticationState
import com.redmadrobot.authenticateme.unauthorized_zone.login.pin.BiometricParams
import kotlinx.android.synthetic.main.input_pin_fragment.*
import java.util.concurrent.Executors


class InputPinFragment : Fragment() {

    private val viewModel: InputPinViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.input_pin_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        hidden_pin.onFilledListener = { viewModel.authenticate(it) }
        keyboard.keyboardClickListener = { hidden_pin.add(it) }

        keyboard.setRightCustomButton(R.drawable.icon_fingerprint) {
            viewModel.biometricAuthenticate()
        }

        val navController = findNavController()

        viewModel.authenticationState.observe(viewLifecycleOwner, Observer { authenticationState ->
            when (authenticationState) {
                AuthenticationState.AUTHENTICATED -> navController.navigate(R.id.authorizedZoneFragment)
                AuthenticationState.INVALID_AUTHENTICATION -> showErrorMessage()
            }
        })

        viewModel.biometricErrorMessage.observe(viewLifecycleOwner, Observer { message ->
            message.getContentIfNotHandled()?.let {
                Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            }
        })

        viewModel.biometricParams.observe(viewLifecycleOwner, Observer { params ->
            params.getContentIfNotHandled()?.let {
                showBiometricPrompt(it)
            }
        })
    }

    private fun showBiometricPrompt(params: BiometricParams) {
        val biometricPrompt = BiometricPrompt(
            this,
            Executors.newSingleThreadExecutor(),
            viewModel.authenticationCallback
        )

        biometricPrompt.authenticate(params.promptInfo, params.cryptoObject)
    }

    private fun showErrorMessage() {
        Toast.makeText(context, "Invalid PIN", Toast.LENGTH_SHORT).show()
        Handler().postDelayed({ hidden_pin.empty() }, 500)

    }
}
