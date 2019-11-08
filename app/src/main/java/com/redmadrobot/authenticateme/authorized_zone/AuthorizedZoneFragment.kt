package com.redmadrobot.authenticateme.authorized_zone

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.redmadrobot.authenticateme.R
import com.redmadrobot.authenticateme.unauthorized_zone.login.pin.input.InputPinViewModel


class AuthorizedZoneFragment : Fragment() {

    private val viewModel: InputPinViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.authorized_zone_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            viewModel.refuseAuthentication()
            findNavController().popBackStack(R.id.splashFragment, false)
        }
    }
}
