package com.redmadrobot.authenticateme.unauthorized_zone.splash

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.redmadrobot.authenticateme.R
import com.redmadrobot.authenticateme.unauthorized_zone.login.pin.AuthenticationState
import com.redmadrobot.authenticateme.unauthorized_zone.login.pin.input.InputPinViewModel


class SplashFragment : Fragment() {

    private val inputPinViewModel: InputPinViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.splash_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val navController = findNavController()

        inputPinViewModel.authenticationState.observe(viewLifecycleOwner, Observer { state ->
            when (state) {
                AuthenticationState.NO_PIN -> navController.navigate(R.id.createPinFragment)
                AuthenticationState.UNAUTHENTICATED -> navController.navigate(R.id.inputPinFragment)
            }
        })
    }
}
